package com.easen.aicode.service;

import com.easen.aicode.model.entity.CodeVersion;
import com.mybatisflex.core.service.IService;

/**
 * 代码版本 服务层。
 */
public interface CodeVersionService extends IService<CodeVersion> {

    /**
     * 保存一条代码版本（同时写入 canonical HTML：带 data-aicode-id 锚点）。
     * 对外展示仍应使用 content 字段。
     */
    boolean addCodeVersion(Long appId, String codeGenType,  String contentWithAnchor, Long userId);

}

