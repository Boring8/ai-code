package com.easen.aicode.service;

/**
 * 截图服务
 */
public interface ScreenshotService {


    /**
     * 通用的截图服务，可以得到访问地址
     *
     * @param webUrl 网址
     * @return 截图上传后的可访问 URL（如对象存储链接）
     */
    String generateAndUploadScreenshot(String webUrl);

}
