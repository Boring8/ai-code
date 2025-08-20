package com.easen.aicode.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 项目打包下载 服务层。
 */
public interface ProjectDownloadService {

    /**
     * 下载项目为压缩包
     *
     * @param projectPath      待打包下载的项目路径
     * @param downloadFileName 下载后的文件名（不含后缀或由实现决定）
     * @param response         HTTP 响应对象（用于写出二进制内容）
     */
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}
