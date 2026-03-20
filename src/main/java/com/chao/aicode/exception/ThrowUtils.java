package com.chao.aicode.exception;

import com.chao.aicode.common.response.HTTPResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ThrowUtils {
    private static boolean PRINT_METHOD;

    /**
     * 由 Spring 把值写进来
     */
    @Value("${exception.print-method:false}")
    public void setPrintMethod(boolean printMethod) {
        PRINT_METHOD = printMethod;
        log.info("ThrowUtils DEBUG = {}", PRINT_METHOD);
    }

    public static void throwIf(boolean condition, HTTPResponseCode code, String msg) {
        if (!condition) return;
        String method = getMethodNameIfNeeded();
        throw BusinessException.builder()
                .code(code)
                .description(msg)
                .method(method)
                .build();
    }

    public static void throwIf(boolean condition, HTTPResponseCode code) {
        if (!condition) return;
        String method = getMethodNameIfNeeded();
        throw BusinessException.builder()
                .code(code)
                .description(code.getDescription())
                .method(method)
                .build();
    }

    private static String getMethodNameIfNeeded() {
        if (!PRINT_METHOD) {
            return null;
        }
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(s -> s.skip(2)          // 跳过当前方法和 throwIf 自身
                        .findFirst()
                        .map(f -> f.getClassName() + "." + f.getMethodName())
                        .orElse("unknown"));
    }
}
