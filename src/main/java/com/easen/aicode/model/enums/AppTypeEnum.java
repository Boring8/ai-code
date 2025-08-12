package com.easen.aicode.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.Objects;

@Getter
public enum AppTypeEnum {
    PUBLIC("公开", 1),
    PRIVATE("私有", 0);

    private final String text;

    private final Integer value;

    AppTypeEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的 value
     * @return 枚举值
     */
    public static AppTypeEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (AppTypeEnum anEnum : AppTypeEnum.values()) {
            if (Objects.equals(anEnum.getValue(), value)) {
                return anEnum;
            }
        }
        return null;
    }
}
