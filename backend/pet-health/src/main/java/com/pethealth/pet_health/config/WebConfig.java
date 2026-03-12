package com.pethealth.pet_health.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许的前端地址
                .allowedOrigins(
                    "http://localhost:5173",  // Vite/React
                    "http://localhost:3000",  // React
                    "http://localhost:8080",  // 后端自身（用于测试）
                    "http://localhost:8081",  // 其他端口
                    "http://127.0.0.1:5173",
                    "http://127.0.0.1:3000"
                )
                // 允许的HTTP方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                // 允许的请求头
                .allowedHeaders("*")
                // 允许携带凭证（cookies、认证头）
                .allowCredentials(true)
                // 预检请求缓存时间（秒）
                .maxAge(3600);
    }
}