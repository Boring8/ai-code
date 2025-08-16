package com.easen.aicode.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 应用点赞信息视图类
 *
 * @author <a>easen</a>
 */
@Data
public class AppThumbVO implements Serializable {

    /**
     * 应用id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 点赞数
     */
    private Long thumbCount;
}
