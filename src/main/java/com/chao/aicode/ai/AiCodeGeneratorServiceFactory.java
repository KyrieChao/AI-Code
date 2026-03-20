package com.chao.aicode.ai;

import com.chao.aicode.ai.tools.*;
import com.chao.aicode.model.enums.CodeGenTypeEnum;
import com.chao.aicode.service.ChatHistoryService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI 服务工厂类
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AiCodeGeneratorServiceFactory {
    private final ChatModel chatModel;
    private final StreamingChatModel openAiStreamingChatModel;
    private final StreamingChatModel reasoningStreamingChatModelPrototype;
    private final RedisChatMemoryStore redisChatMemoryStore;
    private final ChatHistoryService chatHistoryService;

    /**
     * 缓存服务
     */
    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMillis(30))
            .expireAfterAccess(Duration.ofMillis(10))
            .removalListener((key, value, cause) ->
                    log.debug("缓存服务被移除: key={}, value={}, cause={}", key, value, cause)).build();

    /**
     * 根据appId获取服务 old
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
    }

    /**
     * 根据appId获取服务
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        String cacheKey = buildCacheKey(appId, codeGenType);
        return serviceCache.get(cacheKey, key -> createAiCodeGeneratorService(appId, codeGenType));
    }

    /**
     * 创建服务
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        log.info("为appId:{} 创建新的AI服务实例", appId);
        // 根据 appId 构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
        return switch (codeGenType) {
            case HTML, MULTI_FILE -> AiServices.builder(AiCodeGeneratorService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(openAiStreamingChatModel)
                    .chatMemory(chatMemory)
                    .build();
            case VUE_PROJECT -> AiServices.builder(AiCodeGeneratorService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(reasoningStreamingChatModelPrototype)
                    .chatMemoryProvider(memoryId -> chatMemory)
                    .tools(
                            new FileWriteTool(),
                            new FileDeleteTool(),
                            new FileReadTool(),
                            new FileDirReadTool(),
                            new FileModifyTool(),
                            new ExitTool()

                    )
                    .hallucinatedToolNameStrategy(toolExecutionRequest ->
                            ToolExecutionResultMessage.from(toolExecutionRequest,
                                    "Error:there is no tool called" + toolExecutionRequest.name())
                    )
                    .build();
        };
    }

    /**
     * 创建 AI 代码生成服务
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0);
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(long appId, CodeGenTypeEnum codeGenType) {
        return appId + "_" + codeGenType.getValue();
    }
}