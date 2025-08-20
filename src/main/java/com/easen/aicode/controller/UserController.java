package com.easen.aicode.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.easen.aicode.common.BaseResponse;
import com.easen.aicode.common.DeleteRequest;
import com.easen.aicode.common.ResultUtils;
import com.easen.aicode.constant.UserConstant;
import com.easen.aicode.exception.BusinessException;
import com.easen.aicode.exception.ErrorCode;
import com.easen.aicode.exception.ThrowUtils;
import com.easen.aicode.model.dto.auth.SendVerifyCodeRequest;
import com.easen.aicode.model.dto.user.*;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.model.vo.LoginUserVO;
import com.easen.aicode.model.vo.UserVO;
import com.easen.aicode.service.AuthService;
import com.easen.aicode.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户 控制层。
 *
 * @author <a>easen</a>
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Resource
    private AuthService authService;
//    /**
//     * 用户注册（当前登录已改为“存在即登，不存在自动创建后登录”，该接口暂不开放）
//     *
//     * @param userRegisterRequest 用户注册请求
//     * @return 注册结果
//     */
//    @PostMapping("/register")
//    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
//        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
//        String userAccount = userRegisterRequest.getUserAccount();
//        String userPassword = userRegisterRequest.getUserPassword();
//        String checkPassword = userRegisterRequest.getCheckPassword();
//        long result = userService.userRegister(userAccount, userPassword, checkPassword);
//        return ResultUtils.success(result);
//    }
    /**
     * 发送验证码
     */
    @PostMapping("/sendVerify")
    public BaseResponse<Boolean> sendVerifyCode(@RequestBody SendVerifyCodeRequest requestBody) {
        if (requestBody == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID非法");
        }
        authService.sendVerifyCode(requestBody.getEmail());
        return ResultUtils.success(true);
    }
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        String verifyCode = userLoginRequest.getVerifyCode();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, verifyCode, request);
        return ResultUtils.success(loginUserVO);
    }

    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    @PostMapping("/logout")
    @SaCheckLogin
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 创建用户
     */
    @PostMapping("/add")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        // 默认密码 12345678
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/get")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 或账号获取包装类(团队管理)
     */
    @GetMapping("/get/vo")
    @SaCheckLogin
    public BaseResponse<UserVO> getUserVOById(@RequestParam(required = false) Long id,
                                              @RequestParam(required = false) String userAccount) {
        // 参数校验：id 和 userAccount 至少提供一个
        if (id == null && StrUtil.isBlank(userAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请提供用户ID或用户账号");
        }

        User user = null;

        // 优先使用ID查询
        if (id != null && id > 0) {
            user = userService.getById(id);
        }

        // 如果ID查询失败或未提供ID，则使用账号查询
        if (user == null && StrUtil.isNotBlank(userAccount)) {
            user = userService.getUserByUserAccount(userAccount);
        }

        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 更新用户
     */
    @PostMapping("/update/admin")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUserAdmin(@RequestBody UserUpdateAdminRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
    /**
     * 更新用户
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        StpUtil.checkLogin();
        StpUtil.logout();
        return ResultUtils.success(true);
    }
    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }


    /**
     * 分页获取用户封装列表（仅管理员）
     *
     * @param userQueryRequest 查询请求参数
     */
    @PostMapping("/list/page/vo")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = userQueryRequest.getPageNum();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(Page.of(pageNum, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        // 数据脱敏
        Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

}
