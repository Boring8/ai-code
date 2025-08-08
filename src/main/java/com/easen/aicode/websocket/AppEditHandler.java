package com.easen.aicode.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.model.enums.ChatHistoryMessageTypeEnum;
import com.easen.aicode.service.UserService;
import com.easen.aicode.websocket.disruptor.AppEventProducer;
import com.easen.aicode.websocket.model.AppMessageTypeEnum;
import com.easen.aicode.websocket.model.AppRequestMessage;
import com.easen.aicode.websocket.model.AppResponseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 处理器
 */
@Component
@Slf4j
public class AppEditHandler extends TextWebSocketHandler {
    // 保存所有连接的会话，key: pictureId, value: 用户会话集合
    private final Map<Long, Set<WebSocketSession>> appSessions = new ConcurrentHashMap<>();
    // key: appId, value: 当前正在编辑的用户 ID
    private final Map<Long, Long> appDoingUsers = new ConcurrentHashMap<>();
    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private AppEventProducer appEventProducer;

    /**
     * 连接建立成功
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        // 保存会话到集合中
        User user = (User) session.getAttributes().get("user");
        Long appId = (Long) session.getAttributes().get("appId");
        appSessions.putIfAbsent(appId, ConcurrentHashMap.newKeySet());
        appSessions.get(appId).add(session);
        AppResponseMessage appResponseMessage = new AppResponseMessage();
        appResponseMessage.setType(AppMessageTypeEnum.INFO.getValue());
        String message = String.format("用户 %s 加入编辑", user.getUserName());
        appResponseMessage.setMessage(message);
        appResponseMessage.setUser(userService.getUserVO(user));
        broadcastToApp(appId, appResponseMessage);
    }

    /**
     * 广播给该app的所有用户
     *
     * @param appId
     * @param appResponseMessage
     */
    private void broadcastToApp(Long appId, AppResponseMessage appResponseMessage) throws IOException {
        broadcastToApp(appId, appResponseMessage, null);
    }


    /**
     * 收到前端发送的消息，根据消息类别处理消息
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);

        AppRequestMessage appResponseMessage = JSONUtil.toBean(message.getPayload(), AppRequestMessage.class);
        // 从 Session 属性中获取到公共参数
        User user = (User) session.getAttributes().get("user");
        Long appId = (Long) session.getAttributes().get("appId");
        appEventProducer.publishEvent(appResponseMessage, session, user, appId);
    }


    /**
     * 关闭连接
     *
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        // 从 Session 属性中获取到公共参数
        User user = (User) session.getAttributes().get("user");
        Long appId = (Long) session.getAttributes().get("appId");
        // 移除当前用户的编辑状态
        userHandleExitEditMessage(null, session, user, appId);
        // 删除会话
        Set<WebSocketSession> sessionSet = appSessions.get(appId);
        if (sessionSet != null) {
            sessionSet.remove(session);
            if (sessionSet.isEmpty()) {
                appSessions.remove(appId);
            }
        }
        // 通知其他用户，该用户已经离开编辑
        AppResponseMessage pictureEditResponseMessage = new AppResponseMessage();
        pictureEditResponseMessage.setType(AppMessageTypeEnum.INFO.getValue());
        String message = String.format("用户 %s 离开编辑", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVO(user));
        broadcastToApp(appId, pictureEditResponseMessage);
    }

    /**
     * 退出编辑状态
     *
     * @param appRequestMessage
     * @param session
     * @param user
     * @param appId
     */
    public void userHandleExitEditMessage(AppRequestMessage appRequestMessage, WebSocketSession session, User user, Long appId) throws IOException {
        // 正在编辑的用户
        Long editingUserId = appDoingUsers.get(appId);
        // 确认是当前的编辑者
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            // 移除用户正在编辑该图片
            appDoingUsers.remove(appId);
            // 构造响应，发送退出编辑的消息通知
            AppResponseMessage pictureEditResponseMessage = new AppResponseMessage();
            pictureEditResponseMessage.setType(AppMessageTypeEnum.USER_EXIT_EDIT.getValue());
            String message = String.format("AI回答 %s 完毕", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            broadcastToApp(appId, pictureEditResponseMessage);
        }
    }


    /**
     * 进入编辑状态
     *
     * @param appRequestMessage
     * @param session
     * @param user
     * @param appId
     */
    public void userHandleEnterEditMessage(AppRequestMessage appRequestMessage, WebSocketSession session, User user, Long appId) throws IOException {
        // 没有用户正在编辑该图片，才能进入编辑
        if (!appDoingUsers.containsKey(appId)) {
            // 设置用户正在编辑该图片
            appDoingUsers.put(appId, user.getId());
            // 构造响应，发送加入编辑的消息通知
            AppResponseMessage appResponseMessage = new AppResponseMessage();
            appResponseMessage.setType(AppMessageTypeEnum.USER_ENTER_EDIT.getValue());
            String message = String.format("AI开始回答 %s ", user.getUserName());
            appResponseMessage.setMessage(message);
            appResponseMessage.setEditAction(appRequestMessage.getEditAction());
            appResponseMessage.setUser(userService.getUserVO(user));
            // 广播给所有用户
            broadcastToApp(appId, appResponseMessage);
        }
    }

    /**
     * AI生成操作
     *
     * @param appRequestMessage
     * @param session
     * @param user
     * @param appId
     */
    public void aiHandleEditActionMessage(AppRequestMessage appRequestMessage, WebSocketSession session, User user, Long appId) throws IOException {

        AppResponseMessage appResponseMessage = new AppResponseMessage();
        appResponseMessage.setType(AppMessageTypeEnum.AI_EDIT_ACTION.getValue());
        String message = String.format("AI 正在回答 %s", user.getUserName());
        appResponseMessage.setMessage(message);
        appResponseMessage.setEditAction(appRequestMessage.getEditAction());
        appResponseMessage.setUser(userService.getUserVO(user));
        // 广播给除了当前客户端之外的其他用户，否则会造成重复编辑
        broadcastToApp(appId, appResponseMessage, session);

    }

    // 提取通用的会话广播逻辑
    private void broadcastToSessions(Long appId, TextMessage message, WebSocketSession excludeSession) throws IOException {
        Set<WebSocketSession> sessionSet = appSessions.get(appId);
        if (CollUtil.isNotEmpty(sessionSet)) {
            for (WebSocketSession session : sessionSet) {
                // 排除指定会话并检查连接状态
                if (excludeSession != null && session.equals(excludeSession)) {
                    continue;
                }
                if (session.isOpen()) {
                    session.sendMessage(message); // 这里可能报错
                }
            }
        }
    }


    // 保留原有的广播方法，用于处理普通消息
    private void broadcastToApp(Long appId, AppResponseMessage appResponseMessage, WebSocketSession excludeSession) throws IOException {
        // 创建 ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        // 配置序列化：将 Long 类型转为 String，解决丢失精度问题
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance); // 支持 long 基本类型
        objectMapper.registerModule(module);
        // 序列化为 JSON 字符串
        String message = objectMapper.writeValueAsString(appResponseMessage);
        TextMessage textMessage = new TextMessage(message);

        broadcastToSessions(appId, textMessage, excludeSession);
    }
}
