package com.takeaway.analysis.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
public class Order {
    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long merchantId;
    private BigDecimal orderAmount;
    private BigDecimal deliveryFee;
    private BigDecimal discountAmount;
    private BigDecimal actualAmount;
    private Integer orderStatus;
    private String paymentMethod;
    private String deliveryAddress;
    private String contactPhone;
    private String remark;
    private LocalDateTime orderTime;
    private LocalDateTime payTime;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

