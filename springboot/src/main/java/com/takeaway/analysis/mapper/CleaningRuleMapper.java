package com.takeaway.analysis.mapper;

import com.takeaway.analysis.entity.CleaningRule;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CleaningRuleMapper {
    int insert(CleaningRule rule);
    int update(CleaningRule rule);
    CleaningRule selectByType(String ruleType);
    List<CleaningRule> selectAll();
    int deleteByType(String ruleType);
}

