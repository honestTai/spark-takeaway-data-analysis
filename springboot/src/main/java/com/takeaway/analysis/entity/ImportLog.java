package com.takeaway.analysis.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 数据导入日志实体类
 */
@Data
public class ImportLog {
    private Long logId;
    private String importType;
    private String fileName;
    private String filePath;
    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    private String errorMessage;
    private Integer importStatus;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String operator;
    private LocalDateTime createTime;
}

