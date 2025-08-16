package com.easen.aicode.service.impl;


import com.easen.aicode.common.PageRequest;
import com.easen.aicode.constant.ThumbConstant;
import com.easen.aicode.mapper.ThumbMapper;
import com.easen.aicode.model.entity.App;
import com.easen.aicode.model.entity.Thumb;
import com.easen.aicode.model.vo.AppThumbVO;
import com.easen.aicode.service.AppService;
import com.easen.aicode.service.ThumbService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;


/**
 *  服务层实现。
 *
 * @author <a>easen</a>
 */
@Slf4j
@Service
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb>  implements ThumbService{

    @Resource
    private AppService appService;

    @Resource
    private final RedisTemplate<String, Object> redisTemplate;

    public ThumbServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    @Override
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
        
        // 创建点赞记录
        Thumb thumb = new Thumb();
        thumb.setAppId(appId);
        thumb.setUserId(userId);
        thumb.setCreateTime(LocalDateTime.now());
        
        boolean saveResult = this.save(thumb);
        
        // 如果点赞记录保存成功，更新应用的点赞数
        if (saveResult) {
            updateAppThumbCount(appId, 1);
            redisTemplate.opsForHash().put(ThumbConstant.USER_THUMB_KEY_PREFIX + userId.toString(), appId.toString(), thumb.getId());
        }
        return saveResult;
    }

    @Override
    @Transactional
    public boolean unlikeApp(Long appId, Long userId) {
        // 查询并删除点赞记录
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId)
                .eq("userId", userId);
        Long thumbId = ((Long) redisTemplate.opsForHash().get(ThumbConstant.USER_THUMB_KEY_PREFIX + userId.toString(), appId.toString()));
        boolean removeResult = this.remove(queryWrapper);

        // 如果删除成功，更新应用的点赞数
        if (removeResult) {
            updateAppThumbCount(appId, -1);
            redisTemplate.opsForHash().delete(ThumbConstant.USER_THUMB_KEY_PREFIX + userId, thumbId.toString());
        }
        return removeResult;
    }

    @Override
    public boolean isUserLikedApp(Long appId, Long userId) {
        return redisTemplate.opsForHash().hasKey(ThumbConstant.USER_THUMB_KEY_PREFIX + userId, appId.toString());
    }
    
    /**
     * 更新应用的点赞数
     *
     * @param appId 应用ID
     * @param delta 点赞数变化量（+1表示增加，-1表示减少）
     */
    private void updateAppThumbCount(Long appId, int delta) {
        try {
            // 获取应用信息
            App app = appService.getById(appId);
            if (app != null) {
                // 计算新的点赞数，确保不会小于0
                Integer currentThumbCount = app.getThumbCount();
                if (currentThumbCount == null) {
                    currentThumbCount = 0;
                }
                int newThumbCount = Math.max(0, currentThumbCount + delta);
                
                // 更新应用的点赞数
                App updateApp = new App();
                updateApp.setId(appId);
                updateApp.setThumbCount(newThumbCount);
                appService.updateById(updateApp);
            }
        } catch (Exception e) {
            // 记录错误日志，但不影响主流程
            log.error("更新应用点赞数失败，appId: {}, delta: {}", appId, delta, e);
        }
    }
}
