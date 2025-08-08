package com.easen.aicode.websocket.model;

import lombok.Getter;

/**
 * 图片编辑消息类型枚举
 */
@Getter
public enum AppMessageTypeEnum {

    INFO("发送通知", "INFO"),
    ERROR("发送错误", "ERROR"),
    USER_ENTER_EDIT("用户进入对话状态", "USER_ENTER_EDIT"),
    USER_EXIT_EDIT("用户退出对话状态", "USER_EXIT_EDIT"),
    AI_EDIT_ACTION("ai执行对话操作", "AI_EDIT_ACTION");

    private final String text;
    private final String value;

    AppMessageTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static AppMessageTypeEnum getEnumByValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (AppMessageTypeEnum typeEnum : AppMessageTypeEnum.values()) {
            if (typeEnum.value.equals(value)) {
                return typeEnum;
            }
        }
        return null;
    }
}