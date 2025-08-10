package com.easen.aicode.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.easen.aicode.constant.AppConstant;
import com.easen.aicode.model.entity.App;
import com.easen.aicode.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 应用资源清理器
 * 负责清理应用相关的所有资源，包括代码输出目录、部署目录等
 */
@Slf4j
@Component
public class AppResourceCleaner {

    /**
     * 清理应用的所有相关资源
     *
     * @param app 应用实体对象
     */
    public void cleanupAppResources(App app) {
        if (app == null) {
            log.warn("应用对象为空，跳过资源清理");
            return;
        }

        Long appId = app.getId();
        String codeGenType = app.getCodeGenType();
        String deployKey = app.getDeployKey();

        log.info("开始清理应用 {} 的相关资源", appId);

        // 1. 清理代码输出目录
        cleanupCodeOutputDirectory(appId, codeGenType);

        // 2. 清理部署目录
        if (StrUtil.isNotBlank(deployKey)) {
            cleanupDeployDirectory(deployKey);
        }

        log.info("应用 {} 的资源清理完成", appId);
    }

    /**
     * 清理代码输出目录
     *
     * @param appId       应用ID
     * @param codeGenType 代码生成类型
     */
    private void cleanupCodeOutputDirectory(Long appId, String codeGenType) {
        if (appId == null || StrUtil.isBlank(codeGenType)) {
            log.warn("应用ID或代码生成类型为空，跳过代码输出目录清理");
            return;
        }

        // 构建代码输出目录路径：{codeGenType}_{appId}
        String codeOutputDirName = StrUtil.format("{}_{}", codeGenType, appId);
        String codeOutputPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + codeOutputDirName;

        try {
            File codeOutputDir = new File(codeOutputPath);
            if (codeOutputDir.exists()) {
                FileUtil.del(codeOutputDir);
                log.info("成功清理代码输出目录：{}", codeOutputPath);
            } else {
                log.info("代码输出目录不存在，无需清理：{}", codeOutputPath);
            }
        } catch (Exception e) {
            log.error("清理代码输出目录失败：{}，错误：{}", codeOutputPath, e.getMessage(), e);
        }
    }

    /**
     * 清理部署目录
     *
     * @param deployKey 部署标识
     */
    private void cleanupDeployDirectory(String deployKey) {
        if (StrUtil.isBlank(deployKey)) {
            log.warn("部署标识为空，跳过部署目录清理");
            return;
        }

        // 构建部署目录路径：{deployKey}
        String deployPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;

        try {
            File deployDir = new File(deployPath);
            if (deployDir.exists()) {
                FileUtil.del(deployDir);
                log.info("成功清理部署目录：{}", deployPath);
            } else {
                log.info("部署目录不存在，无需清理：{}", deployPath);
            }
        } catch (Exception e) {
            log.error("清理部署目录失败：{}，错误：{}", deployPath, e.getMessage(), e);
        }
    }

    /**
     * 异步清理应用资源
     *
     * @param app 应用实体对象
     */
    public void cleanupAppResourcesAsync(App app) {
        Thread.ofVirtual().name("app-cleanup-" + (app != null ? app.getId() : "unknown"))
                .start(() -> {
                    try {
                        cleanupAppResources(app);
                    } catch (Exception e) {
                        log.error("异步清理应用资源时发生异常：{}", e.getMessage(), e);
                    }
                });
    }
}
