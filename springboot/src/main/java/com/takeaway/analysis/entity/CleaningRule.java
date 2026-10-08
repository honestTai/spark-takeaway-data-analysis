package com.takeaway.analysis.entity;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 数据清洗规则实体类
 */
@Data
public class CleaningRule {
    private Long ruleId;
    private String ruleType; // rating, price, sales
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private String description;
    private Integer status; // 1-启用，0-禁用
}
