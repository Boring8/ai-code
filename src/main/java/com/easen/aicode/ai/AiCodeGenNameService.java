package com.easen.aicode.ai;

import dev.langchain4j.service.SystemMessage;

public interface AiCodeGenNameService {
    @SystemMessage(fromResource = "prompt/codegen-app-name-prompt.txt")
    String generateAppName(String userMessage);
}
