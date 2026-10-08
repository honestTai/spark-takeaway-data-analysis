package com.takeaway.analysis.service;

import com.takeaway.analysis.entity.CleaningRule;
import java.util.List;
import java.util.Map;

/**
 * 清洗规则服务接口
 */
public interface CleaningRuleService {
    /**
     * 保存清洗规则
     */
    void saveRules(Map<String, Object> rules);
    
    /**
     * 获取所有清洗规则
     */
    Map<String, Object> getAllRules();
    
    /**
     * 获取指定类型的规则
     */
    CleaningRule getRuleByType(String ruleType);
    
    /**
     * 初始化默认规则
     */
    void initDefaultRules();
    
    /**
     * 重置为默认规则
     */
    void resetToDefault();
}
