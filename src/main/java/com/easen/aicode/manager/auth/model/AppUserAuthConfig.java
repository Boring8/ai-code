package com.easen.aicode.manager.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * app成员权限配置
 */
@Data
public class AppUserAuthConfig implements Serializable {

    /**
     * 权限列表
     */
    private List<AppUserPermission> permissions;

    /**
     * 角色列表
     */
    private List<AppUserRole> roles;

    private static final long serialVersionUID = 1L;
}