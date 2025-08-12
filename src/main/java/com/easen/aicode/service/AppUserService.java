package com.easen.aicode.service;

import com.easen.aicode.model.entity.App;
import com.easen.aicode.model.entity.AppUser;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.model.vo.AppTeamMemberVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 应用用户关联 服务层。
 *
 * @author <a>easen</a>
 */
public interface AppUserService extends IService<AppUser> {

    /**
     * 邀请用户加入应用团队
     *
     * @param appId  应用ID
     * @param userId 用户ID

     * @return 是否成功
     */
    boolean inviteUserToApp(Long appId, Long userId);


    /**
     * 从应用团队中移除用户
     *
     * @param appId  应用ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean removeUserFromApp(Long appId, Long userId);

    /**
     * 检查用户是否为应用团队成员
     *
     * @param appId  应用ID
     * @param userId 用户ID
     * @return 是否为团队成员
     */
    boolean isUserInApp(Long appId, Long userId);

    /**
     * 获取应用团队成员列表
     *
     * @param appId 应用ID
     * @return 团队成员列表
     */
    List<AppTeamMemberVO> getAppTeamMembers(Long appId);

    /**
     * 分页获取应用团队成员
     *
     * @param appId    应用ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<AppTeamMemberVO> getAppTeamMembersByPage(Long appId, Integer pageNum, Integer pageSize);

    /**
     * 检查用户是否有权限操作应用（创建者或团队成员）
     *
     * @param appId  应用ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasAppPermission(Long appId, Long userId);

    /**
     * 分页查询用户参与的团队应用列表
     *
     * @param userId   用户ID
     * @param appName  应用名称（可选，支持模糊查询）
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<App> getUserTeamAppsByPage(Long userId, String appName, Integer pageNum, Integer pageSize);

    /**
     * 删除应用的所有团队成员关联关系
     *
     * @param appId 应用ID
     * @return 是否成功
     */
    boolean removeAllUsersFromApp(Long appId);

    /**
     * 创建应用团队
     *
     * @param appId  应用ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean createAppTeam(Long appId, Long userId);
}
