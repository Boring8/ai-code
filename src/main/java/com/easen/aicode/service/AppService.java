package com.easen.aicode.service;

import com.easen.aicode.common.BaseResponse;
import com.easen.aicode.common.DeleteRequest;
import com.easen.aicode.model.dto.app.AppAddRequest;
import com.easen.aicode.model.dto.app.AppQueryRequest;
import com.easen.aicode.model.entity.App;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.model.vo.AppThumbDetailVO;
import com.easen.aicode.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestBody;
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
     * 应用聊天生成代码（流式）
     *
     * @param appId   应用 ID
     * @param message 用户消息
     * @param loginUser 登录用户
     * @param images 图片
     * @return 生成的代码流
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser,List<String> images);


    /**
     * 异步生成应用截图并更新封面
     *
     * @param appId  应用ID
     * @param appUrl 应用访问URL
     */
    void generateAppScreenshotAsync(Long appId, String appUrl);

    /**
     * 创建app
     *
     * @param appAddRequest 应用ID
     * @param request
     * @return appId
     */
    String addApp(AppAddRequest appAddRequest, HttpServletRequest request);

    /** 删除 appId
     * @param appId
     * @return
     */
    Boolean deleteApp(Long appId);
}
