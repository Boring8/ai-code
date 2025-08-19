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
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 密码加密
     * @param userPassword
     * @return
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
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, String verifyCode, HttpServletRequest request);
    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);
    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    public UserVO getUserVO(User user);

    public List<UserVO> getUserVOList(List<User> userList);

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
