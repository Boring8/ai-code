package com.easen.aicode.websocket;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;

import com.easen.aicode.model.entity.App;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.model.enums.UserRoleEnum;
import com.easen.aicode.service.AppService;
import com.easen.aicode.service.AppUserService;
import com.easen.aicode.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;


import java.util.List;
import java.util.Map;

/**
 * WebSocket 拦截器，建立连接前要先校验
 */
@Slf4j
@Component
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;

    @Resource
    private AppUserService appUserService;


    /**
     * 建立连接前要先校验
     *
     * @param request    ServerHttpRequest
     * @param response   ServerHttpResponse
     * @param wsHandler  WebSocketHandler
     * @param attributes 给 WebSocketSession 会话设置属性
     * @return 是否允许建立连接
     * @throws Exception 异常
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (!(request instanceof ServletServerHttpRequest)) {
            log.error("WebSocket握手失败：不支持的请求类型");
            return false;
        }
        HttpServletRequest httpServletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        try {
            // 1. 参数校验
            String appIdStr = httpServletRequest.getParameter("appId");
            if (StrUtil.isBlank(appIdStr)) {
                log.error("WebSocket握手失败：缺少appId参数");
                return false;
            }
            Long appId;
            try {
                appId = Long.valueOf(appIdStr);
            } catch (NumberFormatException e) {
                log.error("WebSocket握手失败：appId格式错误，appId: {}", appIdStr);
                return false;
            }
            // 2. 用户登录状态校验
            User loginUser = userService.getLoginUser(httpServletRequest);
            if (ObjUtil.isEmpty(loginUser)) {
                log.error("WebSocket握手失败：用户未登录");
                return false;
            }
            // 3. 应用存在性校验
            App app = appService.getById(appId);
            if (app == null) {
                log.error("WebSocket握手失败：应用不存在，appId: {}, userId: {}", appId, loginUser.getId());
                return false;
            }
            // 4. 应用访问权限校验
            if (!appUserService.hasAppPermission(appId, loginUser.getId())) {
                log.error("WebSocket握手失败：用户无权限访问该应用，appId: {}, userId: {}",
                        appId, loginUser.getId());
                return false;
            }
            // 5. 设置用户登录信息等属性到 WebSocket 会话中
            attributes.put("user", loginUser);
            attributes.put("userId", loginUser.getId());
            attributes.put("appId", appId);
            log.info("WebSocket握手成功：用户 {} 连接到应用 {}", loginUser.getId(), appId);
            return true;
        } catch (Exception e) {
            log.error("WebSocket握手异常：{}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }
}
