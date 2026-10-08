package com.takeaway.analysis.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 分析结果实体类
 */
@Data
public class AnalysisResult {
    private Long resultId;
    private String analysisType;
    private String resultKey;
    private String resultData;
    private LocalDate statDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

