package com.easen.aicode.constant;

/**
 * @author pine
 */
public interface ThumbConstant {

    /**
     * 用户点赞 hash key
     */
    String USER_THUMB_KEY_PREFIX = "thumb:";

    /**
     * 临时 点赞记录 key
     */
    String TEMP_THUMB_KEY_PREFIX = "thumb:temp:%s";

    /**
     * 排行榜
     */
    String RANK_KEY = "app:score:rank";

    /**
     * 用户点赞 hotkey key
     */
    String APP_THUMB_HOTKEY_PREFIX = "app_thumb_";

    /**
     * 用户点赞 hotkey key
     */
    String APP_ID_HOTKEY_PREFIX = "app_id_";
}
