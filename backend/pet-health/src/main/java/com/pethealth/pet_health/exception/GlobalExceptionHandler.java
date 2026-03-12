package com.pethealth.pet_health.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        logger.error("运行时异常: {}", e.getMessage(), e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Internal Server Error");
        response.put("message", e.getMessage());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException e) {
        logger.warn("认证失败: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Authentication Failed");
        response.put("message", "用户名或密码错误");
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException e) {
        logger.warn("访问被拒绝: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Access Denied");
        response.put("message", "没有权限访问该资源");
        response.put("status", HttpStatus.FORBIDDEN.value());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> handleExpiredJwtException(ExpiredJwtException e) {
        logger.warn("JWT已过期: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Token Expired");
        response.put("message", "Token已过期，请重新登录");
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<?> handleSignatureException(SignatureException e) {
        logger.warn("JWT签名无效: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Invalid Token");
        response.put("message", "Token签名无效");
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("参数错误: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Bad Request");
        response.put("message", e.getMessage());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception e) {
        logger.error("未处理的异常: {}", e.getMessage(), e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Internal Server Error");
        response.put("message", "服务器内部错误，请稍后重试");
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("detail", e.getClass().getName() + ": " + e.getMessage());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}