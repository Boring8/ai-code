package com.easen.aicode.service.impl;


import com.easen.aicode.common.PageRequest;
import com.easen.aicode.constant.ThumbConstant;
import com.easen.aicode.hotkey.annotation.HotKeyCache;
import com.easen.aicode.mapper.ThumbMapper;
import com.easen.aicode.model.entity.App;
import com.easen.aicode.model.entity.Thumb;
import com.easen.aicode.model.vo.AppThumbVO;
import com.easen.aicode.service.AppService;
import com.easen.aicode.service.ThumbService;
import com.easen.aicode.utils.ThumbHotKeyUtil;
import com.easen.aicode.utils.RedisKeyUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;


/**
 * 服务层实现。
 *
 * @author <a>easen</a>
 */
@Slf4j
@Service("thumbService")
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb> implements ThumbService {

    @Resource
    private AppService appService;

    @Resource
    private final RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ThumbHotKeyUtil thumbHotKeyUtil;

    public ThumbServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    @Override
//    @HotKeyCache(prefix = "app_thumb_page:", keyParamIndex = 0, keyField = "pageNum", expireSeconds = 300)
    public Page<AppThumbVO> getAppThumbPage(PageRequest pageRequest) {
        // 创建分页对象
        Page<AppThumbVO> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());

        // 使用 QueryWrapper 构建查询，直接使用应用表中的 thumbCount 字段
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select("a.id, a.appName, a.thumbCount")
                .from("app").as("a")
                .where("a.isDelete = 0")
                .orderBy("a.thumbCount DESC, a.id DESC");

        return this.pageAs(page, queryWrapper, AppThumbVO.class);
    }

    @Override
    @Transactional
    public boolean likeApp(Long appId, Long userId) {
        // 检查是否已经点赞
        if (isUserLikedApp(appId, userId)) {
            return false; // 已经点赞过了
        }

        // 只记录到 Redis，由定时任务异步落库，避免与定时任务批量插入冲突
        recordThumbToRedis(userId, appId, 1);
        thumbHotKeyUtil.saveUserThumb(userId, appId);
        return true;
    }

    @Override
    @Transactional
    public boolean unlikeApp(Long appId, Long userId) {
        // 若未点赞，直接返回
        if (!isUserLikedApp(appId, userId)) {
            return false;
        }
        // 只记录到 Redis，由定时任务异步落库
        recordThumbToRedis(userId, appId, -1);
        // 移除热点缓存中的用户点赞记录
        thumbHotKeyUtil.removeUserThumb(userId, appId);
        return true;
    }

    @Override
    public boolean isUserLikedApp(Long appId, Long userId) {
        // 使用工具类检查用户是否已点赞应用
        return thumbHotKeyUtil.isUserLikedApp(userId, appId);
    }


    /**
     * 记录点赞操作到 Redis 临时数据中
     * 由定时任务 SyncThumb2DBJob 统一同步到数据库
     *
     * @param userId 用户ID
     * @param appId  应用ID
     * @param delta  点赞数变化量（1表示点赞，-1表示取消点赞）
     */
    private void recordThumbToRedis(Long userId, Long appId, int delta) {
        try {
            // 获取当前时间，按10秒为一个时间段
            LocalDateTime now = LocalDateTime.now();
            String timeKey = now.format(DateTimeFormatter.ofPattern("HH:mm:")) +
                    (now.getSecond() / 10) * 10;

            // 构造 Redis 临时数据 key
            String tempThumbKey = RedisKeyUtil.getTempThumbKey(timeKey);
            String userIdAppId = userId + ":" + appId;

            // 记录到 Redis 临时数据中
            redisTemplate.opsForHash().put(tempThumbKey, userIdAppId, delta);

            log.debug("记录点赞操作到 Redis 临时数据，userId: {}, appId: {}, delta: {}, timeKey: {}",
                    userId, appId, delta, timeKey);
        } catch (Exception e) {
            log.error("记录点赞操作到 Redis 临时数据失败，userId: {}, appId: {}, delta: {}",
                    userId, appId, delta, e);
        }
    }
}
