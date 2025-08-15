package com.easen.aicode.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 空间角色枚举
 */
@Getter
public enum ChatHistoryStatusEnum {

    NORMAL("正常", 0),
    USER_INTERRUPTED("手动中断", 1),
    AI_INTERRUPTED("AI异常中断", 2);

    private final String text;

    private final Integer value;

    ChatHistoryStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的 value
     * @return 枚举值
     */
    public static ChatHistoryStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (ChatHistoryStatusEnum anEnum : ChatHistoryStatusEnum.values()) {
            if (Objects.equals(anEnum.value, value)) {
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


}