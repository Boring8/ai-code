package com.easen.aicode.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *  实体类。
 *
 * @author <a>easen</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("thumb")
public class Thumb implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column("userId")
    private Long userId;

    @Column("appId")
    private Long appId;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

}
