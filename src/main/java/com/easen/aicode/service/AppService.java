package com.easen.aicode.service;

import com.easen.aicode.model.dto.app.AppQueryRequest;
import com.easen.aicode.model.entity.App;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a>easen</a>
 */
public interface AppService extends IService<App> {

    String deployApp(Long appId, User loginUser);

    /**
     * 获取应用视图对象
     *
     * @param app 应用实体
     * @return 应用视图对象
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用视图对象列表
     *
     * @param appList 应用实体列表
     * @return 应用视图对象列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 获取查询条件
     *
     * @param appQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 验证用户是否有权限操作应用
     *
     * @param appId 应用id
     * @param userId 用户id
     * @return 是否有权限
     */
    boolean validateUserPermission(Long appId, Long userId);

    /**
     * 验证用户是否为应用创建者
     *
     * @param appId 应用id
     * @param userId 用户id
     * @return 是否为创建者
     */
    boolean isAppCreator(Long appId, Long userId);

    /**
     * 应用聊天生成代码（流式 SSE）
     *
     * @param appId   应用 ID
     * @param message 用户消息
     * @param loginUser 请求对象
     * @return 生成结果流
     */
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser);
}
