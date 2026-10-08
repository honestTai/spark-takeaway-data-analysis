package com.takeaway.analysis.service;

import java.util.List;
import java.util.Map;

/**
 * Spark数据分析服务接口
 */
public interface SparkAnalysisService {
    
    /**
     * 商户销量统计与排名
     * @param limit 返回数量
     * @return 商户销量排名列表
     */
    List<Map<String, Object>> analyzeMerchantSales(int limit);
    
    /**
     * 不同价格区间订单分布分析
     * @return 价格区间分布数据
     */
    List<Map<String, Object>> analyzePriceDistribution();
    
    /**
     * 热门菜品分析
     * @param limit 返回数量
     * @return 热门菜品列表
     */
    List<Map<String, Object>> analyzeHotDishes(int limit);
    
    /**
     * 用户消费频次统计
     * @return 消费频次分布数据
     */
    List<Map<String, Object>> analyzeUserConsumptionFrequency();
    
    /**
     * 用户消费金额区间分析
     * @return 消费金额区间分布数据
     */
    List<Map<String, Object>> analyzeUserConsumptionAmount();
    
    /**
     * 商户评分与销量关系分析
     * @return 评分与销量关系数据
     */
    List<Map<String, Object>> analyzeRatingVsSales();
    
    /**
     * 不同商户类型对比分析
     * @return 商户类型对比数据
     */
    List<Map<String, Object>> analyzeMerchantTypeComparison();
    
    /**
     * 订单趋势分析（按时间）
     * @param days 天数
     * @return 订单趋势数据
     */
    List<Map<String, Object>> analyzeOrderTrend(int days);
    
    /**
     * 菜品类别占比分析
     * @return 菜品类别占比数据
     */
    List<Map<String, Object>> analyzeDishCategoryDistribution();
}

