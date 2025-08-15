package com.easen.aicode.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史 实体类。
 *
 * @author <a>easen</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("chat_history")
public class ChatHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    /**
     * id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;


    /**
     * 消息
     */
    private String message;

    /**
     * user/ai
     */
    @Column("messageType")
    private String messageType;

    /**
     * 应用id
     */
    @Column("appId")
    private Long appId;

    /**
     * 创建用户id
     */
    @Column("userId")
    private Long userId;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;

    /**
     * 消息状态 0正常 1手动中断 2 AI异常中断
     */
    @Column("status")
    private Integer status;

    /**
     * 消息类型
     */
    @Column("type")
    private String type;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;

    /**
     * 消息类型
     */
    @Column("onlyId")
    private String onlyId;
}
