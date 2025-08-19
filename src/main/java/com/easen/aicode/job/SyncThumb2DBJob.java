package com.easen.aicode.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrPool;

import cn.hutool.core.util.ObjUtil;
import com.easen.aicode.constant.ThumbConstant;
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

import java.util.*;

import static com.easen.aicode.constant.ThumbConstant.TEMP_THUMB_KEY_PREFIX;

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

//    @Scheduled(initialDelay = 10000, fixedDelay = 10000)
//    @Transactional(rollbackFor = Exception.class)
//    public void run() {
//        log.info("开始补偿数据");
//        Set<String> thumbKeys = redisTemplate.keys(RedisKeyUtil.getTempThumbKey("") + "*");
//        Set<String> needHandleDataSet = new HashSet<>();
//        thumbKeys.stream().filter(ObjUtil::isNotNull).forEach(thumbKey -> needHandleDataSet.add(thumbKey.replace(ThumbConstant.TEMP_THUMB_KEY_PREFIX.formatted(""), "")));
//
//        if (CollUtil.isEmpty(needHandleDataSet)) {
//            log.info("没有需要补偿的临时数据");
//            return;
//        }
//        // 补偿数据
//        for (String date : needHandleDataSet) {
//            syncThumb2DBByDate(date);
//        }
//        log.info("临时数据补偿完成");
//    }

    public void syncThumb2DBByDate(String date) {
        // 获取到临时点赞和取消点赞数据
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(date);
        Map<Object, Object> allTempThumbMap = redisTemplate.opsForHash().entries(tempThumbKey);
        if (CollUtil.isEmpty(allTempThumbMap)) {
            return;
        }

        // 构造候选新增/删除列表
        List<Thumb> insertCandidates = new ArrayList<>();
        List<Thumb> deleteCandidates = new ArrayList<>();
        for (Object userIdAppIdObj : allTempThumbMap.keySet()) {
            String userIdAppId = String.valueOf(userIdAppIdObj);
            String[] userIdAndAppId = userIdAppId.split(StrPool.COLON);
            Long userId = Long.valueOf(userIdAndAppId[0]);
            Long appId = Long.valueOf(userIdAndAppId[1]);

            // -1 取消点赞，1 点赞（兼容类型：Integer、Long、String）
            Object val = allTempThumbMap.get(userIdAppId);
            int thumbType = Integer.parseInt(String.valueOf(val));

            if (thumbType == ThumbTypeEnum.INCR.getValue()) {
                Thumb thumb = new Thumb();
                thumb.setUserId(userId);
                thumb.setAppId(appId);
                insertCandidates.add(thumb);
            } else if (thumbType == ThumbTypeEnum.DECR.getValue()) {
                Thumb thumb = new Thumb();
                thumb.setUserId(userId);
                thumb.setAppId(appId);
                deleteCandidates.add(thumb);
            } else if (thumbType != ThumbTypeEnum.NON.getValue()) {
                log.warn("数据异常：{}, type={}", userIdAppId, thumbType);
            }
        }

        // 过滤：仅对数据库中不存在的记录执行插入，避免重复插入
        List<Thumb> existForInsert = new ArrayList<>();
        if (CollUtil.isNotEmpty(insertCandidates)) {
            existForInsert = thumbMapper.selectExistList(insertCandidates);
        }
        if (CollUtil.isNotEmpty(existForInsert)) {
            // 构建已存在 (userId, appId) 的键集合
            java.util.Set<String> existKeySet = new java.util.HashSet<>();
            for (Thumb t : existForInsert) {
                existKeySet.add(t.getUserId() + ":" + t.getAppId());
            }
            // 仅保留数据库中尚不存在的记录
            List<Thumb> filtered = new ArrayList<>();
            for (Thumb t : insertCandidates) {
                String k = t.getUserId() + ":" + t.getAppId();
                if (!existKeySet.contains(k)) {
                    filtered.add(t);
                }
            }
            insertCandidates = filtered;
        }

        // 过滤：仅对数据库中已存在的记录执行删除，保证计数准确
        List<Thumb> existForDelete = new ArrayList<>();
        if (CollUtil.isNotEmpty(deleteCandidates)) {
            existForDelete = thumbMapper.selectExistList(deleteCandidates);
        }
        deleteCandidates = existForDelete;

        // 汇总增量（基于“实际将要执行”的插入/删除）
        Map<Long, Long> appThumbCountDelta = new HashMap<>();
        for (Thumb t : insertCandidates) {
            appThumbCountDelta.put(t.getAppId(), appThumbCountDelta.getOrDefault(t.getAppId(), 0L) + 1);
        }
        for (Thumb t : deleteCandidates) {
            appThumbCountDelta.put(t.getAppId(), appThumbCountDelta.getOrDefault(t.getAppId(), 0L) - 1);
        }

        // 先批量插入，再批量删除（顺序保证幂等 + 业务直觉）
        if (CollUtil.isNotEmpty(insertCandidates)) {
            thumbService.saveBatch(insertCandidates);
        }
        if (CollUtil.isNotEmpty(deleteCandidates)) {
            thumbMapper.deleteBatch(deleteCandidates);
        }

        // 批量更新应用的点赞量（仅当有增量时）
        if (CollUtil.isNotEmpty(appThumbCountDelta)) {
            appMapper.batchUpdateThumbCount(appThumbCountDelta);
        }
        Thread.startVirtualThread(() -> redisTemplate.delete(tempThumbKey));
    }

}
