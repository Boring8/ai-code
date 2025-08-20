package com.easen.aicode.service;

import com.easen.aicode.common.BaseResponse;

/**
 * 验证码服务。
 *
 * <p>负责发送与校验验证码，可用于登录、重置密码等场景。</p>
 */
public interface AuthService {

    /**
     * 发送验证码
     *
     * @param email 接收验证码的邮箱地址
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


