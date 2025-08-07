package com.easen.aicode.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用更新请求
 *
 * @author <a>easen</a>
 */
@Data
public class AppUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用封面
     */
    private String cover;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 是否为团队应用(0否，1是)
     */
    private Integer isTeam;

    private static final long serialVersionUID = 1L;
} 