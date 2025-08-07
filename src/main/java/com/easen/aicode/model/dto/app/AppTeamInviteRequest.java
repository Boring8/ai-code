package com.easen.aicode.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用团队邀请请求
 *
 * @author <a>easen</a>
 */
@Data
public class AppTeamInviteRequest implements Serializable {

    /**
     * 应用ID
     */
    private Long appId;

    /**
     * 用户ID
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
