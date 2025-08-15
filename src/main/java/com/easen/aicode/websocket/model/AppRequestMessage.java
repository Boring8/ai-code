package com.easen.aicode.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * 图片编辑请求消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppRequestMessage {

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 执行的编辑动作
     */
    private String editAction;

    /**
     * 格式
     */
    private String type;
}