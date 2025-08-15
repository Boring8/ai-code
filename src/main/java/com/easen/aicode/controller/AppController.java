package com.easen.aicode.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.easen.aicode.common.BaseResponse;
import com.easen.aicode.common.DeleteRequest;
import com.easen.aicode.common.ResultUtils;
import com.easen.aicode.constant.AppConstant;
import com.easen.aicode.constant.UserConstant;
import com.easen.aicode.core.AiCodeGeneratorFacade;
import com.easen.aicode.exception.ErrorCode;
import com.easen.aicode.exception.ThrowUtils;
import com.easen.aicode.manager.auth.AppUserAuthManager;
import com.easen.aicode.manager.auth.annotation.SaSpaceCheckPermission;
import com.easen.aicode.manager.auth.model.AppUserPermissionConstant;
import com.easen.aicode.model.dto.app.AppAddRequest;
import com.easen.aicode.model.dto.app.AppDeployRequest;
import com.easen.aicode.model.dto.app.AppQueryRequest;
import com.easen.aicode.model.dto.app.AppUpdateRequest;
import com.easen.aicode.model.entity.App;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.model.enums.AppRoleEnum;
import com.easen.aicode.model.enums.AppTypeEnum;
import com.easen.aicode.model.enums.UserRoleEnum;
import com.easen.aicode.model.vo.AppVO;
import com.easen.aicode.service.AppService;
import com.easen.aicode.service.ProjectDownloadService;
import com.easen.aicode.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 应用 控制层。
 *
 * @author <a>easen</a>
 */
@Slf4j
@RestController
@RequestMapping("/app")
public class AppController {

    @Autowired
    private AppService appService;

    @Autowired
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    /**
     * 应用聊天生成代码（流式 SSE）
     *
     * @param appId   应用 ID
     * @param message 用户消息
     * @param request 请求对象
     * @return 生成结果流
     */
    @GetMapping(value = "/chat/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @SaSpaceCheckPermission(value = AppUserPermissionConstant.APP_EDIT)
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
                                                       @RequestParam String message,
                                                       @RequestParam(required = false) List<String> image,
                                                       HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        //如果是会员调用工作流生成代码
        Flux<String> contentFlux = appService.chatToGenCode(appId, message, loginUser, image);
        // 转换为 ServerSentEvent 格式
        return contentFlux
                .map(chunk -> {
                    // 将内容包装成JSON对象
                    Map<String, String> wrapper = Map.of("d", chunk);
                    String jsonData = JSONUtil.toJsonStr(wrapper);
                    return ServerSentEvent.<String>builder()
                            .data(jsonData)
                            .build();
                })
                .concatWith(Mono.just(
                        // 发送结束事件
                        ServerSentEvent.<String>builder()
                                .event("done")
                                .data("")
                                .build()
                ));
    }

    /**
     * 取消正在进行的代码生成
     *
     * @param appId   应用 ID
     * @param request 请求对象
     * @return 取消结果
     */
    @PostMapping("/chat/cancel")
    public BaseResponse<Boolean> cancelCodeGeneration(@RequestParam Long appId,
                                                      HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 验证用户权限：只有正在生成代码的用户才能取消
        Long taskUserId = aiCodeGeneratorFacade.getGenerationTaskManager().getTaskUserId(appId);

        ThrowUtils.throwIf(taskUserId == null || !taskUserId.equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "只有正在生成代码的用户才能取消");

        // 执行取消操作
        boolean success = aiCodeGeneratorFacade.cancelGeneration(appId, loginUser.getId());

        return ResultUtils.success(success);
    }

    /**
     * 应用部署
     *
     * @param appDeployRequest 部署请求
     * @param request          请求
     * @return 部署 URL
     */
    @PostMapping("/deploy")
    @SaSpaceCheckPermission(value = AppUserPermissionConstant.APP_DEPLOY)
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = Long.getLong(appDeployRequest.getAppId());
        ThrowUtils.throwIf( appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务部署应用
        String deployUrl = appService.deployApp(appId, loginUser);
        return ResultUtils.success(deployUrl);
    }

