package com.takeaway.analysis.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统用户实体类
 */
@Data
public class SysUser {
    private Long userId;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private String role;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

