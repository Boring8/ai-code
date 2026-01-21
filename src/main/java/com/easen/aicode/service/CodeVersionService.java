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
    boolean addCodeVersion(Long appId, String codeGenType, String content, String contentWithAnchor, Long userId);

    /**
     * 查询最新一条代码面板文本（无记录返回空字符串）。
     */
    String getLatestContent(Long appId);

    /**
     * 查询最新一条 canonical HTML（带 data-aicode-id 锚点；无记录返回空字符串）。
     * 仅内部用于增量编辑/自动定位，不建议直接暴露给前端。
     */
    String getLatestAnchoredContent(Long appId);
}

