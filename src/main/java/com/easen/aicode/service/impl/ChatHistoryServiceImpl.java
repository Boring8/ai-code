package com.easen.aicode.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.easen.aicode.constant.UserConstant;
import com.easen.aicode.exception.ErrorCode;
import com.easen.aicode.exception.ThrowUtils;
import com.easen.aicode.mapper.ChatHistoryMapper;
import com.easen.aicode.model.dto.ChatHistoryQueryRequest;
import com.easen.aicode.model.entity.App;
import com.easen.aicode.model.entity.ChatHistory;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.model.enums.ChatHistoryMessageTypeEnum;
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
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author <a>easen</a>
 */
@Slf4j
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService {
    @Resource
    @Lazy
    private AppService appService;

    @Resource
    private AppUserService appUserService;

    @Resource
    private UserService userService;
    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                      LocalDateTime lastCreateTime,
                                                      User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 验证权限：只有应用创建者和管理员可以查看
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isHasAppPermission = appUserService.hasAppPermission(app.getUserId(),loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && isHasAppPermission, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
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
                                                          LocalDateTime lastCreateTime,
                                                          User loginUser) {
        // 先获取原始的ChatHistory分页数据
        Page<ChatHistory> chatHistoryPage = listAppChatHistoryByPage(appId, pageSize, lastCreateTime, loginUser);
        
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
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        // 验证消息类型是否有效
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的消息类型: " + messageType);
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(userId)
                .build();
        return this.save(chatHistory);
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
            // 直接构造查询条件，起始点为 1 而不是 0，用于排除最新的用户消息
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, maxCount);
            List<ChatHistory> historyList = this.list(queryWrapper);
            if (CollUtil.isEmpty(historyList)) {
                return 0;
            }
            // 反转列表，确保按时间正序（老的在前，新的在后）
            historyList = historyList.reversed();
            // 按时间顺序添加到记忆中
            int loadedCount = 0;
            // 先清理历史缓存，防止重复加载
            chatMemory.clear();
            for (ChatHistory history : historyList) {
                if (ChatHistoryMessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    chatMemory.add(UserMessage.from(history.getMessage()));
                    loadedCount++;
                } else if (ChatHistoryMessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                    chatMemory.add(AiMessage.from(history.getMessage()));
                    loadedCount++;
                }
            }
            log.info("成功为 appId: {} 加载了 {} 条历史对话", appId, loadedCount);
            return loadedCount;
        } catch (Exception e) {
            log.error("加载历史对话失败，appId: {}, error: {}", appId, e.getMessage(), e);
            // 加载失败不影响系统运行，只是没有历史上下文
            return 0;
        }
    }

}
