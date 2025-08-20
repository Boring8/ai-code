package com.easen.aicode.utils;

import com.easen.aicode.constant.ThumbConstant;
import com.easen.aicode.mapper.ThumbMapper;
import com.easen.aicode.model.entity.Thumb;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.easen.aicode.constant.ThumbConstant.RANK_KEY;

/**
 * 点赞热点数据处理工具类
 * 用于优化热点数据的处理逻辑，减少 Redis 压力
 *
 * @author easen
 */
@Slf4j
@Component
public class ThumbHotKeyUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 检查用户是否已点赞应用，优先从热键缓存获取
     *
     * @param userId 用户ID
     * @param appId  应用ID
     * @return 是否已点赞
     */
    public boolean isUserLikedApp(Long userId, Long appId) {
        String appIdStr = appId.toString();

        String userThumbKey = ThumbConstant.USER_THUMB_KEY_PREFIX + userId;

        String userThumbHotKey = ThumbConstant.APP_THUMB_HOTKEY_PREFIX + userId + "_" + appIdStr;
        // 1) 先尝试从热键本地缓存获取
        if (JdHotKeyStore.isHotKey(userThumbHotKey)) {
            Object cached = JdHotKeyStore.get(userThumbHotKey);
            if (cached != null) {
                log.debug("热键本地缓存命中，userId: {}, appId: {}", userId, appId);
                return true;
            }
        }

        // 2) 再查 Redis
        Boolean existInRedis = redisTemplate.opsForHash().hasKey(userThumbKey, appIdStr);
        if (Boolean.TRUE.equals(existInRedis)) {
            JdHotKeyStore.smartSet(userThumbHotKey, null);
            return true;
        }

        return false;
    }

    /**
     * 保存用户点赞记录到缓存
     *
     * @param userId 用户ID
     * @param appId  应用ID
     */
    public void saveUserThumb(Long userId, Long appId) {
        String userThumbKey = ThumbConstant.USER_THUMB_KEY_PREFIX + userId;
        String appIdStr = appId.toString();
        // 先存入 Redis（使用标志位 1L）
        redisTemplate.opsForHash().put(userThumbKey, appIdStr, 1L);
        //排行榜处理
        redisTemplate.opsForZSet().incrementScore(RANK_KEY, appId, 1);
    }

    /**
     * 删除用户点赞记录从缓存
     *
     * @param userId 用户ID
     * @param appId  应用ID
     */
    public void removeUserThumb(Long userId, Long appId) {
        String userThumbKey = ThumbConstant.USER_THUMB_KEY_PREFIX + userId;
        String appIdStr = appId.toString();
        // 从 Redis 删除点赞记录
        redisTemplate.opsForHash().delete(userThumbKey, appIdStr);
        //排行榜处理
        redisTemplate.opsForZSet().incrementScore(RANK_KEY, appId, -1);
    }


}