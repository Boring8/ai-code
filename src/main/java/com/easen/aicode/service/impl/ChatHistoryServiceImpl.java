package com.easen.aicode.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.easen.aicode.constant.UserConstant;
import com.easen.aicode.exception.ErrorCode;
import com.easen.aicode.exception.ThrowUtils;
import com.easen.aicode.mapper.ChatHistoryMapper;
import com.easen.aicode.model.dto.ChatHistoryQueryRequest;
import com.easen.aicode.model.entity.App;
import com.easen.aicode.model.entity.ChatHistory;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.model.enums.ChatHistoryMessageTypeEnum;
import com.easen.aicode.model.enums.ChatHistoryTypeEnum;
import com.easen.aicode.model.vo.ChatHistoryVO;
import com.easen.aicode.service.AppService;
import com.easen.aicode.service.AppUserService;
import com.easen.aicode.service.ChatHistoryService;
import com.easen.aicode.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.mybatisflex.core.paginate.Page;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 对话历史 服务层实现。
 *
 * @author <a>easen</a>
 */
@Slf4j
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    private UserService userService;

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                      LocalDateTime lastCreateTime) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        // 构建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        // 查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    @Override
    public Page<ChatHistoryVO> listAppChatHistoryVOByPage(Long appId, int pageSize,
                                                          LocalDateTime lastCreateTime
    ) {
        // 先获取原始的ChatHistory分页数据
        Page<ChatHistory> chatHistoryPage = listAppChatHistoryByPage(appId, pageSize, lastCreateTime);

        // 转换为ChatHistoryVO
        Page<ChatHistoryVO> chatHistoryVOPage = new Page<>();
        chatHistoryVOPage.setPageNumber(chatHistoryPage.getPageNumber());
        chatHistoryVOPage.setPageSize(chatHistoryPage.getPageSize());
        chatHistoryVOPage.setTotalRow(chatHistoryPage.getTotalRow());
        chatHistoryVOPage.setTotalPage(chatHistoryPage.getTotalPage());

        // 转换记录
        List<ChatHistoryVO> voList = chatHistoryPage.getRecords().stream()
                .map(this::convertToVO)
                .toList();
        chatHistoryVOPage.setRecords(voList);

        return chatHistoryVOPage;
    }

    /**
     * 将ChatHistory转换为ChatHistoryVO
     */
    private ChatHistoryVO convertToVO(ChatHistory chatHistory) {
        ChatHistoryVO vo = ChatHistoryVO.builder()
                .message(chatHistory.getMessage())
                .messageType(chatHistory.getMessageType())
                .appId(chatHistory.getAppId())
                .createTime(chatHistory.getCreateTime())
                .status(chatHistory.getStatus())
                .type(chatHistory.getType())
                .build();

        // 获取用户昵称
        if (chatHistory.getUserId() != null) {
            User user = userService.getById(chatHistory.getUserId());
            if (user != null) {
                vo.setUserName(user.getUserName());
            }
        }

        return vo;
    }

    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId, Integer status, List<String> image) {

        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(message.length() <= 0, ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        // 验证消息类型是否有效
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的消息类型: " + messageType);
        //相同消息生成唯一键,表示同一条消息
        String jsonStr = appId + message + messageType + userId + status;
        String onlyId = DigestUtil.md5Hex(jsonStr);
        List<ChatHistory> chatHistory = new ArrayList<>();
        chatHistory.add(ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(userId)
                .status(status)
                .type(ChatHistoryTypeEnum.TEXT.getValue())
                .onlyId(onlyId)
                .build());
        if (image != null && image.size() > 0) {
            for (String ms : image) {
                chatHistory.add(ChatHistory.builder()
                        .appId(appId)
                        .message(ms)
                        .messageType(messageType)
                        .userId(userId)
                        .status(status)
                        .onlyId(onlyId)
                        .type(ChatHistoryTypeEnum.IMAGE.getValue())
                        .build());
            }
        }

        return this.saveBatch(chatHistory);
    }

    @Override
    public boolean deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId);
        return this.remove(queryWrapper);
    }

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    @Override
    public Page<ChatHistoryVO> listAllChatHistoryVOByPageForAdmin(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = chatHistoryQueryRequest.getPageNum();
        long pageSize = chatHistoryQueryRequest.getPageSize();

        // 查询数据
        QueryWrapper queryWrapper = this.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> chatHistoryPage = this.page(Page.of(pageNum, pageSize), queryWrapper);

        // 转换为ChatHistoryVO
        Page<ChatHistoryVO> chatHistoryVOPage = new Page<>();
        chatHistoryVOPage.setPageNumber(chatHistoryPage.getPageNumber());
        chatHistoryVOPage.setPageSize(chatHistoryPage.getPageSize());
        chatHistoryVOPage.setTotalRow(chatHistoryPage.getTotalRow());
        chatHistoryVOPage.setTotalPage(chatHistoryPage.getTotalPage());

        // 转换记录
        List<ChatHistoryVO> voList = chatHistoryPage.getRecords().stream()
                .map(this::convertToVO)
                .toList();
        chatHistoryVOPage.setRecords(voList);

        return chatHistoryVOPage;
    }

    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            // 查询历史记录
            List<ChatHistory> historyList = queryChatHistory(appId, maxCount);
            if (CollUtil.isEmpty(historyList)) {
                log.info("应用 {} 没有历史对话记录", appId);
                return 0;
            }
            // 清理历史缓存，防止重复加载
            chatMemory.clear();
            // 按 onlyId 分组并处理
            int loadedCount = processGroupedHistory(historyList, chatMemory);
            log.info("成功为 appId: {} 加载了 {} 条历史对话", appId, loadedCount);
            return loadedCount;
        } catch (Exception e) {
            log.error("加载历史对话失败，appId: {}, error: {}", appId, e.getMessage(), e);
            // 加载失败不影响系统运行，只是没有历史上下文
            return 0;
        }
    }
    
    /**
     * 查询聊天历史记录
     */
    private List<ChatHistory> queryChatHistory(Long appId, int maxCount) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(ChatHistory::getAppId, appId)
                .orderBy(ChatHistory::getCreateTime, false)
                .limit(1, maxCount);
        
        List<ChatHistory> historyList = this.list(queryWrapper);
        // 反转列表，确保按时间正序（老的在前，新的在后）
        return historyList.reversed();
    }
    
    /**
     * 处理分组后的历史记录
     */
    private int processGroupedHistory(List<ChatHistory> historyList, MessageWindowChatMemory chatMemory) {
        // 按 onlyId 分组，过滤掉空值
        Map<String, List<ChatHistory>> groupedHistory = historyList.stream()
                .filter(history -> StrUtil.isNotBlank(history.getOnlyId()))
                .collect(Collectors.groupingBy(ChatHistory::getOnlyId));
        int loadedCount = 0;
        for (List<ChatHistory> group : groupedHistory.values()) {
            if (CollUtil.isEmpty(group)) {
                continue;
            }
            String mergedMessage = mergeGroupMessages(group);
            if (StrUtil.isNotBlank(mergedMessage)) {
                boolean added = addMessageToMemory(group.get(0).getMessageType(), mergedMessage, chatMemory);
                if (added) {
                    loadedCount++;
                }
            }
        }
        return loadedCount;
    }
    
    /**
     * 合并分组内的消息
     */
    private String mergeGroupMessages(List<ChatHistory> group) {
        // 分离文本和图片消息
        String textMessage = group.stream()
                .filter(history -> ChatHistoryTypeEnum.TEXT.getValue().equals(history.getType()))
                .map(ChatHistory::getMessage)
                .filter(StrUtil::isNotBlank)
                .findFirst()
                .orElse(null);
        
        List<String> imageMessages = group.stream()
                .filter(history -> ChatHistoryTypeEnum.IMAGE.getValue().equals(history.getType()))
                .map(ChatHistory::getMessage)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
        
        // 构建合并后的消息内容
        StringBuilder mergedMessage = new StringBuilder();
        
        // 添加文本消息
        if (StrUtil.isNotBlank(textMessage)) {
            mergedMessage.append(textMessage);
        }
        
        // 添加图片消息
        for (String imageMsg : imageMessages) {
            if (mergedMessage.length() > 0) {
                mergedMessage.append("\n");
            }
            mergedMessage.append(imageMsg);
        }
        
        return mergedMessage.toString();
    }
    
    /**
     * 将消息添加到聊天记忆中
     */
    private boolean addMessageToMemory(String messageType, String message, MessageWindowChatMemory chatMemory) {
        if (StrUtil.isBlank(messageType) || StrUtil.isBlank(message)) {
            return false;
        }
        
        try {
            if (ChatHistoryMessageTypeEnum.USER.getValue().equals(messageType)) {
                chatMemory.add(UserMessage.from(message));
                return true;
            } else if (ChatHistoryMessageTypeEnum.AI.getValue().equals(messageType)) {
                chatMemory.add(AiMessage.from(message));
                return true;
            } else {
                log.warn("不支持的消息类型: {}", messageType);
                return false;
            }
        } catch (Exception e) {
            log.error("添加消息到聊天记忆失败，messageType: {}, message: {}, error: {}", 
                    messageType, message, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean updateChatHistoryStatus(Long appId, Long userId, Integer status) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        ThrowUtils.throwIf(status == null, ErrorCode.PARAMS_ERROR, "状态不能为空");

        try {
            // 构建更新条件：根据appId和userId更新最新的AI消息状态
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .eq(ChatHistory::getUserId, userId)
                    .eq(ChatHistory::getMessageType, ChatHistoryMessageTypeEnum.AI.getValue())
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1);

            // 更新状态
            ChatHistory updateEntity = new ChatHistory();
            updateEntity.setStatus(status);

            boolean result = this.update(updateEntity, queryWrapper);
            log.info("更新聊天记录状态: appId={}, userId={}, status={}, result={}", appId, userId, status, result);
            return result;
        } catch (Exception e) {
            log.error("更新聊天记录状态失败: appId={}, userId={}, status={}", appId, userId, status, e);
            return false;
        }
    }

}
