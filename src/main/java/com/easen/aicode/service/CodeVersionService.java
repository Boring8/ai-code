package com.easen.aicode.service;

import com.easen.aicode.model.entity.CodeVersion;
import com.mybatisflex.core.service.IService;

/**
 * 代码版本 服务层。
 */
public interface CodeVersionService extends IService<CodeVersion> {

    /**
     * 保存一条代码版本（代码面板文本）。
     */
    boolean addCodeVersion(Long appId, String codeGenType, String content, Long userId);

}

