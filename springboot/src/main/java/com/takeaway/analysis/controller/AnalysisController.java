package com.takeaway.analysis.controller;

import com.takeaway.analysis.common.Result;
import com.takeaway.analysis.service.SparkAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据分析控制器
 */
@Slf4j
@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    @Autowired
    private SparkAnalysisService sparkAnalysisService;

    /**
     * 商户销量统计与排名
     */
    @GetMapping("/merchant-sales")
    public Result<List<Map<String, Object>>> getMerchantSales(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> result = sparkAnalysisService.analyzeMerchantSales(limit);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取商户销量排名失败", e);
            return Result.error("分析失败：" + e.getMessage());
        }
    }

    /**
     * 价格区间订单分布
     */
    @GetMapping("/price-distribution")
    public Result<List<Map<String, Object>>> getPriceDistribution() {
        try {
            List<Map<String, Object>> result = sparkAnalysisService.analyzePriceDistribution();
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取价格区间分布失败", e);
            return Result.error("分析失败：" + e.getMessage());
        }
    }

    /**
     * 热门菜品分析
     */
    @GetMapping("/hot-dishes")
    public Result<List<Map<String, Object>>> getHotDishes(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> result = sparkAnalysisService.analyzeHotDishes(limit);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取热门菜品失败", e);
            return Result.error("分析失败：" + e.getMessage());
        }
    }

    /**
     * 用户消费频次统计
     */
    @GetMapping("/user-frequency")
    public Result<List<Map<String, Object>>> getUserConsumptionFrequency() {
        try {
            List<Map<String, Object>> result = sparkAnalysisService.analyzeUserConsumptionFrequency();
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户消费频次失败", e);
            return Result.error("分析失败：" + e.getMessage());
        }
    }

    /**
     * 用户消费金额区间分析
     */
    @GetMapping("/user-amount")
    public Result<List<Map<String, Object>>> getUserConsumptionAmount() {
        try {
            List<Map<String, Object>> result = sparkAnalysisService.analyzeUserConsumptionAmount();
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户消费金额区间失败", e);
            return Result.error("分析失败：" + e.getMessage());
        }
    }

    /**
     * 商户评分与销量关系分析
     */
    @GetMapping("/rating-sales")
    public Result<List<Map<String, Object>>> getRatingVsSales() {
        try {
            List<Map<String, Object>> result = sparkAnalysisService.analyzeRatingVsSales();
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取评分与销量关系失败", e);
            return Result.error("分析失败：" + e.getMessage());
        }
    }

    /**
     * 不同商户类型对比分析
     */
    @GetMapping("/merchant-type")
    public Result<List<Map<String, Object>>> getMerchantTypeComparison() {
        try {
            List<Map<String, Object>> result = sparkAnalysisService.analyzeMerchantTypeComparison();
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取商户类型对比失败", e);
            return Result.error("分析失败：" + e.getMessage());
        }
    }

    /**
     * 订单趋势分析
     */
    @GetMapping("/order-trend")
    public Result<List<Map<String, Object>>> getOrderTrend(
            @RequestParam(defaultValue = "30") int days) {
        try {
            List<Map<String, Object>> result = sparkAnalysisService.analyzeOrderTrend(days);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取订单趋势失败", e);
            return Result.error("分析失败：" + e.getMessage());
        }
    }

    /**
     * 菜品类别占比分析
     */
    @GetMapping("/dish-category")
    public Result<List<Map<String, Object>>> getDishCategoryDistribution() {
        try {
            List<Map<String, Object>> result = sparkAnalysisService.analyzeDishCategoryDistribution();
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取菜品类别占比失败", e);
            return Result.error("分析失败：" + e.getMessage());
        }
    }
}

