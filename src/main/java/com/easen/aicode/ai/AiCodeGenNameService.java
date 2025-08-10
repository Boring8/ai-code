package com.easen.aicode.ai;

import dev.langchain4j.service.SystemMessage;

public interface AiCodeGenNameService {

    /**
     * 生成 app 名称
     *
     * @param userMessage 提示词
     * @return
     */
    @SystemMessage(fromResource = "prompt/codegen-app-name-prompt.txt")
    String generateAppName(String userMessage);
}
