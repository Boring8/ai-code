package com.easen.aicode.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史 视图类。
 *
 * @author <a>easen</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 消息
     */
    private String message;

    /**
     * user/ai
     */
    private String messageType;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 创建用户userName
     */
    private String userName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 消息状态 0正常 1手动中断 2 AI异常中断
     */
    private Integer status;

    /**
     * 消息类型
     */
    private String type;
}
