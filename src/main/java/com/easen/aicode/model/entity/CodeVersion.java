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
 * 代码版本（用于保存代码面板文本的历史版本）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("code_version")
public class CodeVersion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 应用 id
     */
    @Column("appId")
    private Long appId;

    /**
     * 代码生成类型（html/multi_file/vue_project）
     */
    @Column("codeGenType")
    private String codeGenType;
    /**
     * 带稳定锚点的 canonical HTML（仅内部用于增量编辑/定位；对外展示应使用 content）
     */
    @Column("contentWithAnchor")
    private String contentWithAnchor;

    /**
     * 创建用户 id
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
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;
}

