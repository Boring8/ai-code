package com.easen.aicode.websocket.model;

import com.easen.aicode.model.vo.UserVO;
import lombok.Data;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

@Data
public class AppResponseMessage {
    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 格式
     */
    private String type;
    /**
     * 动作
     */
    private String editAction;

    /**
     * 流式内容类型（用于 AI_EDIT_ACTION 区分说明/代码）
     */
    private String streamType;

    /**
     * 用户信息
     */
    private UserVO user;
}
