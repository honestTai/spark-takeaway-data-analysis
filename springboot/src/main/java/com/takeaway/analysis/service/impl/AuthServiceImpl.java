package com.takeaway.analysis.service.impl;

import com.takeaway.analysis.entity.SysUser;
import com.takeaway.analysis.mapper.SysUserMapper;
import com.takeaway.analysis.service.AuthService;
import com.takeaway.analysis.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务实现
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Map<String, Object> login(String username, String password, String ip) {
        Map<String, Object> result = new HashMap<>();
        
        // 去除前后空格，防止输入误差
        if (username != null) username = username.trim();
        if (password != null) password = password.trim();
        
        log.info("尝试登录 - 用户名: [{}], 密码长度: {}, 原始密码内容: [{}]", username, password != null ? password.length() : 0, password);
        
        // 查询用户
        SysUser user = sysUserMapper.selectByUsername(username);
        if (user == null) {
            log.warn("登录失败 - 用户不存在: {}", username);
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }

        log.info("查找到用户 - 数据库密文: [{}]", user.getPassword());

        // 验证密码
        boolean matches = passwordEncoder.matches(password, user.getPassword());
        log.info("密码验证结果: {}", matches);
        
        if (!matches) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            result.put("success", false);
            result.put("message", "用户已被禁用");
            return result;
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getUsername(), user.getUserId(), user.getRole());

        // 更新最后登录时间和IP
        sysUserMapper.updateLastLogin(user.getUserId(), LocalDateTime.now(), ip);

        // 返回结果
        result.put("success", true);
        result.put("token", token);
        result.put("user", Map.of(
            "userId", user.getUserId(),
            "username", user.getUsername(),
            "nickname", user.getNickname() != null ? user.getNickname() : user.getUsername(),
            "role", user.getRole(),
            "avatar", user.getAvatar() != null ? user.getAvatar() : ""
        ));

        log.info("用户 {} 登录成功，IP: {}", username, ip);
        return result;
    }

    @Override
    public SysUser getUserByUsername(String username) {
        return sysUserMapper.selectByUsername(username);
    }

    @Override
    public String refreshToken(String token) {
        if (jwtUtil.isTokenExpired(token)) {
            return null;
        }
        return jwtUtil.refreshToken(token);
    }

    @Override
    public void logout(String token) {
        // 这里可以实现Token黑名单机制
        log.info("用户登出，Token: {}", token.substring(0, Math.min(20, token.length())));
    }
}

