package com.takeaway.analysis.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.takeaway.analysis.entity.AnalysisResult;
import com.takeaway.analysis.mapper.AnalysisResultMapper;
import com.takeaway.analysis.mapper.MerchantMapper;
import com.takeaway.analysis.service.SparkAnalysisService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.*;
import org.apache.spark.sql.functions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Spark数据分析服务实现
 * 增加缓存、异常处理、数据验证等功能
 */
@Slf4j
@Service
public class SparkAnalysisServiceImpl implements SparkAnalysisService {

    @Value("${spark.app-name}")
    private String appName;

    @Value("${spark.master}")
    private String master;

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${analysis.cache.enabled:true}")
    private boolean cacheEnabled;

    @Value("${analysis.cache.expire-hours:24}")
    private int cacheExpireHours;

    @Autowired
    private SparkSession sparkSession;

    @Autowired
    private AnalysisResultMapper analysisResultMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    // 内存缓存
    private final Map<String, CacheItem> memoryCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("Spark分析服务初始化完成，缓存启用：{}", cacheEnabled);
    }

    @PreDestroy
    public void destroy() {
        log.info("Spark分析服务已关闭");
    }

    @Override
    public List<Map<String, Object>> analyzeMerchantSales(int limit) {
        log.info("开始分析商户销量排名，限制：{}", limit);
        
        // 参数验证
        if (limit <= 0 || limit > 1000) {
            log.warn("无效的limit参数：{}，使用默认值10", limit);
            limit = 10;
        }

        String cacheKey = "merchant_sales_" + limit;
        List<Map<String, Object>> cached = getFromCache(cacheKey);
        if (cached != null) {
            log.info("从缓存获取商户销量排名数据");
            return cached;
        }

        try {
            Dataset<Row> merchants = loadTable("merchants");
            if (merchants == null) {
                log.warn("商户表加载失败，返回空结果");
                return Collections.emptyList();
            }

            Dataset<Row> result = merchants
                    .select("merchant_id", "merchant_name", "total_sales", "rating")
                    .filter("status = 1")
                    .orderBy(functions.desc("total_sales"))
                    .limit(limit);

            List<Map<String, Object>> data = convertToMapList(result);
            if (data.isEmpty()) {
                log.warn("商户表为空，返回空结果");
                return Collections.emptyList();
            }
            saveToCache(cacheKey, data, "merchant_sales");
            return data;
        } catch (Exception e) {
            log.error("分析商户销量排名失败", e);
            // 尝试从数据库缓存获取
            return getFromDbCache("merchant_sales", String.valueOf(limit));
        }
    }

    @Override
    public List<Map<String, Object>> analyzePriceDistribution() {
        log.info("开始分析价格区间订单分布");
        
        String cacheKey = "price_distribution";
        List<Map<String, Object>> cached = getFromCache(cacheKey);
        if (cached != null) {
            log.info("从缓存获取价格区间分布数据");
            return cached;
        }

        try {
            Dataset<Row> orders = loadTable("orders");
            if (orders == null) {
                log.warn("订单表加载失败，返回空结果");
                return Collections.emptyList();
            }

            Dataset<Row> result = orders
                    .filter("order_status IN (2, 4)")
                    .select(
                            functions.when(functions.col("actual_amount").lt(30), "0-30元")
                                    .when(functions.col("actual_amount").lt(50), "30-50元")
                                    .when(functions.col("actual_amount").lt(100), "50-100元")
                                    .otherwise("100元以上")
                                    .as("price_range"),
                            functions.col("order_id")
                    )
                    .groupBy("price_range")
                    .agg(functions.count("order_id").as("order_count"))
                    .orderBy("price_range");

            List<Map<String, Object>> data = convertToMapList(result);
            saveToCache(cacheKey, data, "price_distribution");
            return data;
        } catch (Exception e) {
            log.error("分析价格区间订单分布失败", e);
            return getFromDbCache("price_distribution", null);
        }
    }

    @Override
    public List<Map<String, Object>> analyzeHotDishes(int limit) {
        log.info("开始分析热门菜品，限制：{}", limit);
        
        if (limit <= 0 || limit > 1000) {
            log.warn("无效的limit参数：{}，使用默认值10", limit);
            limit = 10;
        }

        String cacheKey = "hot_dishes_" + limit;
        List<Map<String, Object>> cached = getFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            Dataset<Row> dishes = loadTable("dishes");
            if (dishes == null) {
                log.warn("菜品表加载失败，返回空结果");
                return Collections.emptyList();
            }

            Dataset<Row> result = dishes
                    .select("dish_id", "dish_name", "category", "sales_count", "price")
                    .filter("status = 1")
                    .orderBy(functions.desc("sales_count"))
                    .limit(limit);

            List<Map<String, Object>> data = convertToMapList(result);
            saveToCache(cacheKey, data, "hot_dishes");
            return data;
        } catch (Exception e) {
            log.error("分析热门菜品失败", e);
            return getFromDbCache("hot_dishes", String.valueOf(limit));
        }
    }

    @Override
    public List<Map<String, Object>> analyzeUserConsumptionFrequency() {
        log.info("开始分析用户消费频次");
        
        String cacheKey = "user_frequency";
        List<Map<String, Object>> cached = getFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            Dataset<Row> userStats = loadTable("user_consumption_stats");
            if (userStats == null) {
                log.warn("用户消费统计表加载失败，返回空结果");
                return Collections.emptyList();
            }

            Dataset<Row> result = userStats
                    .select(
                            functions.when(functions.col("total_orders").equalTo(1), "1次")
                                    .when(functions.col("total_orders").leq(5), "2-5次")
                                    .when(functions.col("total_orders").leq(10), "6-10次")
                                    .otherwise("10次以上")
                                    .as("frequency_range"),
                            functions.col("user_id")
                    )
                    .groupBy("frequency_range")
                    .agg(functions.count("user_id").as("user_count"))
                    .orderBy("frequency_range");

            List<Map<String, Object>> data = convertToMapList(result);
            saveToCache(cacheKey, data, "user_frequency");
            return data;
        } catch (Exception e) {
            log.error("分析用户消费频次失败", e);
            return getFromDbCache("user_frequency", null);
        }
    }

    @Override
    public List<Map<String, Object>> analyzeUserConsumptionAmount() {
        log.info("开始分析用户消费金额区间");
        
        String cacheKey = "user_amount";
        List<Map<String, Object>> cached = getFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            Dataset<Row> userStats = loadTable("user_consumption_stats");
            if (userStats == null) {
                log.warn("用户消费统计表加载失败，返回空结果");
                return Collections.emptyList();
            }

            Dataset<Row> result = userStats
                    .select(
                            functions.when(functions.col("total_amount").lt(100), "0-100元")
                                    .when(functions.col("total_amount").lt(500), "100-500元")
                                    .when(functions.col("total_amount").lt(1000), "500-1000元")
                                    .otherwise("1000元以上")
                                    .as("amount_range"),
                            functions.col("user_id")
                    )
                    .groupBy("amount_range")
                    .agg(functions.count("user_id").as("user_count"))
                    .orderBy("amount_range");

            List<Map<String, Object>> data = convertToMapList(result);
            saveToCache(cacheKey, data, "user_amount");
            return data;
        } catch (Exception e) {
            log.error("分析用户消费金额区间失败", e);
            return getFromDbCache("user_amount", null);
        }
    }

    @Override
    public List<Map<String, Object>> analyzeRatingVsSales() {
        log.info("开始分析商户评分与销量关系（走 MySQL）");

        String cacheKey = "rating_sales";
        List<Map<String, Object>> cached = getFromCache(cacheKey);
        if (cached != null) {
            log.info("从缓存获取评分与销量数据");
            return cached;
        }

        try {
            // 简单排序查询，直接走 MySQL，无需 Spark
            List<Map<String, Object>> data = merchantMapper.selectRatingVsSales(500);
            if (data == null || data.isEmpty()) {
                log.warn("商户表无数据");
                return Collections.emptyList();
            }
            saveToCache(cacheKey, data, "rating_sales");
            return data;
        } catch (Exception e) {
            log.error("分析商户评分与销量关系失败", e);
            return getFromDbCache("rating_sales", null);
        }
    }

    @Override
    public List<Map<String, Object>> analyzeMerchantTypeComparison() {
        log.info("开始分析不同商户类型对比（走 MySQL）");

        String cacheKey = "merchant_type";
        List<Map<String, Object>> cached = getFromCache(cacheKey);
        if (cached != null) {
            log.info("从缓存获取商户类型对比数据");
            return cached;
        }

        try {
            // GROUP BY 聚合，直接走 MySQL，无需 Spark
            List<Map<String, Object>> data = merchantMapper.selectTypeComparison();
            if (data == null || data.isEmpty()) {
                log.warn("商户表无数据");
                return Collections.emptyList();
            }
            saveToCache(cacheKey, data, "merchant_type");
            return data;
        } catch (Exception e) {
            log.error("分析不同商户类型对比失败", e);
            return getFromDbCache("merchant_type", null);
        }
    }

    @Override
    public List<Map<String, Object>> analyzeOrderTrend(int days) {
        log.info("开始分析订单趋势，天数：{}", days);
        
        if (days <= 0 || days > 365) {
            log.warn("无效的days参数：{}，使用默认值30", days);
            days = 30;
        }

        String cacheKey = "order_trend_" + days;
        List<Map<String, Object>> cached = getFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            Dataset<Row> orders = loadTable("orders");
            if (orders == null) {
                log.warn("订单表加载失败，返回空结果");
                return Collections.emptyList();
            }

            Dataset<Row> result = orders
                    .filter("order_status IN (2, 4)")
                    .select(
                            functions.date_format(functions.col("order_time"), "yyyy-MM-dd").as("order_date"),
                            functions.col("order_id")
                    )
                    .groupBy("order_date")
                    .agg(functions.count("order_id").as("order_count"))
                    .orderBy("order_date")
                    .limit(days);

            List<Map<String, Object>> data = convertToMapList(result);
            saveToCache(cacheKey, data, "order_trend");
            return data;
        } catch (Exception e) {
            log.error("分析订单趋势失败", e);
            return getFromDbCache("order_trend", String.valueOf(days));
        }
    }

    @Override
    public List<Map<String, Object>> analyzeDishCategoryDistribution() {
        log.info("开始分析菜品类别占比");
        
        String cacheKey = "dish_category";
        List<Map<String, Object>> cached = getFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            Dataset<Row> dishes = loadTable("dishes");
            if (dishes == null) {
                log.warn("菜品表加载失败，返回空结果");
                return Collections.emptyList();
            }

            Dataset<Row> result = dishes
                    .filter("status = 1 AND category IS NOT NULL")
                    .groupBy("category")
                    .agg(functions.count("dish_id").as("dish_count"))
                    .orderBy(functions.desc("dish_count"));

            List<Map<String, Object>> data = convertToMapList(result);
            saveToCache(cacheKey, data, "dish_category");
            return data;
        } catch (Exception e) {
            log.error("分析菜品类别占比失败", e);
            return getFromDbCache("dish_category", null);
        }
    }

    /**
     * 从MySQL加载表数据
     */
    private Dataset<Row> loadTable(String tableName) {
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            return sparkSession.read()
                    .format("jdbc")
                    .option("url", jdbcUrl)
                    .option("dbtable", tableName)
                    .option("user", username)
                    .option("password", password)
                    .load();
        } catch (Exception e) {
            log.error("加载表 {} 失败", tableName, e);
            return null;
        }
    }

    /**
     * 将Dataset转换为Map列表
     */
    private List<Map<String, Object>> convertToMapList(Dataset<Row> dataset) {
        if (dataset == null) {
            return Collections.emptyList();
        }
        
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            return dataset.collectAsList().stream()
                    .map(row -> {
                        Map<String, Object> map = new HashMap<>();
                        String[] columns = row.schema().fieldNames();
                        for (String column : columns) {
                            Object value = row.getAs(column);
                            map.put(column, value);
                        }
                        return map;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("转换Dataset失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 从内存缓存获取
     */
    private List<Map<String, Object>> getFromCache(String key) {
        if (!cacheEnabled) {
            return null;
        }
        
        CacheItem item = memoryCache.get(key);
        if (item != null && !item.isExpired()) {
            return item.getData();
        }
        
        if (item != null) {
            memoryCache.remove(key);
        }
        return null;
    }

    /**
     * 保存到内存缓存
     */
    private void saveToCache(String key, List<Map<String, Object>> data, String analysisType) {
        if (!cacheEnabled) {
            return;
        }
        
        memoryCache.put(key, new CacheItem(data, cacheExpireHours));
        
        // 同时保存到数据库缓存
        try {
            AnalysisResult result = new AnalysisResult();
            result.setAnalysisType(analysisType);
            result.setResultKey(key);
            result.setResultData(JSON.toJSONString(data));
            result.setStatDate(LocalDate.now());
            
            AnalysisResult existing = analysisResultMapper.selectByTypeAndKey(analysisType, key);
            if (existing != null) {
                result.setResultId(existing.getResultId());
                analysisResultMapper.update(result);
            } else {
                analysisResultMapper.insert(result);
            }
        } catch (Exception e) {
            log.warn("保存分析结果到数据库失败", e);
        }
    }

    /**
     * 从数据库缓存获取
     */
    private List<Map<String, Object>> getFromDbCache(String analysisType, String resultKey) {
        try {
            AnalysisResult result;
            if (StringUtils.hasText(resultKey)) {
                result = analysisResultMapper.selectByTypeAndKey(analysisType, resultKey);
            } else {
                List<AnalysisResult> results = analysisResultMapper.selectByType(analysisType);
                result = results != null && !results.isEmpty() ? results.get(0) : null;
            }
            
            if (result != null && result.getResultData() != null) {
                return JSON.parseObject(result.getResultData(), new TypeReference<List<Map<String, Object>>>() {});
            }
        } catch (Exception e) {
            log.warn("从数据库缓存获取分析结果失败", e);
        }
        return Collections.emptyList();
    }

    /**
     * 缓存项
     */
    private static class CacheItem {
        @Getter
        private final List<Map<String, Object>> data;
        private final long expireTime;

        public CacheItem(List<Map<String, Object>> data, int expireHours) {
            this.data = data;
            this.expireTime = System.currentTimeMillis() + expireHours * 3600 * 1000L;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
}
