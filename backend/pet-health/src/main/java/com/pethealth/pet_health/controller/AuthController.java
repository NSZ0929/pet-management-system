package com.pethealth.pet_health.controller;

import com.pethealth.pet_health.entity.User;
import com.pethealth.pet_health.security.JwtUtil;
import com.pethealth.pet_health.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理", description = "用户注册、登录、认证相关接口")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            logger.info("注册用户: {}", user.getUsername());
            
            // 验证必要字段
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("用户名不能为空");
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("密码不能为空");
            }
            
            User registeredUser = userService.register(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "用户注册成功");
            response.put("userId", registeredUser.getId());
            response.put("username", registeredUser.getUsername());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("注册失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("注册失败: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        try {
            logger.info("用户登录: {}", loginUser.getUsername());
            
            // 验证必要字段
            if (loginUser.getUsername() == null || loginUser.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("用户名不能为空");
            }
            if (loginUser.getPassword() == null || loginUser.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("密码不能为空");
            }
            
            // 验证用户
            User user = userService.authenticate(loginUser.getUsername(), loginUser.getPassword());
            
            // 生成Token
            String token = jwtUtil.generateToken(user.getUsername());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "登录成功");
            response.put("token", token);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", jwtUtil.getExpirationDate(token).getTime());
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.warn("登录失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("登录失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("登录异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("服务器错误: " + e.getMessage());
        }
    }

    @GetMapping("/check-token")
    @Operation(summary = "验证Token有效性")
    public ResponseEntity<?> checkToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Token格式错误");
            }
            
            String token = authHeader.substring(7);
            boolean isValid = jwtUtil.validateToken(token);
            
            if (isValid) {
                String username = jwtUtil.extractUsername(token);
                Map<String, Object> response = new HashMap<>();
                response.put("valid", true);
                response.put("username", username);
                response.put("expiresAt", jwtUtil.getExpirationDate(token).getTime());
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Token无效或已过期");
            }
            
        } catch (Exception e) {
            logger.error("Token验证失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token验证失败: " + e.getMessage());
        }
    }
}