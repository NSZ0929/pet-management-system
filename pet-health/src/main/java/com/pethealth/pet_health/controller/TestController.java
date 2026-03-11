package com.pethealth.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Tag(name = "测试接口", description = "用于验证JWT和权限的测试接口")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @GetMapping("/public")
    @Operation(summary = "公共测试接口", description = "任何人都可以访问，不需要认证")
    public ResponseEntity<?> publicEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "这是一个公共接口，无需认证");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "success");
        
        logger.info("公共接口被访问");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/secure")
    @Operation(
        summary = "安全测试接口", 
        description = "需要JWT认证才能访问",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> secureEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "这是一个安全接口，需要认证");
        response.put("timestamp", LocalDateTime.now());
        response.put("currentUser", auth.getName());
        response.put("authorities", auth.getAuthorities());
        response.put("authenticated", auth.isAuthenticated());
        
        logger.info("安全接口被用户 {} 访问", auth.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    @Operation(
        summary = "管理员测试接口", 
        description = "需要管理员权限才能访问",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> adminEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "管理员接口访问成功");
        response.put("timestamp", LocalDateTime.now());
        response.put("currentUser", auth.getName());
        response.put("role", "ADMIN");
        
        logger.info("管理员接口被用户 {} 访问", auth.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查服务器是否正常运行")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "Pet Health Management System");
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }
}