package com.easen.aicode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.easen.aicode.constant.UserConstant;
import com.easen.aicode.exception.BusinessException;
import com.easen.aicode.exception.ErrorCode;
import com.easen.aicode.model.entity.App;
import com.easen.aicode.model.entity.AppUser;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.mapper.AppUserMapper;
import com.easen.aicode.model.vo.AppTeamMemberVO;
import com.easen.aicode.service.AppService;
import com.easen.aicode.service.AppUserService;
import com.easen.aicode.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 应用用户关联 服务层实现。
 *
 * @author <a>easen</a>
 */
@Service
public class AppUserServiceImpl extends ServiceImpl<AppUserMapper, AppUser> implements AppUserService {

    @Resource
    @Lazy
    private AppService appService;

    @Resource
    private UserService userService;

    @Override
    public boolean inviteUserToApp(Long appId, Long userId, Integer isCreate) {
        // 参数校验
        if (appId == null || userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        //如果不是创建团队，就校验
        if (isCreate == 0) {
            // 检查应用是否存在且为团队应用
            App app = appService.getById(appId);
            if (app == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
            }
            if (app.getIsTeam() == null || app.getIsTeam() != 1) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该应用不是团队应用");
            }
        }


        // 检查用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        // 检查用户是否已经是团队成员
        if (isUserInApp(appId, userId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已经是团队成员");
        }

        // 创建关联记录
        AppUser appUser = new AppUser();
        appUser.setAppId(appId);
        appUser.setUserId(userId);
        appUser.setCreateTime(LocalDateTime.now());
        appUser.setUpdateTime(LocalDateTime.now());
        appUser.setIsCreate(isCreate);
        return this.save(appUser);
    }

    @Override
    public boolean removeUserFromApp(Long appId, Long userId) {
        // 参数校验
        if (appId == null || userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }

        // 检查应用是否存在
        App app = appService.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }

        // 不能移除创建者
        if (app.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能移除应用创建者");
        }

        // 检查用户是否在团队中
        if (!isUserInApp(appId, userId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不是团队成员");
        }

        // 删除关联记录
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("appId = ?", appId)
                .and("userId = ?", userId);

        return this.remove(queryWrapper);
    }

    @Override
    public boolean isUserInApp(Long appId, Long userId) {
        if (appId == null || userId == null) {
            return false;
        }

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("appId = ?", appId)
                .and("userId = ?", userId);

        return this.count(queryWrapper) > 0;
    }

    @Override
    public List<AppTeamMemberVO> getAppTeamMembers(Long appId) {
        if (appId == null) {
            return new ArrayList<>();
        }

        // 查询应用信息
        App app = appService.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }

        // 查询团队成员
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select("u.id as userId, u.userAccount, u.userName, u.userAvatar, u.userProfile, u.userRole, au.createTime as joinTime,au.isCreate")
                .from("app_user").as("au")
                .leftJoin("user").as("u").on("au.userId = u.id")
                .where("au.appId = ?", appId);

        List<AppTeamMemberVO> members = this.listAs(queryWrapper, AppTeamMemberVO.class);

        return members;
    }

    @Override
    public Page<AppTeamMemberVO> getAppTeamMembersByPage(Long appId, Integer pageNum, Integer pageSize) {
        if (appId == null) {
            return new Page<>();
        }

        // 查询应用信息
        App app = appService.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }

        // 查询团队成员
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select("u.id as userId, u.userAccount, u.userName, u.userAvatar, u.userProfile, u.userRole, au.createTime as joinTime")
                .from("app_user").as("au")
                .leftJoin("user").as("u").on("au.userId = u.id")
                .where("au.appId = ?", appId)
                .orderBy("au.createTime asc");

        Page<AppTeamMemberVO> page = this.pageAs(new Page<>(pageNum, pageSize), queryWrapper, AppTeamMemberVO.class);


        return page;
    }

    @Override
    public boolean hasAppPermission(Long appId, Long userId) {
        if (appId == null || userId == null) {
            return false;
        }
        // 查询应用信息
        App app = appService.getById(appId);
        if (app == null) {
            return false;
        }


        // 如果是创建者，直接有权限
        if (app.getUserId().equals(userId)) {
            return true;
        }

        // 如果是团队应用，检查是否为团队成员
        if (app.getIsTeam() != null && app.getIsTeam() == 1) {
            return isUserInApp(appId, userId);
        }

        return false;
    }

    @Override
    public Page<App> getUserTeamAppsByPage(Long userId, String appName, Integer pageNum, Integer pageSize) {
        if (userId == null) {
            return new Page<>();
        }

        // 构建查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select("DISTINCT a.*")
                .from("app").as("a")
                .leftJoin("app_user").as("au").on("a.id = au.appId")
                .where("(a.userId = ? OR au.userId = ?)", userId, userId)
                .and("a.isTeam = 1"); // 只查询团队应用

        // 如果提供了应用名称，添加模糊查询条件
        if (appName != null && !appName.trim().isEmpty()) {
            queryWrapper.and("a.appName LIKE ?", "%" + appName.trim() + "%");
        }

        // 按创建时间倒序排列
        queryWrapper.orderBy("a.createTime desc");

        // 执行分页查询
        Page<App> page = this.pageAs(new Page<>(pageNum, pageSize), queryWrapper, App.class);

        return page;
    }
}
