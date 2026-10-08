package com.takeaway.analysis.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户消费统计实体类
 */
@Data
public class UserConsumptionStats {
    private Long statId;
    private Long userId;
    private Integer totalOrders;
    private BigDecimal totalAmount;
    private BigDecimal avgOrderAmount;
    private LocalDateTime lastOrderTime;
    private LocalDateTime firstOrderTime;
    private LocalDate statDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

