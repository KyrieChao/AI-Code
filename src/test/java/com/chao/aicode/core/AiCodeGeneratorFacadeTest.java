package com.chao.aicode.core;

import cn.hutool.core.io.FileUtil;
import com.chao.aicode.ai.model.MultiFileCodeResult;
import com.chao.aicode.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorFacadeTest {
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode
                ("生成一个登录页面", CodeGenTypeEnum.MULTI_FILE, 1L);
        assertNotNull(file);
    }

    @Test
    void generateAndSaveCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("生成一个登录页面", CodeGenTypeEnum.VUE_PROJECT, 1L);
        // 阻塞等待
        List<String> result = codeStream.collectList().block();
        Assertions.assertNotNull(result);
        String join = String.join("", result);
        Assertions.assertNotNull(join);
    }
}