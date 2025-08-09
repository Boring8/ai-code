package com.easen.aicode.controller;

import com.easen.aicode.common.BaseResponse;
import com.easen.aicode.common.ResultUtils;
import com.easen.aicode.exception.ErrorCode;
import com.easen.aicode.exception.ThrowUtils;
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
@RequestMapping("/team")
public class AppUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private AppService appService;
    /**
     * 邀请用户加入应用团队
     *
     * @param appTeamInviteRequest 邀请请求
     * @param request              请求对象
     * @return 邀请结果
     */
    @PostMapping("/invite")
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
        boolean result = appUserService.inviteUserToApp(appTeamInviteRequest.getAppId(), appTeamInviteRequest.getUserId(),0);
        return ResultUtils.success(result);
    }

    /**
     * 从应用团队中移除用户
     *
     * @param appTeamRemoveRequest 移除请求
     * @param request              请求对象
     * @return 移除结果
     */
    @PostMapping("/remove")
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
    @PostMapping("/members/page")
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
    @GetMapping("/check")
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
    @PostMapping("/list/my")
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
