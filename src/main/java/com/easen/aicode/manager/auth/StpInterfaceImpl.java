package com.easen.aicode.manager.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.easen.aicode.exception.BusinessException;
import com.easen.aicode.exception.ErrorCode;
import com.easen.aicode.model.entity.App;
import com.easen.aicode.model.entity.AppUser;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.model.enums.AppRoleEnum;
import com.easen.aicode.model.enums.AppTypeEnum;
import com.easen.aicode.model.enums.UserRoleEnum;
import com.easen.aicode.service.AppService;
import com.easen.aicode.service.AppUserService;
import com.easen.aicode.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import java.util.*;

import static com.easen.aicode.constant.UserConstant.USER_LOGIN_STATE;


/**
 * 自定义权限加载接口实现类
 */
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {

    // 默认是 /api
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private AppService appService;

    @Resource
    private AppUserService appUserService;

    @Resource
    private AppUserAuthManager appUserAuthManager;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 判断 loginType，仅对类型为 "space" 进行权限校验
        if (!StpKit.TEAM_TYPE.equals(loginType)) {
            return new ArrayList<>();
        }
        // 管理员权限
        List<String> ADMIN_PERMISSIONS = appUserAuthManager.getPermissionsByRole(AppRoleEnum.ADMIN.getValue());
        // 获取上下文对象
        AppUserAuthContext authContext = getAuthContextByRequest();
        // 获取 userId
        User loginUser = (User) StpKit.TEAM.getSessionByLoginId(loginId).get(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户未登录");
        }
        // 管理员
        if (UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole())) {
            return ADMIN_PERMISSIONS;
        }
        Long userId = loginUser.getId();
        // 优先从上下文中获取 AppUser 对象
        AppUser appUser = authContext.getAppUser();
        if (appUser != null) {
            return appUserAuthManager.getPermissionsByRole(appUser.getAppRole());
        }
        // 如果有 appUserId，必然是团队app，通过数据库查询 AppUser 对象
        Long appUserId = authContext.getAppUserId();
        if (appUserId != null) {
            appUser = appUserService.getById(appUserId);
            if (appUser == null) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "应用权限校验 appUserId 错误");
            }
            return appUserAuthManager.getPermissionsByRole(appUser.getAppRole());
        }
        // 如果没有 appUserId，尝试通过 appId 获取 app 对象并处理
        Long appId = authContext.getAppId();
        if (appId == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到 appId 信息");
        }
        App app = appService.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到 app 信息");
        }
        // 根据 App 类型判断权
        boolean isPublic = Objects.equals(app.getIsPublic(), AppTypeEnum.PUBLIC.getValue());
        //是否为团队管理员
        if (app.getUserId().equals(userId)) {
            return ADMIN_PERMISSIONS;
        } else if (appUserService.isUserInApp(appId, userId)) { //是否为团队成员
            // 取出当前登录用户对应的 spaceUser
            AppUser loginSpaceUser = appUserService.getOne(new QueryWrapper().eq(AppUser::getAppId, appId)
                    .eq(AppUser::getUserId, userId));
            return appUserAuthManager.getPermissionsByRole(loginSpaceUser.getAppRole());
        } else if (isPublic) {//是否有公开
            return appUserAuthManager.getPermissionsByRole(AppRoleEnum.VIEWER.getValue());
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * 本项目中不使用。返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    /**
     * 从请求中上下文对象
     */
    private AppUserAuthContext getAuthContextByRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String contentType = request.getHeader(Header.CONTENT_TYPE.getValue());
        AppUserAuthContext authRequest;
        // 请求参数
        if (ContentType.JSON.getValue().equals(contentType)) {
            String body = JakartaServletUtil.getBody(request);
            authRequest = JSONUtil.toBean(body, AppUserAuthContext.class);
        } else {
            Map<String, String> paramMap = JakartaServletUtil.getParamMap(request);
            authRequest = BeanUtil.toBean(paramMap, AppUserAuthContext.class);
        }
        // 根据请求路径区分 id 字段的含义
        Long id = authRequest.getId();
        if (ObjUtil.isNotNull(id)) {
            // 到请求路径的业务前缀，/api/app/aaa?a=1
            String requestURI = request.getRequestURI();
            // 先替换掉上下文，剩下的就是前缀
            String partURI = requestURI.replace(contextPath + "/", "");
            // 前缀的第一个斜杠前的字符串
            String moduleName = StrUtil.subBefore(partURI, "/", false);
            switch (moduleName) {
                case "app" -> authRequest.setAppId(id);
                case "appUser" -> authRequest.setAppUserId(id);
                default -> {
                }
            }
        }
        return authRequest;
    }

    /**
     * 判断对象的所有字段是否为空
     *
     * @param object
     * @return
     */
    private boolean isAllFieldsNull(Object object) {
        if (object == null) {
            return true; // 对象本身为空
        }
        // 获取所有字段并判断是否所有字段都为空
        return Arrays.stream(ReflectUtil.getFields(object.getClass()))
                // 获取字段值
                .map(field -> ReflectUtil.getFieldValue(object, field))
                // 检查是否所有字段都为空
                .allMatch(ObjectUtil::isEmpty);
    }


}
