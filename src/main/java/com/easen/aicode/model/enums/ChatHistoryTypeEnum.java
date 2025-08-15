package com.easen.aicode.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 空间角色枚举
 */
@Getter
public enum ChatHistoryTypeEnum {

    TEXT("文本", "text"),
    IMAGE("图片", "image");

    private final String text;

    private final String value;

    ChatHistoryTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的 value
     * @return 枚举值
     */
    public static ChatHistoryTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (ChatHistoryTypeEnum anEnum : ChatHistoryTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    /**
     * 获取所有枚举的文本列表
     *
     * @return 文本列表
     */
    public static List<String> getAllTexts() {
        return Arrays.stream(ChatHistoryStatusEnum.values())
                .map(ChatHistoryStatusEnum::getText)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有枚举的值列表
     *
     * @return 值列表
     */
    public static List<String> getAllValues() {
        return Arrays.stream(ChatHistoryTypeEnum.values())
                .map(ChatHistoryTypeEnum::getValue)
                .collect(Collectors.toList());
    }
}