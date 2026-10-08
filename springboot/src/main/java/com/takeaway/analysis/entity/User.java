package com.takeaway.analysis.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
public class User {
    private Long userId;
    private String username;
    private String phone;
    private String email;
    private Integer gender;
    private LocalDate birthday;
    private LocalDateTime registerTime;
    private LocalDateTime lastLoginTime;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

