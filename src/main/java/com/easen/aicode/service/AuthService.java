package com.easen.aicode.service;

import com.easen.aicode.common.BaseResponse;

/**
 * 验证码服务
 */
public interface AuthService {

    /**
     * 发送验证码
     *
     * @param email   必填，接收邮箱（默认认为 userAccount 是邮箱）
     */
    void sendVerifyCode( String email);

    /**
     * 校验验证码
     *
     * @param userId 用户ID
     * @param code   验证码
     * @return true 校验通过
     */
    boolean verifyCode(Long userId, String code);
}


