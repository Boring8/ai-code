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
    private String type;

    /**
     * 信息
     */
    private String message;

    private String editAction;

    private Flux<ServerSentEvent<String>> aiMassage;

    /**
     * 用户信息
     */
    private UserVO user;
}