    /**
     * 创建应用
     *
     * @param appAddRequest 应用添加请求
     * @param request       HTTP请求
     * @return 创建结果
     */
    @PostMapping("/add")
    public BaseResponse<String> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        String appId = appService.addApp(appAddRequest, request);
        return ResultUtils.success(appId);
    }

    /**
     * 根据 id 修改自己的应用（目前只支持修改应用名称）
     * 注意：只有应用创始人才能修改 isTeam 字段
     *
     * @param appUpdateRequest 应用更新请求
     * @return 更新结果
     */
    @PostMapping("/update/my")
    @SaSpaceCheckPermission(value = AppUserPermissionConstant.APP_UPDATE)
    public BaseResponse<Boolean> updateMyApp(@RequestBody AppUpdateRequest appUpdateRequest) {
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        // 验证应用名称长度不超过10个字
        String appName = appUpdateRequest.getAppName();
        if (StrUtil.isNotBlank(appName)) {
            ThrowUtils.throwIf(appName.length() > 10, ErrorCode.PARAMS_ERROR, "应用名称不能超过10个字");
        }
        Long appId = Long.valueOf(appUpdateRequest.getId());
        App app = new App();
        app.setId(appId);
        app.setAppName(appUpdateRequest.getAppName());
        app.setIsPublic(appUpdateRequest.getIsPublic());
        app.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 删除自己的应用
     *
     * @param deleteRequest 删除请求
     * @return 删除结果
     */
    @PostMapping("/delete/my")
    @SaSpaceCheckPermission(value = AppUserPermissionConstant.APP_DELETE)
    public BaseResponse<Boolean> deleteMyApp(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);

        boolean result = appService.deleteApp(Long.valueOf(deleteRequest.getId()));
        return ResultUtils.success(result);
    }

    @Resource
    private AppUserAuthManager appUserAuthManager;

    /**
     * 根据 id 查看应用详情
     *
     * @param id 应用id
     * @return 应用详情
     */
    @GetMapping("/get")
    @SaSpaceCheckPermission(value = AppUserPermissionConstant.APP_VIEW)
    public BaseResponse<AppVO> getAppById(String id, HttpServletRequest request) {
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);

        User loginUser = userService.getLoginUser(request);
        AppVO appVO = appService.getAppVO(app);
        List<String> permissionList = appUserAuthManager.getPermissionList(app, loginUser);
        appVO.setPermissionList(permissionList);
        return ResultUtils.success(appVO);
    }

    /**
     * 分页查询自己的应用列表（支持根据名称查询，每页最多 20 个）
     *
     * @param appQueryRequest 查询请求
     * @param request         HTTP请求
     * @return 应用列表
     */
    @PostMapping("/list/my")
    public BaseResponse<Page<AppVO>> listMyAppByPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);

        User loginUser = userService.getLoginUser(request);
        appQueryRequest.setUserId(loginUser.getId());

        // 限制每页最多20个
        Integer pageSize = Math.min(appQueryRequest.getPageSize(), 20);
        appQueryRequest.setPageSize(pageSize);
        long pageNum = appQueryRequest.getPageNum();
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize),
                appService.getQueryWrapper(appQueryRequest));

        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 分页查询精选的应用列表（支持根据名称查询，每页最多 20 个）
     *
     * @param appQueryRequest 查询请求
     * @return 精选应用列表
     */
    @Cacheable(
            value = "good_app_page",
            key = "T(com.easen.aicode.utils.CacheKeyUtils).generateKey(#appQueryRequest)",
            condition = "#appQueryRequest.pageNum <= 10"
    )
    @PostMapping("/list/featured")
    public BaseResponse<Page<AppVO>> listFeaturedAppByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 限制每页最多20个
        Integer pageSize = Math.min(appQueryRequest.getPageSize(), 20);
        appQueryRequest.setPageSize(pageSize);
        appQueryRequest.setPriority(99);
        appQueryRequest.setIsPublic(AppTypeEnum.PUBLIC.getValue());
        long pageNum = appQueryRequest.getPageNum();
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize),
                appService.getQueryWrapper(appQueryRequest));

        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 管理员根据 id 删除任意应用
     *
     * @param deleteRequest 删除请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean result = appService.deleteApp(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 管理员根据 id 更新任意应用（支持更新应用名称、应用封面、优先级）
     * 注意：只有应用创始人才能修改 isTeam 字段
     *
     * @param appUpdateRequest 应用更新请求
     * @return 更新结果
     */
    @PostMapping("/update")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest) {
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        // 验证应用名称长度不超过10个字
        String appName = appUpdateRequest.getAppName();
        if (StrUtil.isNotBlank(appName)) {
            ThrowUtils.throwIf(appName.length() > 10, ErrorCode.PARAMS_ERROR, "应用名称不能超过10个字");
        }
        Long appId = appUpdateRequest.getId();
        App app = new App();
        app.setId(appId);
        app.setAppName(appUpdateRequest.getAppName());
        app.setIsPublic(appUpdateRequest.getIsPublic());
        app.setEditTime(LocalDateTime.now());
        app.setPriority(appUpdateRequest.getPriority());
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员分页查询应用列表（支持根据除时间外的任何字段查询，每页数量不限）
     *
     * @param appQueryRequest 查询请求
     * @return 应用列表
     */
    @PostMapping("/list/page")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> listAppByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);

        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize),
                appService.getQueryWrapper(appQueryRequest));

        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 管理员根据 id 查看应用详情
     *
     * @param id 应用id
     * @return 应用详情
     */
    @GetMapping("/get/admin")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<App> getAppByIdAdmin(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        AppVO appVO = appService.getAppVO(app);
        List<String> permissionList = appUserAuthManager.getPermissionsByRole(AppRoleEnum.ADMIN.getValue());
        appVO.setPermissionList(permissionList);
        return ResultUtils.success(app);
    }


    @Resource
    private ProjectDownloadService projectDownloadService;

    /**
     * 下载应用代码
     *
     * @param appId    应用ID
     * @param response 响应
     */
    @GetMapping("/download/{appId}")
    @SaSpaceCheckPermission(value = AppUserPermissionConstant.APP_UPLOAD)
    public void downloadAppCode(@PathVariable Long appId,
                                HttpServletResponse response) {
        // 1. 基础校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        // 2. 查询应用信息
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 4. 构建应用代码目录路径（生成目录，非部署目录）
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 5. 检查代码目录是否存在
        File sourceDir = new File(sourceDirPath);
        ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(),
                ErrorCode.NOT_FOUND_ERROR, "应用代码不存在，请先生成代码");
        // 6. 生成下载文件名（不建议添加中文内容）
        String downloadFileName = String.valueOf(appId);
        // 7. 调用通用下载服务
        projectDownloadService.downloadProjectAsZip(sourceDirPath, downloadFileName, response);
    }
}
