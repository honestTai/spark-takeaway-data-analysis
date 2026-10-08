package com.takeaway.analysis.config;

import com.takeaway.analysis.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT认证拦截器
 */
@Slf4j
@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    // 不需要认证的路径
    private static final String[] EXCLUDE_PATHS = {
        "/auth/login",
        "/auth/refresh",
        "/error",
        "/swagger-ui",
        "/v2/api-docs"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 显式放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        String requestPath = request.getRequestURI();
        String contextPath = request.getContextPath();
        
        // 移除context path
        if (contextPath != null && !contextPath.isEmpty()) {
            requestPath = requestPath.substring(contextPath.length());
        }

        // 检查是否在排除列表中
        for (String excludePath : EXCLUDE_PATHS) {
            if (requestPath.equals(excludePath) || requestPath.startsWith(excludePath + "/")) {
                return true;
            }
        }

        // 获取Token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            try {
                response.getWriter().write("{\"code\":401,\"message\":\"未授权，请先登录\"}");
            } catch (Exception e) {
                log.error("写入响应失败", e);
            }
            return false;
        }

        // 移除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证Token
        if (jwtUtil.isTokenExpired(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            try {
                response.getWriter().write("{\"code\":401,\"message\":\"Token已过期，请重新登录\"}");
            } catch (Exception e) {
                log.error("写入响应失败", e);
            }
            return false;
        }

        // 将用户信息存储到request中，供后续使用
        String username = jwtUtil.getUsernameFromToken(token);
        Long userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        
        request.setAttribute("username", username);
        request.setAttribute("userId", userId);
        request.setAttribute("role", role);

        return true;
    }
}

