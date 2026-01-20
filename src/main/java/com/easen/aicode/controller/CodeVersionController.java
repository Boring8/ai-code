package com.easen.aicode.controller;

import com.easen.aicode.common.BaseResponse;
import com.easen.aicode.common.ResultUtils;
import com.easen.aicode.manager.auth.annotation.SaSpaceCheckPermission;
import com.easen.aicode.manager.auth.model.AppUserPermissionConstant;
import com.easen.aicode.service.CodeVersionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 代码版本接口（代码面板文本版本）
 */
@RestController
@RequestMapping("/app/codeText")
public class CodeVersionController {

    @Resource
    private CodeVersionService codeVersionService;

    /**
     * 查询最新的代码面板文本（无记录返回空字符串）
     */
    @GetMapping("/latest")
    @SaSpaceCheckPermission(value = AppUserPermissionConstant.APP_VIEW)
    public BaseResponse<String> getLatest(@RequestParam Long appId) {
        String content = codeVersionService.getLatestContent(appId);
        return ResultUtils.success(content);
    }
}

