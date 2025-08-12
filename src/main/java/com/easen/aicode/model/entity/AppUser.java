package com.easen.aicode.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 空间用户关联 实体类。
 *
 * @author <a>easen</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("app_user")
public class AppUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * app id
     */
    @Column("appId")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long appId;

    /**
     * 用户 id
     */
    @Column("userId")
    @JsonSerialize(using = ToStringSerializer.class)
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
     * app角色：viewer/editor/admin
     */
    @Column("appRole")
    private String appRole;

}
