package com.easen.aicode.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.easen.aicode.config.MailConfig;
import com.easen.aicode.exception.BusinessException;
import com.easen.aicode.exception.ErrorCode;
import com.easen.aicode.service.AuthService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    private static final String VERIFY_CODE_PREFIX = "verify:code:"; // verify:code:{email}
    private static final String VERIFY_FAIL_COUNT_PREFIX = "verify:fail:"; // verify:fail:{email}
    private static final String SEND_COUNT_PREFIX = "verify:send:"; // verify:send:{email}:{hour}

    private static final int CODE_TTL_MINUTES = 15;
    private static final int MAX_SEND_PER_EMAIL_PER_HOUR = 3;
    private static final int MAX_VERIFY_FAIL = 3;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

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
        String nickname = user != null ? user.getUserName() : "用户";

        // 频次限制：1小时内同邮箱最多3次
        String hourKey = DateUtil.format(DateUtil.date(), "yyyyMMddHH");
        String sendCountKey = SEND_COUNT_PREFIX + email + ":" + hourKey;

        Long sendCount = redisTemplate.opsForValue().increment(sendCountKey);
        if (sendCount != null && sendCount == 1L) {
            redisTemplate.expire(sendCountKey, Duration.ofHours(1));
        }
        if (sendCount != null && sendCount > MAX_SEND_PER_EMAIL_PER_HOUR) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该邮箱接收过于频繁，请稍后再试");
        }

        // 生成6位数字验证码
        String code = RandomUtil.randomNumbers(6);
        
        // 统一使用email存储验证码
        String codeKey = VERIFY_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(codeKey, code, CODE_TTL_MINUTES, TimeUnit.MINUTES);
        
        // 清空失败计数
        String failKey = VERIFY_FAIL_COUNT_PREFIX + email;
        redisTemplate.delete(failKey);

        // 发送邮件
        sendMail(email, nickname, code, "LOGIN");
    }

    @Override
    public boolean verifyCode(Long userId, String code) {
        if (userId == null || userId <= 0 || StrUtil.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        
        // 获取用户邮箱
        User user = userService.getById(userId);
        if (user == null || StrUtil.isBlank(user.getUserAccount())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或邮箱为空");
        }
        
        String email = user.getUserAccount();
        String codeKey = VERIFY_CODE_PREFIX + email;
        Object cached = redisTemplate.opsForValue().get(codeKey);
        
        if (cached == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码不存在或已过期");
        }
        
        String cachedCode = String.valueOf(cached);
        if (!StrUtil.equals(cachedCode, code)) {
            // 失败计数
            String failKey = VERIFY_FAIL_COUNT_PREFIX + email;
            Long fail = redisTemplate.opsForValue().increment(failKey);
            if (fail != null && fail == 1L) {
                redisTemplate.expire(failKey, Duration.ofMinutes(CODE_TTL_MINUTES));
            }
            if (fail != null && fail >= MAX_VERIFY_FAIL) {
                // 失效当前验证码并自动重发
                redisTemplate.delete(codeKey);
                redisTemplate.delete(failKey);
                try {
                    sendVerifyCode(email);
                } catch (Exception e) {
                    log.warn("自动重发验证码失败 email={}, error={}", email, e.getMessage());
                }
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码错误次数过多，已重新发送，请查收邮箱");
            }
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码错误");
        }
        
        // 校验通过，清理验证码
        redisTemplate.delete(codeKey);
        redisTemplate.delete(VERIFY_FAIL_COUNT_PREFIX + email);
        return true;
    }


    private void sendMail(String email, String nickname, String code, String scene) {
        if (mailSender == null) {
            log.warn("未配置邮件发送，跳过实际发送，email={}, code={}", email, code);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
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


