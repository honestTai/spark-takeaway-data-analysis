package com.takeaway.analysis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security配置
 * 解决 Spring Security 默认拦截导致跨域失败和重定向到 /login 的问题
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（因为使用 JWT）
            .csrf().disable()
            // 启用跨域支持（会使用我们定义的 CorsFilter）
            .cors()
            .and()
            // 放行所有请求，认证逻辑由我们自定义的 JwtAuthenticationInterceptor 处理
            .authorizeRequests()
            .anyRequest().permitAll()
            .and()
            // 禁用默认的登录页面
            .formLogin().disable()
            // 禁用 HTTP Basic 认证
            .httpBasic().disable();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
