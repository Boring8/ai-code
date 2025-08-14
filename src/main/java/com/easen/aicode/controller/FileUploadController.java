package com.easen.aicode.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.core.util.StrUtil;
import com.easen.aicode.common.BaseResponse;
import com.easen.aicode.common.ResultUtils;
import com.easen.aicode.constant.UserConstant;
import com.easen.aicode.exception.BusinessException;
import com.easen.aicode.exception.ErrorCode;
import com.easen.aicode.exception.ThrowUtils;
import com.easen.aicode.manager.CosManager;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传控制器
 *
 * @author <a>easen</a>
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileUploadController {

    @Resource
    private CosManager cosManager;

    @Resource
    private UserService userService;

    /**
     * 支持的图片格式
     */
    private static final String[] SUPPORTED_IMAGE_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    /**
     * 最大文件大小（5MB）
     */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * 上传图片
     *
     * @param file    图片文件
     * @param request HTTP请求
     * @return 上传结果
     */
    @PostMapping("/upload/image")
    @SaCheckLogin
    public BaseResponse<String> uploadImage(@RequestParam("file") MultipartFile file,
                                           HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(file == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        ThrowUtils.throwIf(file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件内容不能为空");
        
        // 文件大小校验
        ThrowUtils.throwIf(file.getSize() > MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, 
                "文件大小不能超过5MB");
        
        // 文件类型校验
        String contentType = file.getContentType();
        ThrowUtils.throwIf(!isValidImageType(contentType), ErrorCode.PARAMS_ERROR, 
                "不支持的文件类型，仅支持JPG、PNG、GIF、WEBP格式");
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String fileName = generateUniqueFileName(fileExtension);
            
            // 生成COS对象键
            String cosKey = generateImageKey(fileName);
            
            // 创建临时文件
            File tempFile = File.createTempFile("upload_", fileExtension);
            file.transferTo(tempFile);
            
            try {
                // 上传到COS
                String imageUrl = cosManager.uploadFile(cosKey, tempFile);
                ThrowUtils.throwIf(StrUtil.isBlank(imageUrl), ErrorCode.OPERATION_ERROR, 
                        "图片上传失败");
                
                log.info("图片上传成功，用户ID：{}，文件名：{}，URL：{}", 
                        loginUser.getId(), originalFilename, imageUrl);
                
                return ResultUtils.success(imageUrl);
            } finally {
                // 清理临时文件
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
        } catch (Exception e) {
            log.error("图片上传失败，用户ID：{}，文件名：{}", 
                    loginUser.getId(), file.getOriginalFilename(), e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片上传失败：" + e.getMessage());
        }
    }

    /**
     * 批量上传图片
     *
     * @param files   图片文件数组
     * @param request HTTP请求
     * @return 上传结果
     */
    @PostMapping("/upload/images")
    @SaCheckLogin
    public BaseResponse<String[]> uploadImages(@RequestParam("files") MultipartFile[] files,
                                              HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(files == null || files.length == 0, ErrorCode.PARAMS_ERROR, 
                "文件不能为空");
        ThrowUtils.throwIf(files.length > 10, ErrorCode.PARAMS_ERROR, 
                "一次最多只能上传10张图片");
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        String[] imageUrls = new String[files.length];
        
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            
            // 文件校验
            ThrowUtils.throwIf(file.isEmpty(), ErrorCode.PARAMS_ERROR, 
                    "第" + (i + 1) + "个文件内容不能为空");
            ThrowUtils.throwIf(file.getSize() > MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, 
                    "第" + (i + 1) + "个文件大小不能超过5MB");
            
            String contentType = file.getContentType();
            ThrowUtils.throwIf(!isValidImageType(contentType), ErrorCode.PARAMS_ERROR, 
                    "第" + (i + 1) + "个文件类型不支持，仅支持JPG、PNG、GIF、WEBP格式");
            
            try {
                // 生成唯一文件名
                String originalFilename = file.getOriginalFilename();
                String fileExtension = getFileExtension(originalFilename);
                String fileName = generateUniqueFileName(fileExtension);
                
                // 生成COS对象键
                String cosKey = generateImageKey(fileName);
                
                // 创建临时文件
                File tempFile = File.createTempFile("upload_", fileExtension);
                file.transferTo(tempFile);
                
                try {
                    // 上传到COS
                    String imageUrl = cosManager.uploadFile(cosKey, tempFile);
                    ThrowUtils.throwIf(StrUtil.isBlank(imageUrl), ErrorCode.OPERATION_ERROR, 
                            "第" + (i + 1) + "个图片上传失败");
                    
                    imageUrls[i] = imageUrl;
                } finally {
                    // 清理临时文件
                    if (tempFile.exists()) {
                        tempFile.delete();
                    }
                }
            } catch (Exception e) {
                log.error("批量图片上传失败，用户ID：{}，第{}个文件：{}", 
                        loginUser.getId(), i + 1, file.getOriginalFilename(), e);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, 
                        "第" + (i + 1) + "个图片上传失败：" + e.getMessage());
            }
        }
        
        log.info("批量图片上传成功，用户ID：{}，文件数量：{}", loginUser.getId(), files.length);
        return ResultUtils.success(imageUrls);
    }

    /**
     * 删除图片（管理员权限）
     *
     * @param imageUrl 图片URL
     * @return 删除结果
     */
    @DeleteMapping("/delete/image")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteImage(@RequestParam String imageUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(imageUrl), ErrorCode.PARAMS_ERROR, "图片URL不能为空");
        
        try {
            // 从URL中提取COS对象键
            String cosKey = cosManager.extractKeyFromUrl(imageUrl);
            ThrowUtils.throwIf(StrUtil.isBlank(cosKey), ErrorCode.PARAMS_ERROR, "无效的图片URL");
            
            // 删除COS中的文件
            boolean success = cosManager.deleteFile(cosKey);
            ThrowUtils.throwIf(!success, ErrorCode.OPERATION_ERROR, "删除图片失败");
            
            log.info("管理员删除图片成功：{}", imageUrl);
            return ResultUtils.success(true);
        } catch (Exception e) {
            log.error("删除图片失败：{}", imageUrl, e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除图片失败：" + e.getMessage());
        }
    }

    /**
     * 校验图片类型是否有效
     *
     * @param contentType 文件类型
     * @return 是否有效
     */
    private boolean isValidImageType(String contentType) {
        if (StrUtil.isBlank(contentType)) {
            return false;
        }
        for (String supportedType : SUPPORTED_IMAGE_TYPES) {
            if (supportedType.equals(contentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 文件扩展名
     */
    private String getFileExtension(String filename) {
        if (StrUtil.isBlank(filename)) {
            return ".jpg";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex);
        }
        return ".jpg";
    }

    /**
     * 生成唯一文件名
     *
     * @param extension 文件扩展名
     * @return 唯一文件名
     */
    private String generateUniqueFileName(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }

    /**
     * 生成图片的COS对象键
     * 格式：/images/2025/01/31/filename.jpg
     *
     * @param fileName 文件名
     * @return COS对象键
     */
    private String generateImageKey(String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/images/%s/%s", datePath, fileName);
    }
}
