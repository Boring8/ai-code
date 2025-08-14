package com.easen.aicode.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.easen.aicode.common.BaseResponse;
import com.easen.aicode.common.ResultUtils;
import com.easen.aicode.constant.UserConstant;
import com.easen.aicode.exception.ErrorCode;
import com.easen.aicode.exception.ThrowUtils;
import com.easen.aicode.manager.auth.annotation.SaSpaceCheckPermission;
import com.easen.aicode.manager.auth.model.AppUserPermissionConstant;
import com.easen.aicode.model.dto.app.AppTeamCreateRequest;
import com.easen.aicode.model.dto.app.AppTeamInviteRequest;
import com.easen.aicode.model.dto.app.AppTeamMemberQueryRequest;
import com.easen.aicode.model.dto.app.AppTeamQueryRequest;
import com.easen.aicode.model.dto.app.AppTeamRemoveRequest;
import com.easen.aicode.model.entity.App;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.model.vo.AppTeamMemberVO;
import com.easen.aicode.model.vo.AppVO;
import com.easen.aicode.service.AppService;
import com.easen.aicode.service.AppUserService;
import com.easen.aicode.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/appUser")
public class AppUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private AppService appService;

    /**
     * 创建应用团队
     *
     * @param appTeamCreateRequest 创建团队请求
     * @return 创建结果
     */
    @PostMapping("/create")
    @SaSpaceCheckPermission(value = AppUserPermissionConstant.APP_USER_MANAGE)
    public BaseResponse<Boolean> createAppTeam(@RequestBody AppTeamCreateRequest appTeamCreateRequest) {
        ThrowUtils.throwIf(appTeamCreateRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(appTeamCreateRequest.getAppId() == null || appTeamCreateRequest.getUserId() == null,
                ErrorCode.PARAMS_ERROR, "应用ID和用户ID不能为空");

        // 执行创建团队
        boolean result = appUserService.createAppTeam(appTeamCreateRequest.getAppId(), appTeamCreateRequest.getUserId());
        return ResultUtils.success(result);
    }

    /**
     * 邀请用户加入应用团队
     *
     * @param appTeamInviteRequest 邀请请求
     * @return 邀请结果
     */
    @PostMapping("/invite")
    @SaSpaceCheckPermission(value = AppUserPermissionConstant.APP_USER_MANAGE)
    public BaseResponse<Boolean> inviteUserToApp(@RequestBody AppTeamInviteRequest appTeamInviteRequest) {
        ThrowUtils.throwIf(appTeamInviteRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(appTeamInviteRequest.getAppId() == null || appTeamInviteRequest.getUserId() == null,
                ErrorCode.PARAMS_ERROR, "应用ID和用户ID不能为空");
        // 执行邀请
        boolean result = appUserService.inviteUserToApp(appTeamInviteRequest.getAppId(), appTeamInviteRequest.getUserId());
        return ResultUtils.success(result);
    }

    /**
     * 从应用团队中移除用户
     *
     * @param appTeamRemoveRequest 移除请求
     * @return 移除结果
     */
    @PostMapping("/remove")
    @SaSpaceCheckPermission(value = AppUserPermissionConstant.APP_USER_MANAGE)
    public BaseResponse<Boolean> removeUserFromApp(@RequestBody AppTeamRemoveRequest appTeamRemoveRequest) {
        ThrowUtils.throwIf(appTeamRemoveRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(appTeamRemoveRequest.getAppId() == null || appTeamRemoveRequest.getUserId() == null,
                ErrorCode.PARAMS_ERROR, "应用ID和用户ID不能为空");

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
    @GetMapping("/members")
    @SaSpaceCheckPermission(value = AppUserPermissionConstant.APP_USER_MANAGE)
    public BaseResponse<List<AppTeamMemberVO>> getAppTeamMembers(@RequestParam String appId) {
        ThrowUtils.throwIf(appId == null , ErrorCode.PARAMS_ERROR, "应用ID无效");

        List<AppTeamMemberVO> members = appUserService.getAppTeamMembers(Long.valueOf(appId));

        return ResultUtils.success(members);
    }

    /**
     * 分页获取应用团队成员
     *
     * @param appTeamMemberQueryRequest 查询请求
     * @return 分页结果
     */
    @PostMapping("/members/page")
    @SaSpaceCheckPermission(value = AppUserPermissionConstant.APP_USER_MANAGE)
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
     * 分页查询自己有参与的团队应用列表（支持根据名称查询，每页最多 20 个）
     *
     * @param appTeamQueryRequest 查询请求
     * @param request             HTTP请求
     * @return 团队应用列表
     */
    @PostMapping("/list/my")
    @SaCheckLogin
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
