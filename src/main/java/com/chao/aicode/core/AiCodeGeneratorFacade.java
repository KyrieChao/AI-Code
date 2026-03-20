package com.chao.aicode.core;

import cn.hutool.json.JSONUtil;
import com.chao.aicode.ai.AiCodeGeneratorService;
import com.chao.aicode.ai.AiCodeGeneratorServiceFactory;
import com.chao.aicode.ai.model.HtmlCodeResult;
import com.chao.aicode.ai.model.MultiFileCodeResult;
import com.chao.aicode.ai.model.message.AIResponseMessage;
import com.chao.aicode.ai.model.message.ToolExecutedMessage;
import com.chao.aicode.ai.model.message.ToolRequestMessage;
import com.chao.aicode.common.constants.AppConstant;
import com.chao.aicode.common.response.HTTPResponseCode;
import com.chao.aicode.core.builder.VueProjectBuilder;
import com.chao.aicode.core.parser.CodeParserExecutor;
import com.chao.aicode.core.saver.CodeFileSaverExecutor;
import com.chao.aicode.exception.BusinessException;
import com.chao.aicode.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI代码生成器门面类 组合代码生成和保存功能
 */
@Slf4j
@Service
public class AiCodeGeneratorFacade {
    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;
    @Resource
    private VueProjectBuilder vueProjectBuilder;

    /**
     * 统一入口 根据生成类型生成代码并保存到文件
     *
     * @param userMessage     用户输入信息
     * @param codeGenTypeEnum 生成
     * @param appId           应用 ID
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (appId == null) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "appId不能为空");
        }
        String errorMessage = "不支持的生成类型: " + codeGenTypeEnum;
        // 根据appId获取对应服务
        AiCodeGeneratorService codeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = codeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(htmlCodeResult, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = codeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(multiFileCodeResult, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> throw new BusinessException(HTTPResponseCode.SYSTEM_ERROR, errorMessage);
        };
    }

    /**
     * 统一入口 根据生成类型生成代码并保存到文件
     *
     * @param userMessage     用户输入信息
     * @param codeGenTypeEnum 生成
     * @param appId           应用 ID
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (appId == null) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "appId不能为空");
        }
        AiCodeGeneratorService codeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> result = codeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(result, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> result = codeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(result, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = codeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(tokenStream, appId);
            }
        };
    }
    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @param appId       应用 ID
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream, Long appId) {
        return Flux.create(sink -> tokenStream.onPartialResponse((String partialResponse) -> {
                    AIResponseMessage aiResponseMessage = new AIResponseMessage(partialResponse);
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
                    sink.complete();
                })
                .onError((Throwable error) -> {
                    log.error("处理代码流时发生异常: {}", error.getMessage(), error);
                    sink.error(error);
                })
                .start());
    }

    /**
     * 处理代码流 并保存代码到文件
     *
     * @param codeStream  代码
     * @param codeGenType 生成类型
     * @param appId       应用 ID
     * @return 保存的目录
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId) {
        // 字符串拼接器 用于当流式返回所有的代码之后在保存代码
        StringBuilder builder = new StringBuilder();
        return codeStream.doOnNext(builder::append).doOnComplete(() -> {
            // 流式返回完成后,保存代码
            try {
                // 使用执行器解析代码
                Object parser = CodeParserExecutor.executeParser(builder.toString(), codeGenType);
                // 使用执行器保存代码
                File saveDir = CodeFileSaverExecutor.executeSaver(parser, codeGenType, appId);
                log.info("文件保存目录: {}", saveDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存代码失败: {}", e.getMessage());
            }
        });
    }
}