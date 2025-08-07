package com.easen.aicode.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.easen.aicode.annotation.AuthCheck;
import com.easen.aicode.common.BaseResponse;
import com.easen.aicode.common.DeleteRequest;
import com.easen.aicode.common.ResultUtils;
import com.easen.aicode.constant.UserConstant;
import com.easen.aicode.exception.ErrorCode;
import com.easen.aicode.exception.ThrowUtils;
import com.easen.aicode.model.dto.app.AppAddRequest;
import com.easen.aicode.model.dto.app.AppDeployRequest;
import com.easen.aicode.model.dto.app.AppQueryRequest;
import com.easen.aicode.model.dto.app.AppTeamQueryRequest;
import com.easen.aicode.model.dto.app.AppUpdateRequest;
import com.easen.aicode.model.dto.app.AppTeamInviteRequest;
import com.easen.aicode.model.dto.app.AppTeamRemoveRequest;
import com.easen.aicode.model.dto.app.AppTeamMemberQueryRequest;
import com.easen.aicode.model.entity.App;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.model.vo.AppVO;
import com.easen.aicode.model.vo.AppTeamMemberVO;
import com.easen.aicode.service.AppService;
import com.easen.aicode.service.AppUserService;
import com.easen.aicode.service.UserService;
import com.mybatisflex.core.paginate.Page;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

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

    @Autowired
    private AppUserService appUserService;

    /**
     * 应用聊天生成代码（流式 SSE）
     *
     * @param appId   应用 ID
     * @param message 用户消息
     * @param request 请求对象
     * @return 生成结果流
     */
    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
                                                       @RequestParam String message,
                                                       HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务生成代码（流式）
        Flux<String> contentFlux = appService.chatToGenCode(appId, message, loginUser);
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
     * 应用部署
     *
     * @param appDeployRequest 部署请求
     * @param request          请求
     * @return 部署 URL
     */
    @PostMapping("/deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = appDeployRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
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
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(appAddRequest.getInitPrompt()), ErrorCode.PARAMS_ERROR, "初始化提示词不能为空");
        
        // 验证应用名称长度不超过10个字
        String appName = appAddRequest.getAppName();
        ThrowUtils.throwIf(StrUtil.isBlank(appName), ErrorCode.PARAMS_ERROR, "应用名称不能为空");
        ThrowUtils.throwIf(appName.length() > 10, ErrorCode.PARAMS_ERROR, "应用名称不能超过10个字");
        
        User loginUser = userService.getLoginUser(request);
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        boolean result = appService.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(String.valueOf(app.getId()));
    }

    /**
     * 根据 id 修改自己的应用（目前只支持修改应用名称）
     * 注意：只有应用创始人才能修改 isTeam 字段
     *
     * @param appUpdateRequest 应用更新请求
     * @param request          HTTP请求
     * @return 更新结果
     */
    @PostMapping("/update/my")
    @Transactional
    public BaseResponse<Boolean> updateMyApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        
        // 验证应用名称长度不超过10个字
        String appName = appUpdateRequest.getAppName();
        if (StrUtil.isNotBlank(appName)) {
            ThrowUtils.throwIf(appName.length() > 10, ErrorCode.PARAMS_ERROR, "应用名称不能超过10个字");
        }
        
        User loginUser = userService.getLoginUser(request);
        Long appId = appUpdateRequest.getId();
        
        // 验证权限,创建者
        ThrowUtils.throwIf(!appService.validateUserPermission(appId, loginUser.getId()), 
                ErrorCode.NO_AUTH_ERROR, "无权限操作此应用");

        // 检查是否要修改 isTeam 字段
        if (appUpdateRequest.getIsTeam() != null) {
            // 只有应用创始人才能修改 isTeam 字段
            ThrowUtils.throwIf(!appService.isAppCreator(appId, loginUser.getId()),
                    ErrorCode.NO_AUTH_ERROR, "只有应用创始人才能修改团队属性");
        }

        boolean isConvertingToTeam = appUpdateRequest.getIsTeam() != null && appUpdateRequest.getIsTeam() == 1;
        
        App app = new App();
        app.setId(appId);
        app.setAppName(appUpdateRequest.getAppName());
        app.setIsTeam(appUpdateRequest.getIsTeam());
        app.setEditTime(LocalDateTime.now());
        
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        
        // 如果从个人应用转换为团队应用，将创建者添加到团队中
        if (isConvertingToTeam) {
            try {
                appUserService.addCreatorToApp(appId, loginUser.getId());
            } catch (Exception e) {
                // 如果添加创建者到团队失败，记录日志但不影响应用更新
                log.warn("将创建者添加到团队失败: appId={}, userId={}, error={}", 
                        appId, loginUser.getId(), e.getMessage());
            }
        }
        
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 删除自己的应用
     *
     * @param deleteRequest 删除请求
     * @param request       HTTP请求
     * @return 删除结果
     */
    @PostMapping("/delete/my")
    public BaseResponse<Boolean> deleteMyApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        
        User loginUser = userService.getLoginUser(request);
        Long appId = deleteRequest.getId();
        
        // 验证权限
        ThrowUtils.throwIf(!appService.validateUserPermission(appId, loginUser.getId()), 
                ErrorCode.NO_AUTH_ERROR, "无权限操作此应用");
        
        boolean result = appService.removeById(appId);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 查看应用详情
     *
     * @param id 应用id
     * @return 应用详情
     */
    @GetMapping("/get")
    public BaseResponse<AppVO> getAppById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(appService.getAppVO(app));
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
    @PostMapping("/list/featured")
    public BaseResponse<Page<AppVO>> listFeaturedAppByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);

        
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
     * 管理员根据 id 删除任意应用
     *
     * @param deleteRequest 删除请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean result = appService.removeById(deleteRequest.getId());
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
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);

        // 验证应用名称长度不超过10个字
        String appName = appUpdateRequest.getAppName();
        if (StrUtil.isNotBlank(appName)) {
            ThrowUtils.throwIf(appName.length() > 10, ErrorCode.PARAMS_ERROR, "应用名称不能超过10个字");
        }

        User loginUser = userService.getLoginUser(request);
        Long appId = appUpdateRequest.getId();


        // 检查是否要修改 isTeam 字段
        if (appUpdateRequest.getIsTeam() != null) {
            // 只有应用创始人才能修改 isTeam 字段
            ThrowUtils.throwIf(!appService.isAppCreator(appId, loginUser.getId()),
                    ErrorCode.NO_AUTH_ERROR, "只有应用创始人才能修改团队属性");
        }

        boolean isConvertingToTeam = appUpdateRequest.getIsTeam() != null && appUpdateRequest.getIsTeam() == 1;

        App app = new App();
        app.setId(appId);
        app.setAppName(appUpdateRequest.getAppName());
        app.setIsTeam(appUpdateRequest.getIsTeam());
        app.setEditTime(LocalDateTime.now());

        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        // 如果从个人应用转换为团队应用，将创建者添加到团队中
        if (isConvertingToTeam) {
            try {
                appUserService.addCreatorToApp(appId, loginUser.getId());
            } catch (Exception e) {
                // 如果添加创建者到团队失败，记录日志但不影响应用更新
                log.warn("将创建者添加到团队失败: appId={}, userId={}, error={}",
                        appId, loginUser.getId(), e.getMessage());
            }
        }

        return ResultUtils.success(true);
    }

    /**
     * 管理员分页查询应用列表（支持根据除时间外的任何字段查询，每页数量不限）
     *
     * @param appQueryRequest 查询请求
     * @return 应用列表
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
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
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<App> getAppByIdAdmin(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(app);
    }

    // ==================== 团队协作相关接口 ====================

    /**
     * 邀请用户加入应用团队
     *
     * @param appTeamInviteRequest 邀请请求
     * @param request              请求对象
     * @return 邀请结果
     */
    @PostMapping("/team/invite")
    public BaseResponse<Boolean> inviteUserToApp(@RequestBody AppTeamInviteRequest appTeamInviteRequest, 
                                                HttpServletRequest request) {
        ThrowUtils.throwIf(appTeamInviteRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(appTeamInviteRequest.getAppId() == null || appTeamInviteRequest.getUserId() == null, 
                          ErrorCode.PARAMS_ERROR, "应用ID和用户ID不能为空");

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 验证当前用户是否为应用创建者
        boolean isCreator = appService.isAppCreator(appTeamInviteRequest.getAppId(), loginUser.getId());
        ThrowUtils.throwIf(!isCreator, ErrorCode.NO_AUTH_ERROR, "只有应用创建者可以邀请用户");

        // 执行邀请
        boolean result = appUserService.inviteUserToApp(appTeamInviteRequest.getAppId(), appTeamInviteRequest.getUserId());
        return ResultUtils.success(result);
    }

    /**
     * 从应用团队中移除用户
     *
     * @param appTeamRemoveRequest 移除请求
     * @param request              请求对象
     * @return 移除结果
     */
    @PostMapping("/team/remove")
    public BaseResponse<Boolean> removeUserFromApp(@RequestBody AppTeamRemoveRequest appTeamRemoveRequest, 
                                                  HttpServletRequest request) {
        ThrowUtils.throwIf(appTeamRemoveRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(appTeamRemoveRequest.getAppId() == null || appTeamRemoveRequest.getUserId() == null, 
                          ErrorCode.PARAMS_ERROR, "应用ID和用户ID不能为空");

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 验证当前用户是否为应用创建者
        boolean isCreator = appService.isAppCreator(appTeamRemoveRequest.getAppId(), loginUser.getId());
        ThrowUtils.throwIf(!isCreator, ErrorCode.NO_AUTH_ERROR, "只有应用创建者可以移除用户");

        // 执行移除
        boolean result = appUserService.removeUserFromApp(appTeamRemoveRequest.getAppId(), appTeamRemoveRequest.getUserId());
        return ResultUtils.success(result);
    }

    /**
     * 获取应用团队成员列表
     *
     * @param appId 应用ID
     * @return 团队成员列表
     */
    @GetMapping("/team/members")
    public BaseResponse<List<AppTeamMemberVO>> getAppTeamMembers(@RequestParam Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");

        List<AppTeamMemberVO> members = appUserService.getAppTeamMembers(appId);
        return ResultUtils.success(members);
    }

    /**
     * 分页获取应用团队成员
     *
     * @param appTeamMemberQueryRequest 查询请求
     * @return 分页结果
     */
    @PostMapping("/team/members/page")
    public BaseResponse<Page<AppTeamMemberVO>> getAppTeamMembersByPage(@RequestBody AppTeamMemberQueryRequest appTeamMemberQueryRequest) {
        ThrowUtils.throwIf(appTeamMemberQueryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(appTeamMemberQueryRequest.getAppId() == null, ErrorCode.PARAMS_ERROR, "应用ID不能为空");

        Integer pageNum = appTeamMemberQueryRequest.getPageNum();
        Integer pageSize = appTeamMemberQueryRequest.getPageSize();
        
        Page<AppTeamMemberVO> page = appUserService.getAppTeamMembersByPage(
                appTeamMemberQueryRequest.getAppId(), pageNum, pageSize);
        
        return ResultUtils.success(page);
    }

    /**
     * 检查用户是否为应用团队成员
     *
     * @param appId  应用ID
     * @param userId 用户ID
     * @return 是否为团队成员
     */
    @GetMapping("/team/check")
    public BaseResponse<Boolean> checkUserInApp(@RequestParam Long appId, @RequestParam Long userId) {
        ThrowUtils.throwIf(appId == null || userId == null, ErrorCode.PARAMS_ERROR, "应用ID和用户ID不能为空");

        boolean isMember = appUserService.isUserInApp(appId, userId);
        return ResultUtils.success(isMember);
    }

    /**
     * 分页查询自己有参与的团队应用列表（支持根据名称查询，每页最多 20 个）
     *
     * @param appTeamQueryRequest 查询请求
     * @param request             HTTP请求
     * @return 团队应用列表
     */
    @PostMapping("/list/my/team")
    public BaseResponse<Page<AppVO>> listMyTeamAppByPage(@RequestBody AppTeamQueryRequest appTeamQueryRequest, 
                                                        HttpServletRequest request) {
        ThrowUtils.throwIf(appTeamQueryRequest == null, ErrorCode.PARAMS_ERROR);
        
        User loginUser = userService.getLoginUser(request);
        appTeamQueryRequest.setUserId(loginUser.getId());
        
        // 限制每页最多20个
        Integer pageSize = Math.min(appTeamQueryRequest.getPageSize(), 20);
        appTeamQueryRequest.setPageSize(pageSize);
        
        long pageNum = appTeamQueryRequest.getPageNum();
        String appName = appTeamQueryRequest.getAppName();
        
        // 调用服务查询用户参与的团队应用
        Page<App> appPage = appUserService.getUserTeamAppsByPage(
                loginUser.getId(), appName, (int) pageNum, pageSize);
        
        // 转换为AppVO
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        
        return ResultUtils.success(appVOPage);
    }
}
