package com.easen.aicode.service;

import com.easen.aicode.model.dto.ChatHistoryQueryRequest;
import com.easen.aicode.model.entity.ChatHistory;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.model.vo.ChatHistoryVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层。
 *
 * @author <a>easen</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加对话历史
     *
     * @param appId       应用 id
     * @param message     消息
     * @param messageType 消息类型
     * @param userId      用户 id
     * @param status      状态
     * @param image       图片 URL 列表（可选，多模态支持）
     * @return 是否成功
     */
    boolean addChatMessage(Long appId,String message, String messageType, Long userId, Integer status,List<String> image);

    /**
     * 根据应用 id 删除对话历史
     *
     * @param appId 应用 id
     * @return 是否删除成功
     */
    boolean deleteByAppId(Long appId);

    /**
     * 分页查询某 APP 的对话记录
     *
     * @param appId           应用 id
     * @param pageSize        每页大小
     * @param lastCreateTime  上次查询的最后创建时间（基于时间游标翻页）
     * @return 对话历史分页
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime);

    /**
     * 分页查询某 APP 的对话记录（返回VO）
     *
     * @param appId           应用 id
     * @param pageSize        每页大小
     * @param lastCreateTime  上次查询的最后创建时间（基于时间游标翻页）
     * @return 对话历史视图分页
     */
    Page<ChatHistoryVO> listAppChatHistoryVOByPage(Long appId, int pageSize,
                                                   LocalDateTime lastCreateTime);

    /**
     * 加载对话历史到内存
     *
     * @param appId      应用 id
     * @param chatMemory 窗口记忆对象
     * @param maxCount   最多加载多少条
     * @return 加载成功的条数
     */
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);

    /**
     * 构造查询条件
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 查询包装器
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 管理员分页查询所有对话历史（返回VO）
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史分页
     */
    Page<ChatHistoryVO> listAllChatHistoryVOByPageForAdmin(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 更新聊天记录状态
     *
     * @param appId  应用ID
     * @param userId 用户ID
     * @param status 状态值
     * @return 是否成功
     */
    boolean updateChatHistoryStatus(Long appId, Long userId, Integer status);
}
