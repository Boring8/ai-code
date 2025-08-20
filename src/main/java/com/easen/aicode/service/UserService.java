package com.easen.aicode.service;

import com.easen.aicode.model.dto.user.UserQueryRequest;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.model.vo.LoginUserVO;
import com.easen.aicode.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author <a>easen</a>
 */
public interface UserService extends IService<User> {
    /**
     * 获取脱敏的已登录用户信息
     *
     * @param user 原始用户实体（可能包含敏感字段）
     * @return 脱敏后的登录用户视图
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 密码加密
     *
     * @param userPassword 明文密码
     * @return 加密后的密码串
     */
    public String getEncryptPassword(String userPassword);

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);
    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param verifyCode   验证码（如需）
     * @param request      HTTP 请求（用于会话、IP 等）
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, String verifyCode, HttpServletRequest request);
    /**
     * 获取当前登录用户
     *
     * @param request HTTP 请求（从会话/令牌中解析用户）
     * @return 当前登录用户；未登录时由实现决定行为（抛出异常或返回 null）
     */
    User getLoginUser(HttpServletRequest request);
    /**
     * 用户注销
     *
     * @param request HTTP 请求
     * @return 是否注销成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取用户视图对象
     *
     * @param user 用户实体
     * @return 用户视图对象
     */
    public UserVO getUserVO(User user);

    /**
     * 批量转换用户视图对象
     *
     * @param userList 用户实体列表
     * @return 用户视图对象列表
     */
    public List<UserVO> getUserVOList(List<User> userList);

    /**
     * 构建用户查询条件
     *
     * @param userQueryRequest 查询请求（包含关键词、角色、状态、时间范围等）
     * @return 查询包装器
     */
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 判断用户是否为管理员
     *
     * @param user 用户对象
     * @return 是否为管理员
     */
    boolean isAdmin(User user);

    /**
     * 根据用户账号查询用户
     *
     * @param userAccount 用户账号
     * @return 用户对象
     */
    User getUserByUserAccount(String userAccount);

}
