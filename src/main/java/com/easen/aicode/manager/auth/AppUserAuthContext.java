package com.easen.aicode.manager.auth;

import com.easen.aicode.model.entity.App;
import com.easen.aicode.model.entity.AppUser;
import lombok.Data;

/**
 * AppUserAuthContext
 * 表示用户在特定空间内的授权上下文，包括关联的图片、空间和用户信息。
 */
@Data
public class AppUserAuthContext {

    /**
     * 临时参数，不同请求对应的 id 可能不同
     */
    private Long id;

    /**
     * app ID
     */
    private Long appId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 应用用户 ID
     */
    private Long appUserId;


    /**
     * 空间信息
     */
    private App app;

    /**
     * 空间用户信息
     */
    private AppUser appUser;
}