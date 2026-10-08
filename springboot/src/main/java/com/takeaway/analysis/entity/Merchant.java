package com.takeaway.analysis.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商户实体类
 */
@Data
public class Merchant {
    private Long merchantId;
    private String merchantName;
    private String merchantType;
    private BigDecimal rating;
    private Integer totalSales;
    private Integer monthlySales;
    private BigDecimal minOrderPrice;
    private BigDecimal deliveryFee;
    private String address;
    private String phone;
    private String businessHours;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

