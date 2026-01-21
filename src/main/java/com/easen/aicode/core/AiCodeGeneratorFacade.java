package com.easen.aicode.core;

import cn.hutool.json.JSONUtil;
import com.easen.aicode.ai.AiCodeGeneratorService;
import com.easen.aicode.ai.AiCodeGeneratorServiceFactory;
import com.easen.aicode.ai.GenerationTaskManager;
import com.easen.aicode.ai.model.HtmlCodeResult;
import com.easen.aicode.ai.model.MultiFileCodeResult;
import com.easen.aicode.ai.model.message.AiResponseMessage;
import com.easen.aicode.ai.model.message.ToolExecutedMessage;
import com.easen.aicode.ai.model.message.ToolRequestMessage;
import com.easen.aicode.constant.AppConstant;
import com.easen.aicode.core.anchorPoint.HtmlStableAnchorInjector;
import com.easen.aicode.core.builder.VueProjectBuilder;
import com.easen.aicode.core.parser.CodeParserExecutor;
import com.easen.aicode.core.saver.CodeFileSaverExecutor;
import com.easen.aicode.exception.BusinessException;
import com.easen.aicode.exception.ErrorCode;
import com.easen.aicode.model.enums.CodeGenTypeEnum;
import com.easen.aicode.service.CodeVersionService;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.File;

/**
 * AI 代码生成外观类，组合生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    @Resource
    private GenerationTaskManager generationTaskManager;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private CodeVersionService codeVersionService;
//
//    /**
//     * 统一入口：根据类型生成并保存代码
//     *
//     * @param userMessage     用户提示词
//     * @param codeGenTypeEnum 生成类型
//     * @param appId 应用 ID
//     * @return 保存的目录
//     */
//    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
//        if (codeGenTypeEnum == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
//        }
//        // 根据 appId 获取相应的 AI 服务实例
//        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
//        return switch (codeGenTypeEnum) {
//            case HTML -> {
//                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
//                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML, appId);
//            }
//            case MULTI_FILE -> {
//                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
//                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE, appId);
//            }
//            default -> {
//                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
//                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
//            }
//        };
//    }

    /**
     * 统一入口：根据类型生成并保存代码（流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @param appId 应用 ID
     * @param userId 用户ID
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId, Long userId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        
        // 检查是否已有正在进行的任务
        if (generationTaskManager.hasActiveTask(appId)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "该应用已有正在进行的生成任务，请等待完成或取消后再试");
        }
        
        // 根据 appId 获取对应的 AI 服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId, userId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId, userId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(tokenStream, appId, userId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 取消正在进行的代码生成
     *
     * @param appId 应用ID
     * @param userId 用户ID
     * @return 是否成功取消
     */
    public boolean cancelGeneration(Long appId, Long userId) {

        return generationTaskManager.cancelTask(appId, userId);
    }

    /**
     * 获取任务管理器
     *
     * @return 任务管理器
     */
    public GenerationTaskManager getGenerationTaskManager() {
        return generationTaskManager;
    }

    /**
     * 通用流式代码处理方法
     *
     * @param codeStream  代码流
     * @param codeGenType 代码生成类型
     * @param appId 应用 ID
     * @param userId 用户ID
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId, Long userId) {
        // 字符串拼接器，用于当流式返回所有的代码之后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        // 部分内容收集器，用于取消时保存已生成的内容
        StringBuilder partialContentBuilder = new StringBuilder();
        
        return Flux.create((FluxSink<String> sink) -> {
            // 注册任务到管理器
            Disposable disposable = codeStream
                    .doOnNext(chunk -> {
                        // 实时收集代码片段
                        codeBuilder.append(chunk);
                        // 同时收集部分内容用于取消时保存
                        partialContentBuilder.append(chunk);
                        // 发送到客户端
                        sink.next(chunk);
                    })
                    .doOnComplete(() -> {
                        // 流式返回完成后，保存代码
                        try {
                            String completeCode = codeBuilder.toString();
                            // 使用执行器解析代码
                            Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                            // 生成完成后，写入一条代码版本记录（用于进入页面恢复代码面板）
                            try {
                                // contentWithAnchor：仅存 canonical HTML（带稳定锚点，用于后续增量编辑/定位）
                                String htmlForAnchor = null;

                                if (parsedResult instanceof HtmlCodeResult htmlCodeResult) {
                                    htmlForAnchor = htmlCodeResult.getHtmlCode();
                                } else if (parsedResult instanceof MultiFileCodeResult multiFileCodeResult) {
                                    htmlForAnchor = multiFileCodeResult.getHtmlCode();
                                }
                                String anchoredHtml = (htmlForAnchor == null || htmlForAnchor.isBlank())
                                        ? ""
                                        : HtmlStableAnchorInjector.injectStableAnchors(htmlForAnchor);
                                codeVersionService.addCodeVersion(appId, codeGenType.getValue(), htmlForAnchor, anchoredHtml, userId);
                            } catch (Exception e) {
                                log.error("写入代码版本失败: {}", e.getMessage(), e);
                            }
                            // 使用执行器保存代码
                            File saveDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appId);

                            log.info("保存成功，目录为：{}", saveDir.getAbsolutePath());
                        } catch (Exception e) {
                            log.error("保存失败: {}", e.getMessage());
                        } finally {
                            // 完成任务
                            generationTaskManager.completeTask(appId);
                            sink.complete();
                        }
                    })
                    .doOnError(error -> {
                        log.error("代码生成失败: {}", error.getMessage());
                        generationTaskManager.completeTask(appId);
                        sink.error(error);
                    })
                    .subscribe();
            
            // 注册任务，传递部分内容收集器
            generationTaskManager.registerTask(appId, codeGenType.getValue(), disposable, sink, userId, partialContentBuilder);
        });
    }

    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @param appId 应用ID
     * @param userId 用户ID
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream, Long appId, Long userId) {
        // 部分内容收集器，用于取消时保存已生成的内容
        StringBuilder partialContentBuilder = new StringBuilder();
        
        return Flux.create((FluxSink<String> sink) -> {
            // 注册任务到管理器
            Disposable disposable = Flux.never().subscribe(); // 占位符，实际取消逻辑在TokenStream中
            
            generationTaskManager.registerTask(appId, "VUE_PROJECT", disposable, sink, userId, partialContentBuilder);
            
            tokenStream.onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        // 收集部分内容
                        partialContentBuilder.append(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        // 执行 Vue 项目构建（同步执行，确保预览时项目已就绪）
                        String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
                        vueProjectBuilder.buildProject(projectPath);

                        generationTaskManager.completeTask(appId);
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        generationTaskManager.completeTask(appId);
                        sink.error(error);
                    })
                    .start();
        });
    }
}
