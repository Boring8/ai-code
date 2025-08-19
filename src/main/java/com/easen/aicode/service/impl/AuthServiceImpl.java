package com.easen.aicode.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.easen.aicode.exception.BusinessException;
import com.easen.aicode.exception.ErrorCode;
import com.easen.aicode.service.AuthService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.service.UserService;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final String VERIFY_CODE_KEY_PREFIX = "verify:code:"; // verify:code:{userId}
    private static final String VERIFY_CODE_EMAIL_PREFIX = "verify:code:email:"; // verify:code:email:{email}
    private static final String VERIFY_CODE_FAIL_COUNT_PREFIX = "verify:fail:"; // verify:fail:{userId}
    private static final String VERIFY_CODE_FAIL_EMAIL_PREFIX = "verify:fail:email:"; // verify:fail:email:{email}
    private static final String SEND_COUNT_USER_PREFIX = "verify:send:user:"; // verify:send:user:{userId}:{hour}
    private static final String SEND_COUNT_EMAIL_PREFIX = "verify:send:email:"; // verify:send:email:{email}:{hour}

    private static final int CODE_TTL_MINUTES = 15;
    private static final int MAX_SEND_PER_USER_PER_HOUR = 5;
    private static final int MAX_SEND_PER_EMAIL_PER_HOUR = 3;
    private static final int MAX_VERIFY_FAIL = 3;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private JavaMailSender mailSender;

    @Resource
    @Lazy
    private UserService userService;

    @Override
    public void sendVerifyCode(String email) {
        if (StrUtil.isBlank(email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        }

        // 根据邮箱查找用户
        User user = userService.getUserByUserAccount(email);
        boolean isRegisteredUser = user != null;
        
        Long userId = null;
        String nickname = "用户";
        
        if (isRegisteredUser) {
            userId = user.getId();
            nickname = user.getUserName();
        }

        // 频次限制：1小时内同邮箱最多3次
        String hourKey = DateUtil.format(DateUtil.date(), "yyyyMMddHH");
        String emailSendKey = SEND_COUNT_EMAIL_PREFIX + email + ":" + hourKey;

        Long emailCount = redisTemplate.opsForValue().increment(emailSendKey);
        if (emailCount != null && emailCount == 1L) {
            redisTemplate.expire(emailSendKey, Duration.ofHours(1));
        }
        if (emailCount != null && emailCount > MAX_SEND_PER_EMAIL_PER_HOUR) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该邮箱接收过于频繁，请稍后再试");
        }

        // 如果是已注册用户，还需要检查用户级别的频次限制
        if (isRegisteredUser) {
            String userSendKey = SEND_COUNT_USER_PREFIX + userId + ":" + hourKey;
            Long userCount = redisTemplate.opsForValue().increment(userSendKey);
            if (userCount != null && userCount == 1L) {
                redisTemplate.expire(userSendKey, Duration.ofHours(1));
            }
            if (userCount != null && userCount > MAX_SEND_PER_USER_PER_HOUR) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "发送过于频繁，请稍后再试");
            }
        }

        // 生成6位数字验证码
        String code = RandomUtil.randomNumbers(6);
        
        if (isRegisteredUser) {
            // 已注册用户：使用userId存储验证码
            String codeKey = VERIFY_CODE_KEY_PREFIX + userId;
            redisTemplate.opsForValue().set(codeKey, code, CODE_TTL_MINUTES, TimeUnit.MINUTES);
            // 清空失败计数
            String failKey = VERIFY_CODE_FAIL_COUNT_PREFIX + userId;
            redisTemplate.delete(failKey);
        } else {
            // 未注册用户：使用email存储验证码
            String codeKey = VERIFY_CODE_EMAIL_PREFIX + email;
            redisTemplate.opsForValue().set(codeKey, code, CODE_TTL_MINUTES, TimeUnit.MINUTES);
            // 清空失败计数
            String failKey = VERIFY_CODE_FAIL_EMAIL_PREFIX + email;
            redisTemplate.delete(failKey);
        }

        // 发送邮件
        sendMail(email, nickname, code, "LOGIN");
    }

    @Override
    public boolean verifyCode(Long userId, String code) {
        if (userId == null || userId <= 0 || StrUtil.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        String codeKey = VERIFY_CODE_KEY_PREFIX + userId;
        Object cached = redisTemplate.opsForValue().get(codeKey);
        if (cached == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码不存在或已过期");
        }
        String cachedCode = String.valueOf(cached);
        if (!StrUtil.equals(cachedCode, code)) {
            // 失败计数
            String failKey = VERIFY_CODE_FAIL_COUNT_PREFIX + userId;
            Long fail = redisTemplate.opsForValue().increment(failKey);
            if (fail != null && fail == 1L) {
                redisTemplate.expire(failKey, Duration.ofMinutes(CODE_TTL_MINUTES));
            }
            if (fail != null && fail >= MAX_VERIFY_FAIL) {
                // 失效当前验证码并自动重发
                redisTemplate.delete(codeKey);
                redisTemplate.delete(failKey);
                try {
                    User user = userService.getById(userId);
                    if (user != null) {
                        String email = user.getUserAccount();
                        sendVerifyCode(email);
                    }
                } catch (Exception e) {
                    log.warn("自动重发验证码失败 userId={}, error={}", userId, e.getMessage());
                }
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码错误次数过多，已重新发送，请查收邮箱");
            }
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码错误");
        }
        // 校验通过，清理验证码
        redisTemplate.delete(codeKey);
        redisTemplate.delete(VERIFY_CODE_FAIL_COUNT_PREFIX + userId);
        return true;
    }


    private void sendMail(String email, String nickname, String code, String scene) {
        if (mailSender == null) {
            log.warn("未配置邮件发送，跳过实际发送，email={}, code={}", email, code);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("【安全验证】登录验证码");
        String nick = StrUtil.blankToDefault(nickname, "用户");
        String content = StrUtil.format(
                "您好，{}：\n\n您的登录验证码为：{}。\n验证码有效期：{}分钟。请勿泄露给他人。\n（若非本人操作，请忽略本邮件）\n\n触发场景：{}",
                nick, code, CODE_TTL_MINUTES, StrUtil.blankToDefault(scene, "LOGIN")
        );
        message.setText(content);
        try {
            mailSender.send(message);
            log.info("验证码邮件发送成功，email={}, scene={}", email, scene);
        } catch (Exception ex) {
            log.error("验证码邮件发送失败，email={}, error={}", email, ex.getMessage(), ex);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码发送失败，请检查邮箱是否正确或稍后再试");
        }
    }
}


