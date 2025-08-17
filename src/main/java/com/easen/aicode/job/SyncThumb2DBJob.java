package com.easen.aicode.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrPool;

import com.easen.aicode.mapper.AppMapper;
import com.easen.aicode.mapper.ThumbMapper;
import com.easen.aicode.model.entity.Thumb;
import com.easen.aicode.model.enums.ThumbTypeEnum;
import com.easen.aicode.service.AppService;

import com.easen.aicode.service.ThumbService;
import com.easen.aicode.utils.RedisKeyUtil;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时将 Redis 中的临时点赞数据同步到数据库
 *
 * @author pine
 */
@Component
@Slf4j
public class SyncThumb2DBJob {

    @Resource
    private AppMapper appMapper;

    @Resource
    private ThumbService thumbService;

    @Resource
    private ThumbMapper thumbMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Scheduled(initialDelay = 10000, fixedDelay = 10000)
    @Transactional(rollbackFor = Exception.class)
    public void run() {
        log.info("开始执行");
        DateTime nowDate = DateUtil.date();
        String date = DateUtil.format(nowDate, "HH:mm:") + (DateUtil.second(nowDate) / 10 - 1) * 10;
        syncThumb2DBByDate(date);
        log.info("临时数据同步完成");
    }

    public void syncThumb2DBByDate(String date) {
        // 获取到临时点赞和取消点赞数据
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(date);
        Map<Object, Object> allTempThumbMap = redisTemplate.opsForHash().entries(tempThumbKey);
        boolean thumbMapEmpty = CollUtil.isEmpty(allTempThumbMap);
        // 同步 点赞 到数据库
        // 构建插入列表并收集appId
        Map<Long, Long> appThumbCountMap = new HashMap<>();
        if (thumbMapEmpty) {
            return;
        }
        List<Thumb> thumbList = new ArrayList<>();
        List<Thumb> thumbListDecr = new ArrayList<>();
        for (Object userIdAppIdObj : allTempThumbMap.keySet()) {
            String userIdAppId = (String) userIdAppIdObj;
            String[] userIdAndAppId = userIdAppId.split(StrPool.COLON);
            Long userId = Long.valueOf(userIdAndAppId[0]);
            Long appId = Long.valueOf(userIdAndAppId[1]);
            // -1 取消点赞，1 点赞
            Integer thumbType = Integer.valueOf(allTempThumbMap.get(userIdAppId).toString());
            if (thumbType == ThumbTypeEnum.INCR.getValue()) {
                Thumb thumb = new Thumb();
                thumb.setUserId(userId);
                thumb.setAppId(appId);
                thumbList.add(thumb);
            } else if (thumbType == ThumbTypeEnum.DECR.getValue()) {
                Thumb thumb = new Thumb();
                thumb.setUserId(userId);
                thumb.setAppId(appId);
                thumbListDecr.add(thumb);
            } else {
                if (thumbType != ThumbTypeEnum.NON.getValue()) {
                    log.warn("数据异常：{}", userId + "," + appId + "," + thumbType);
                }
                continue;
            }
            // 计算点赞增量
            appThumbCountMap.put(appId, appThumbCountMap.getOrDefault(appId, 0L) + thumbType);
        }
        // 批量插入
        thumbService.saveBatch(thumbList);
        // 批量删除
        if (!thumbListDecr.isEmpty()) {
            thumbMapper.deleteBatch(thumbListDecr);
        }
        // 批量更新博客点赞量
        if (!appThumbCountMap.isEmpty()) {
            appMapper.batchUpdateThumbCount(appThumbCountMap);
        }
        // 异步删除
        Thread.startVirtualThread(() -> {
            redisTemplate.delete(tempThumbKey);
        });
    }

}
