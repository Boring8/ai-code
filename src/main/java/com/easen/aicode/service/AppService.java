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

//    /**
//     * 通过 HotKey/本地热点缓存优先获取应用数据，未命中时回源数据库。
//     * 实现类应负责缓存一致性与降级策略。
//     *
//     * @param appId 应用 ID（不能为空）
//     * @return 应用实体；若不存在返回 null 或由实现自行处理（如抛出业务异常）
//     */
//    App getAppByIdWithHotKey(Long appId);
//
//    /**
//     * 删除指定应用并清理相关 HotKey/缓存，保障数据一致性。
//     *
//     * @param appId 应用 ID
//     */
//    void removeByIdWithHotKey(Long appId);

    /**
     * 部署应用（例如将前端产物上传至静态资源托管或对象存储），返回可访问的 URL。
     *
     * @param appId     应用 ID
     * @param loginUser 当前登录用户（用于鉴权与审计）
     * @return 部署后的应用访问 URL
     */
    String deployApp(Long appId, User loginUser);

    /**
     * 获取应用视图对象
     *
     * @param app 应用实体（允许为 null）
     * @return 应用视图对象；当 app 为空或不存在时，返回空视图或由实现决定行为
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用视图对象列表
     *
     * @param appList 应用实体列表（允许为空或包含空元素）
     * @return 应用视图对象列表，顺序与入参对应
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 获取查询条件
     *
     * @param appQueryRequest 查询请求（包含关键词、分类、创建者、排序等条件）
     * @return 用于 MyBatis-Flex 的查询包装器
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);


    /**
     * 应用聊天生成代码（流式）
     *
     * @param appId     应用 ID
     * @param message   用户输入消息/需求描述
     * @param loginUser 登录用户（用于鉴权与限流）
     * @param images    可选的图片 URL 列表（用于多模态场景）
     * @return 代码片段字符串的响应流（可对接 SSE/WebFlux 实时推送）
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser,List<String> images);


    /**
     * 异步生成应用截图并更新封面
     *
     * @param appId  应用 ID
     * @param appUrl 应用访问 URL（用于截图）
     */
    void generateAppScreenshotAsync(Long appId, String appUrl);

    /**
     * 创建app
     *
     * @param appAddRequest 新建应用的参数请求体（名称、描述、配置等）
     * @param request       HTTP 请求（可用于获取操作者、IP、头部信息等）
     * @return 新创建的应用 ID 字符串
     */
    String addApp(AppAddRequest appAddRequest, HttpServletRequest request);

    /**
     * 根据 appId 删除应用。
     *
     * @param appId 应用 ID
     * @return 删除是否成功
     */
    Boolean deleteApp(Long appId);
}
