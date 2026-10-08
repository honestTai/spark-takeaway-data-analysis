package com.takeaway.analysis.service;

import com.takeaway.analysis.entity.SysUser;
import java.util.Map;

/**
 * 认证服务接口
 */
public interface AuthService {
    /**
     * 用户登录
     */
    Map<String, Object> login(String username, String password, String ip);
    
    /**
     * 根据用户名获取用户信息
     */
    SysUser getUserByUsername(String username);
    
    /**
     * 刷新Token
     */
    String refreshToken(String token);
    
    /**
     * 登出
     */
    void logout(String token);
}

