package com.takeaway.analysis.controller;

import com.takeaway.analysis.common.Result;
import com.takeaway.analysis.service.DataCleaningService;
import com.takeaway.analysis.service.CleaningRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据清洗控制器
 */
@Slf4j
@RestController
@RequestMapping("/cleaning")
public class DataCleaningController {

    @Autowired
    private DataCleaningService dataCleaningService;

    @Autowired
    private CleaningRuleService cleaningRuleService;

    /**
     * 清洗商户数据
     */
    @PostMapping("/merchants")
    public Result<Map<String, Object>> cleanMerchants(@RequestBody List<com.takeaway.analysis.entity.Merchant> merchants) {
        try {
            Map<String, Object> result = dataCleaningService.cleanMerchants(merchants);
            return Result.success(result);
        } catch (Exception e) {
            log.error("清洗商户数据失败", e);
            return Result.error("清洗失败：" + e.getMessage());
        }
    }

    /**
     * 清洗菜品数据
     */
    @PostMapping("/dishes")
    public Result<Map<String, Object>> cleanDishes(@RequestBody List<com.takeaway.analysis.entity.Dish> dishes) {
        try {
            Map<String, Object> result = dataCleaningService.cleanDishes(dishes);
            return Result.success(result);
        } catch (Exception e) {
            log.error("清洗菜品数据失败", e);
            return Result.error("清洗失败：" + e.getMessage());
        }
    }

    /**
     * 清洗订单数据
     */
    @PostMapping("/orders")
    public Result<Map<String, Object>> cleanOrders(@RequestBody List<com.takeaway.analysis.entity.Order> orders) {
        try {
            Map<String, Object> result = dataCleaningService.cleanOrders(orders);
            return Result.success(result);
        } catch (Exception e) {
            log.error("清洗订单数据失败", e);
            return Result.error("清洗失败：" + e.getMessage());
        }
    }

    /**
     * 验证数据
     */
    @PostMapping("/validate")
    public Result<Map<String, Object>> validateData(@RequestBody Map<String, Object> request) {
        try {
            Object data = request.get("data");
            String dataType = (String) request.get("dataType");
            Map<String, Object> result = dataCleaningService.validateData(data, dataType);
            return Result.success(result);
        } catch (Exception e) {
            log.error("验证数据失败", e);
            return Result.error("验证失败：" + e.getMessage());
        }
    }

    /**
     * 获取清洗规则
     */
    @GetMapping("/rules")
    public Result<Map<String, Object>> getRules() {
        try {
            Map<String, Object> rules = cleaningRuleService.getAllRules();
            return Result.success(rules);
        } catch (Exception e) {
            log.error("获取清洗规则失败", e);
            return Result.error("获取规则失败：" + e.getMessage());
        }
    }

    /**
     * 保存清洗规则
     */
    @PostMapping("/rules")
    public Result<String> saveRules(@RequestBody Map<String, Object> rules) {
        try {
            cleaningRuleService.saveRules(rules);
            return Result.success("清洗规则保存成功");
        } catch (Exception e) {
            log.error("保存清洗规则失败", e);
            return Result.error("保存规则失败：" + e.getMessage());
        }
    }

    /**
     * 重置清洗规则为默认值
     */
    @PostMapping("/rules/reset")
    public Result<String> resetRules() {
        try {
            cleaningRuleService.resetToDefault();
            return Result.success("清洗规则已重置为默认值");
        } catch (Exception e) {
            log.error("重置清洗规则失败", e);
            return Result.error("重置规则失败：" + e.getMessage());
        }
    }
}

