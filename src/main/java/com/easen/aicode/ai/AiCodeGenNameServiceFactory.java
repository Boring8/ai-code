package com.easen.aicode.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AiCodeGenNameServiceFactory {
    @Resource
    private ChatModel chatModel;

    /**
     * 创建AI代码生成app名称实例
     */
    @Bean
    public AiCodeGenNameService aiCodeGenNameService() {
        return AiServices.builder(AiCodeGenNameService.class)
                .chatModel(chatModel)
                .build();
    }
}
