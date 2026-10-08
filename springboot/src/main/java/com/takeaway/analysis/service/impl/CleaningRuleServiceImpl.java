package com.takeaway.analysis.service.impl;

import com.takeaway.analysis.entity.CleaningRule;
import com.takeaway.analysis.mapper.CleaningRuleMapper;
import com.takeaway.analysis.service.CleaningRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 清洗规则服务实现
 */
@Slf4j
@Service
public class CleaningRuleServiceImpl implements CleaningRuleService {

    @Autowired
    private CleaningRuleMapper cleaningRuleMapper;

    @Override
    @Transactional
    public void saveRules(Map<String, Object> rules) {
        log.info("保存清洗规则：{}", rules);
        
        // 保存评分规则
        if (rules.containsKey("ratingMin") && rules.containsKey("ratingMax")) {
            saveRule("rating", 
                    new BigDecimal(rules.get("ratingMin").toString()),
                    new BigDecimal(rules.get("ratingMax").toString()),
                    "评分范围");
        }
        
        // 保存价格规则
        if (rules.containsKey("priceMin") && rules.containsKey("priceMax")) {
            saveRule("price",
                    new BigDecimal(rules.get("priceMin").toString()),
                    new BigDecimal(rules.get("priceMax").toString()),
                    "价格范围");
        }
        
        // 保存销量规则
        if (rules.containsKey("salesMin") && rules.containsKey("salesMax")) {
            saveRule("sales",
                    new BigDecimal(rules.get("salesMin").toString()),
                    new BigDecimal(rules.get("salesMax").toString()),
                    "销量范围");
        }
        
        log.info("清洗规则保存完成");
    }

    @Override
    public Map<String, Object> getAllRules() {
        List<CleaningRule> rules = cleaningRuleMapper.selectAll();
        Map<String, Object> result = new HashMap<>();
        
        // 默认值
        result.put("ratingMin", 0.0);
        result.put("ratingMax", 5.0);
        result.put("priceMin", 0.0);
        result.put("priceMax", 10000.0);
        result.put("salesMin", 0);
        result.put("salesMax", 10000000);
        
        // 从数据库读取
        for (CleaningRule rule : rules) {
            switch (rule.getRuleType()) {
                case "rating":
                    result.put("ratingMin", rule.getMinValue().doubleValue());
                    result.put("ratingMax", rule.getMaxValue().doubleValue());
                    break;
                case "price":
                    result.put("priceMin", rule.getMinValue().doubleValue());
                    result.put("priceMax", rule.getMaxValue().doubleValue());
                    break;
                case "sales":
                    result.put("salesMin", rule.getMinValue().intValue());
                    result.put("salesMax", rule.getMaxValue().intValue());
                    break;
            }
        }
        
        return result;
    }

    @Override
    public CleaningRule getRuleByType(String ruleType) {
        return cleaningRuleMapper.selectByType(ruleType);
    }

    @Override
    @Transactional
    public void initDefaultRules() {
        // 检查是否已有规则
        if (cleaningRuleMapper.selectAll().isEmpty()) {
            log.info("初始化默认清洗规则");
            
            // 评分规则
            saveRule("rating", BigDecimal.valueOf(0), BigDecimal.valueOf(5), "评分范围");
            
            // 价格规则
            saveRule("price", BigDecimal.valueOf(0), BigDecimal.valueOf(10000), "价格范围");
            
            // 销量规则
            saveRule("sales", BigDecimal.valueOf(0), BigDecimal.valueOf(10000000), "销量范围");
        }
    }

    @Override
    @Transactional
    public void resetToDefault() {
        log.info("重置清洗规则为默认值");
        
        // 删除现有规则
        cleaningRuleMapper.deleteByType("rating");
        cleaningRuleMapper.deleteByType("price");
        cleaningRuleMapper.deleteByType("sales");
        
        // 重新初始化
        initDefaultRules();
    }

    /**
     * 保存单个规则
     */
    private void saveRule(String ruleType, BigDecimal minValue, BigDecimal maxValue, String description) {
        CleaningRule existing = cleaningRuleMapper.selectByType(ruleType);
        
        if (existing != null) {
            // 更新
            existing.setMinValue(minValue);
            existing.setMaxValue(maxValue);
            existing.setDescription(description);
            cleaningRuleMapper.update(existing);
        } else {
            // 新增
            CleaningRule rule = new CleaningRule();
            rule.setRuleType(ruleType);
            rule.setMinValue(minValue);
            rule.setMaxValue(maxValue);
            rule.setDescription(description);
            rule.setStatus(1);
            cleaningRuleMapper.insert(rule);
        }
    }
}
