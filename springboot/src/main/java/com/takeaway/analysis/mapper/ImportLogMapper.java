package com.takeaway.analysis.mapper;

import com.takeaway.analysis.entity.ImportLog;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ImportLogMapper {
    int insert(ImportLog importLog);
    ImportLog selectById(Long logId);
    List<ImportLog> selectAll();
    int update(ImportLog importLog);
}

