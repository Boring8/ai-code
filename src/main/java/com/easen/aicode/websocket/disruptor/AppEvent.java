package com.easen.aicode.websocket.disruptor;

import com.easen.aicode.model.entity.User;
import com.easen.aicode.websocket.model.AppRequestMessage;

import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

/**
 * app编辑事件
 */
@Data
public class AppEvent {

    /**
     * 消息
     */
    private AppRequestMessage appRequestMessage;

    /**
     * 当前用户的 session
     */
    private WebSocketSession session;

    /**
     * 当前用户
     */
    private User user;

    /**
     * app id
     */
    private Long appId;

}