package com.takeaway.analysis.mapper;

import com.takeaway.analysis.entity.AnalysisResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AnalysisResultMapper {
    int insert(AnalysisResult result);
    int update(AnalysisResult result);
    AnalysisResult selectByTypeAndKey(@Param("analysisType") String analysisType, 
                                      @Param("resultKey") String resultKey);
    List<AnalysisResult> selectByType(String analysisType);
    int deleteByType(String analysisType);
}

