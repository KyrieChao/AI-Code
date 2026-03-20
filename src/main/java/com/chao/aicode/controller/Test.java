package com.chao.aicode.controller;

import com.chao.aicode.common.response.ApiResponse;
import com.chao.aicode.common.response.HTTPResponseCode;
import com.chao.aicode.exception.ThrowUtils;
import com.chao.aicode.model.dto.test.TestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class Test {

    @Value("${exception.print-method}")
    private boolean flag;

    @GetMapping("/hello")
    public ApiResponse<String> hello(TestDTO dto) {
        ThrowUtils.throwIf(dto.getName() == null, HTTPResponseCode.PARAM_ERROR);
        return ApiResponse.success("hello world");
    }

    @GetMapping("/hello2")
    public ApiResponse<String> hello2(@RequestBody TestDTO dto) {
        return ApiResponse.success("hello world");
    }
}
