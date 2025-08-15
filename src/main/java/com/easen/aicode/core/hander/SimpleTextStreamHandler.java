package com.easen.aicode.core.hander;

import com.easen.aicode.model.entity.User;
import com.easen.aicode.model.enums.ChatHistoryMessageTypeEnum;
import com.easen.aicode.model.enums.ChatHistoryStatusEnum;
import com.easen.aicode.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 简单文本流处理器
 * 处理 HTML 和 MULTI_FILE 类型的流式响应
 */
@Slf4j
public class SimpleTextStreamHandler {

    /**
     * 处理传统流（HTML, MULTI_FILE）
     * 直接收集完整的文本响应
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser) {
        StringBuilder aiResponseBuilder = new StringBuilder();
        return originFlux
                .map(chunk -> {
                    // 收集AI响应内容
                    aiResponseBuilder.append(chunk);
                    return chunk;
                })
                .doOnComplete(() -> {
                    // 流式响应正常完成，添加AI消息到对话历史
                    String aiResponse = aiResponseBuilder.toString();
                    chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId(), ChatHistoryStatusEnum.NORMAL.getValue(), null);
                    log.info("流正常完成，保存完整AI响应到数据库: appId={}, userId={}, contentLength={}", appId, loginUser.getId(), aiResponse.length());
                })
                .doOnError(error -> {
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    log.error("AI回复失败，保存错误信息到数据库: appId={}, userId={}, error={}", appId, loginUser.getId(), error.getMessage());
                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId(),ChatHistoryStatusEnum.AI_INTERRUPTED.getValue(),null);
                });
    }
}
