package com.takeaway.analysis.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 商户经营统计实体类
 */
@Data
public class MerchantBusinessStats {
    private Long statId;
    private Long merchantId;
    private Integer totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal avgOrderAmount;
    private BigDecimal avgRating;
    private LocalDate statDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

