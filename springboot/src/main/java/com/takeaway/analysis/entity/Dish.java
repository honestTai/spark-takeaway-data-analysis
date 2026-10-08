package com.takeaway.analysis.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品实体类
 */
@Data
public class Dish {
    private Long dishId;
    private Long merchantId;
    private String dishName;
    private String category;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer salesCount;
    private Integer monthlySales;
    private String description;
    private String imageUrl;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

