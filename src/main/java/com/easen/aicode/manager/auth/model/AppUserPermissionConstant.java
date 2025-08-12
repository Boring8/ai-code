package com.easen.aicode.manager.auth.model;

import lombok.Data;

/**
 * 空间成员权限常量
 */
public interface AppUserPermissionConstant {

    /**
     * app用户管理权限
     */
    String APP_USER_MANAGE = "appUser:manage";

    /**
     * app查看权限
     */
    String APP_VIEW = "app:view";

    /**
     * app上传权限
     */
    String APP_UPLOAD = "app:upload";

    /**
     * app生成权限
     */
    String APP_EDIT = "app:edit";

    /**
     * app编辑权限
     */
    String APP_UPDATE = "app:update";

    /**
     * app删除权限
     */
    String APP_DELETE = "app:delete";

    /**
     * app部署权限
     */
    String APP_DEPLOY = "app:deploy";

}