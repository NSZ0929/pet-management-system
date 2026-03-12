package com.pethealth.pet_health.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class JwtSecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public JwtSecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（因为使用JWT，无状态）
            .csrf(csrf -> csrf.disable())
            
            // 设置会话管理为无状态
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 启用CORS（使用WebConfig中的配置）
            .cors(Customizer.withDefaults())
            
        .authorizeHttpRequests(auth -> auth
        // 1. 开放认证相关接口（不需要Token）
        .requestMatchers("/auth/**").permitAll()
    
        // 🔐【关键修改】新增这一行，放行所有测试接口
        .requestMatchers("/api/test/**").permitAll()
    
        // 2. 开放API文档相关（如果有Swagger）
        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
    
        // 3. 开放预检请求
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
    
         // 4. 所有其他请求都需要认证
         .anyRequest().authenticated()
        )
            
            // 添加JWT认证过滤器
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    // 密码加密器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}