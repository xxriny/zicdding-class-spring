package com.example.zicdding.application.controller;

import com.example.zicdding.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController("/test")
public class TestController {
    @GetMapping("/test1")
    public ApiResponse<?> test() {
        return ApiResponse.success("성공",null);
    }
    @GetMapping("/test2")
    public ApiResponse<?> test2() {
        return ApiResponse.success("성공",null);
    }
}
