package com.pethealth.pet_health.service;

import com.pethealth.pet_health.entity.User;
import com.pethealth.pet_health.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 构造器注入
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 用户注册
     */
    public User register(User user) {
        if (user == null) {
            throw new RuntimeException("User cannot be null");
        }

        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }

        // 检查用户名是否已存在
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在: " + user.getUsername());
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 默认角色
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }

        return userRepository.save(user);
    }

    /**
     * 根据用户名查找用户
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
    }

    /**
     * 登录验证
     */
    public User authenticate(String username, String password) {
        User user = findByUsername(username);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return user;
    }

    /**
     * 修改密码
     */
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = findByUsername(username);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("新密码不能为空");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * 修改用户名
     */
    public User updateUsername(String oldUsername, String newUsername) {
        User user = findByUsername(oldUsername);

        if (newUsername == null || newUsername.trim().isEmpty()) {
            throw new RuntimeException("新用户名不能为空");
        }

        if (userRepository.findByUsername(newUsername).isPresent()) {
            throw new RuntimeException("用户名已存在: " + newUsername);
        }

        user.setUsername(newUsername);
        return userRepository.save(user);
    }
}