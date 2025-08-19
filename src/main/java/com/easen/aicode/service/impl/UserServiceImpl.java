package com.easen.aicode.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.RandomUtil;
import com.easen.aicode.constant.UserConstant;
import com.easen.aicode.exception.BusinessException;
import com.easen.aicode.exception.ErrorCode;
import com.easen.aicode.manager.auth.StpKit;
import com.easen.aicode.model.enums.UserRoleEnum;
import com.easen.aicode.model.dto.user.UserQueryRequest;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.mapper.UserMapper;
import com.easen.aicode.model.vo.LoginUserVO;
import com.easen.aicode.model.vo.UserVO;
import com.easen.aicode.service.UserService;
import com.easen.aicode.service.AuthService;
import com.easen.aicode.utils.DeviceUtils;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import java.time.Duration;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.easen.aicode.constant.UserConstant.USER_LOGIN_STATE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户 服务层实现。
 *
 * @author <a>easen</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final String PWD_FAIL_COUNT_PREFIX = "verify:pwd:fail:"; // verify:pwd:fail:{userId}

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private AuthService authService;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 2. 检查是否重复
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 3. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName(userAccount);
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "easen";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, String verifyCode, HttpServletRequest request) {
        // 1. 基础校验（验证码登录：不再校验密码）
        if (StrUtil.isBlank(userAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能为空");
        }
        String reqCode = StrUtil.isBlank(verifyCode) ? request.getParameter("verifyCode") : verifyCode;
        if (StrUtil.isBlank(reqCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码不能为空");
        }

        // 2. 查询用户（以账号=邮箱为准）
        QueryWrapper onlyAccount = new QueryWrapper();
        onlyAccount.eq("userAccount", userAccount);
        User user = this.mapper.selectOneByQuery(onlyAccount);
        if (user == null) {
            // 不存在则自动创建用户（默认角色 user，昵称为邮箱）
            user = new User();
            user.setUserAccount(userAccount);
            user.setUserName(userAccount);
            user.setUserRole(UserRoleEnum.USER.getValue());
            // 密码字段非空，写入随机占位密码（不可用于登录）
            String placeholder = RandomUtil.randomString(16);
            user.setUserPassword(getEncryptPassword(placeholder));
            boolean created = this.save(user);
            if (!created) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "自动创建用户失败");
            }
        }

        // 3. 校验验证码
        boolean ok = authService.verifyCode(user.getId(), reqCode);
        if (!ok) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码校验未通过");
        }

        // 4. 设备信息与登录
        String device = DeviceUtils.getRequestDevice(request);

        StpKit.TEAM.login(user.getId());
        StpKit.TEAM.getSession().set(UserConstant.USER_LOGIN_STATE, user);
        StpUtil.login(user.getId(), device);
        log.info("用户登录成功 - 用户ID: {}, 设备: {}", user.getId(), device);
        StpUtil.getSession().set(USER_LOGIN_STATE, user);

        try {
            redisTemplate.delete(PWD_FAIL_COUNT_PREFIX + user.getId());
        } catch (Exception ignore) {
        }

        // 6. 返回脱敏信息
        return this.getLoginUserVO(user);
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    //    @Override
//    public User getLoginUser(HttpServletRequest request) {
//        // 先判断是否已登录
//        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
//        User currentUser = (User) userObj;
//        if (currentUser == null || currentUser.getId() == null) {
//            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
//        }
//        // 从数据库查询（追求性能的话可以注释，直接返回上述结果）
//        long userId = currentUser.getId();
//        currentUser = this.getById(userId);
//        if (currentUser == null) {
//            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
//        }
//        return currentUser;
//    }
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (Objects.isNull(loginId)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return (User) StpUtil.getSessionByLoginId(loginId).get(USER_LOGIN_STATE);
    }


    //    @Override
//    public boolean userLogout(HttpServletRequest request) {
//        // 先判断是否已登录
//        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
//        if (userObj == null) {
//            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
//        }
//        // 移除登录态
//        request.getSession().removeAttribute(USER_LOGIN_STATE);
//        return true;
//    }
    @Override
    public boolean userLogout(HttpServletRequest request) {
        StpUtil.checkLogin();
        // 移除登录态
        StpUtil.logout();
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .eq("userRole", userRole)
                .like("userAccount", userAccount)
                .like("userName", userName)
                .like("userProfile", userProfile)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public boolean isAdmin(User user) {
        if (user == null) {
            return false;
        }
        return UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    public boolean isAdminRequest(HttpServletRequest request) {
        // 仅管理员可查询
        // 基于 Sa-Token 改造
        Object userObj = StpUtil.getSession().get(USER_LOGIN_STATE);
        // Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public User getUserByUserAccount(String userAccount) {
        if (StrUtil.isBlank(userAccount)) {
            return null;
        }
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        return this.getOne(queryWrapper);
    }

}
