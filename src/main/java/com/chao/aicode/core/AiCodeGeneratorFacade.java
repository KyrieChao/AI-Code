package com.chao.aicode.core;

import com.chao.aicode.ai.AiCodeGeneratorService;
import com.chao.aicode.ai.model.HtmlCodeResult;
import com.chao.aicode.ai.model.MultiFileCodeResult;
import com.chao.aicode.common.response.HTTPResponseCode;
import com.chao.aicode.core.parser.CodeParserExecutor;
import com.chao.aicode.core.saver.CodeFileSaverExecutor;
import com.chao.aicode.exception.BusinessException;
import com.chao.aicode.model.enums.CodeGenTypeEnum;
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
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口 根据生成类型生成代码并保存到文件
     *
     * @param userMessage     用户输入信息
     * @param codeGenTypeEnum 生成
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        String errorMessage = "不支持的生成类型: " + codeGenTypeEnum;
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(htmlCodeResult, CodeGenTypeEnum.HTML);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(multiFileCodeResult, CodeGenTypeEnum.MULTI_FILE);
            }
            default -> throw new BusinessException(HTTPResponseCode.SYSTEM_ERROR, errorMessage);
        };
    }

    /**
     * 统一入口 根据生成类型生成代码并保存到文件
     *
     * @param userMessage     用户输入信息
     * @param codeGenTypeEnum 生成
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        String errorMessage = "不支持的生成类型: " + codeGenTypeEnum;
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(result, CodeGenTypeEnum.HTML);
            }
            case MULTI_FILE -> {
                Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(result, CodeGenTypeEnum.MULTI_FILE);
            }
            default -> throw new BusinessException(HTTPResponseCode.SYSTEM_ERROR, errorMessage);
        };
    }

    /**
     * 处理代码流 并保存代码到文件
     *
     * @param codeStream  代码
     * @param codeGenType 生成类型
     * @return 保存的目录
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType) {
        // 字符串拼接器 用于当流式返回所有的代码之后在保存代码
        StringBuilder builder = new StringBuilder();
        return codeStream.doOnNext(builder::append).doOnComplete(() -> {
            // 流式返回完成后,保存代码
            try {
                // 使用执行器解析代码
                Object parser = CodeParserExecutor.executeParser(builder.toString(), codeGenType);
                // 使用执行器保存代码
                File saveDir = CodeFileSaverExecutor.executeSaver(parser, codeGenType);
                log.info("文件保存目录: {}", saveDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存代码失败: {}", e.getMessage());
            }
        });
    }
}