package com.easen.aicode;

import com.easen.aicode.ai.AiCodeGenNameService;
import com.easen.aicode.ai.AiCodeGenTypeRoutingServiceFactory;
import com.easen.aicode.ai.AiCodeGeneratorService;
import com.easen.aicode.ai.model.HtmlCodeResult;
import com.easen.aicode.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;
    @Resource
    private AiCodeGenNameService aiCodeGenNameService;
    @Test
    void generateHtmlCode() {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("easen个人博客网站");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult multiFileCode = aiCodeGeneratorService.generateMultiFileCode("easen个人博客网站");
        Assertions.assertNotNull(multiFileCode);
    }
    @Test
    void generateNameCode() {
        String result = aiCodeGenNameService.generateAppName("easen个人博客网站");

        System.out.println("123" + result+"321");
        Assertions.assertNotNull(result);
    }

}
