package com.takeaway.analysis.service.impl;

import com.takeaway.analysis.entity.Merchant;
import com.takeaway.analysis.entity.Dish;
import com.takeaway.analysis.entity.Order;
import com.takeaway.analysis.entity.CleaningRule;
import com.takeaway.analysis.mapper.MerchantMapper;
import com.takeaway.analysis.mapper.DishMapper;
import com.takeaway.analysis.service.DataCleaningService;
import com.takeaway.analysis.service.CleaningRuleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据清洗服务实现
 */
@Slf4j
@Service
public class DataCleaningServiceImpl implements DataCleaningService {

    @Value("${data.cleaning.rating.min:0}")
    private Double minRating;

    @Value("${data.cleaning.rating.max:5}")
    private Double maxRating;

    @Value("${data.cleaning.price.min:0}")
    private Double minPrice;

    @Value("${data.cleaning.price.max:10000}")
    private Double maxPrice;

    @Value("${data.cleaning.sales.min:0}")
    private Integer minSales;

    @Value("${data.cleaning.sales.max:10000000}")
    private Integer maxSales;

    @Autowired
    private CleaningRuleService cleaningRuleService;
    
    @Autowired
    private MerchantMapper merchantMapper;
    
    @Autowired
    private DishMapper dishMapper;

    @Override
    public Map<String, Object> cleanMerchants(List<Merchant> merchants) {
        log.info("开始清洗商户数据，原始数量：{}", merchants.size());
        
        // 从数据库加载最新规则
        loadRulesFromDb();
        
        Map<String, Object> result = new HashMap<>();
        List<Merchant> cleaned = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int duplicateCount = 0;
        int dbDuplicateCount = 0;
        int invalidCount = 0;
        
        // 用于去重的Set（基于商户名称）- 当前批次去重
        Set<String> nameSet = new HashSet<>();
        
        // 从数据库加载已存在的商户名称（用于数据库去重）
        Set<String> existingNames = new HashSet<>();
        try {
            List<String> dbNames = merchantMapper.selectAllNames();
            if (dbNames != null) {
                for (String name : dbNames) {
                    if (name != null) {
                        existingNames.add(name.toLowerCase().trim());
                    }
                }
            }
            log.info("数据库中已存在 {} 个商户", existingNames.size());
        } catch (Exception e) {
            log.warn("从数据库加载商户名称失败，跳过数据库去重检查", e);
        }
        
        for (Merchant merchant : merchants) {
            try {
                // 1. 数据标准化
                standardizeMerchant(merchant);
                
                // 2. 数据验证
                String validationError = validateMerchant(merchant);
                if (validationError != null) {
                    errors.add("商户[" + merchant.getMerchantName() + "]验证失败：" + validationError);
                    invalidCount++;
                    continue;
                }
                
                // 3. 异常值处理
                handleMerchantOutliers(merchant);
                
                // 4. 当前批次去重检查
                String nameKey = merchant.getMerchantName().toLowerCase().trim();
                if (nameSet.contains(nameKey)) {
                    duplicateCount++;
                    log.debug("商户[{}]在当前批次中重复，跳过", merchant.getMerchantName());
                    continue;
                }
                
                // 5. 数据库去重检查
                if (existingNames.contains(nameKey)) {
                    dbDuplicateCount++;
                    log.debug("商户[{}]在数据库中已存在，跳过", merchant.getMerchantName());
                    continue;
                }
                
                nameSet.add(nameKey);
                cleaned.add(merchant);
            } catch (Exception e) {
                log.error("清洗商户数据失败：{}", merchant.getMerchantName(), e);
                errors.add("商户[" + merchant.getMerchantName() + "]清洗失败：" + e.getMessage());
                invalidCount++;
            }
        }
        
        result.put("originalCount", merchants.size());
        result.put("cleanedCount", cleaned.size());
        result.put("duplicateCount", duplicateCount);
        result.put("dbDuplicateCount", dbDuplicateCount);
        result.put("invalidCount", invalidCount);
        result.put("errors", errors);
        result.put("cleanedData", cleaned);
        
        log.info("商户数据清洗完成，清洗后：{}条，批次去重：{}条，数据库去重：{}条，无效：{}条", 
                cleaned.size(), duplicateCount, dbDuplicateCount, invalidCount);
        
        return result;
    }
    
    /**
     * 从数据库加载规则
     */
    private void loadRulesFromDb() {
        try {
            Map<String, Object> rules = cleaningRuleService.getAllRules();
            if (rules.containsKey("ratingMin")) {
                minRating = ((Number) rules.get("ratingMin")).doubleValue();
                maxRating = ((Number) rules.get("ratingMax")).doubleValue();
            }
            if (rules.containsKey("priceMin")) {
                minPrice = ((Number) rules.get("priceMin")).doubleValue();
                maxPrice = ((Number) rules.get("priceMax")).doubleValue();
            }
            if (rules.containsKey("salesMin")) {
                minSales = ((Number) rules.get("salesMin")).intValue();
                maxSales = ((Number) rules.get("salesMax")).intValue();
            }
        } catch (Exception e) {
            log.warn("从数据库加载清洗规则失败，使用默认值", e);
        }
    }

    @Override
    public Map<String, Object> cleanDishes(List<Dish> dishes) {
        log.info("开始清洗菜品数据，原始数量：{}", dishes.size());
        
        Map<String, Object> result = new HashMap<>();
        List<Dish> cleaned = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int duplicateCount = 0;
        int dbDuplicateCount = 0;
        int invalidCount = 0;
        
        // 用于去重的Set（基于商户ID+菜品名称）- 当前批次去重
        Set<String> keySet = new HashSet<>();
        
        for (Dish dish : dishes) {
            try {
                // 1. 数据标准化
                standardizeDish(dish);
                
                // 2. 数据验证
                String validationError = validateDish(dish);
                if (validationError != null) {
                    errors.add("菜品[" + dish.getDishName() + "]验证失败：" + validationError);
                    invalidCount++;
                    continue;
                }
                
                // 3. 异常值处理
                handleDishOutliers(dish);
                
                // 4. 当前批次去重检查
                String key = dish.getMerchantId() + "_" + dish.getDishName().toLowerCase().trim();
                if (keySet.contains(key)) {
                    duplicateCount++;
                    log.debug("菜品[{}]在当前批次中重复，跳过", dish.getDishName());
                    continue;
                }
                
                // 5. 数据库去重检查
                try {
                    Dish existingDish = dishMapper.selectByMerchantIdAndName(dish.getMerchantId(), dish.getDishName());
                    if (existingDish != null) {
                        dbDuplicateCount++;
                        log.debug("菜品[{}]在数据库中已存在，跳过", dish.getDishName());
                        continue;
                    }
                } catch (Exception e) {
                    log.warn("数据库去重检查失败：{}", e.getMessage());
                }
                
                keySet.add(key);
                cleaned.add(dish);
            } catch (Exception e) {
                log.error("清洗菜品数据失败：{}", dish.getDishName(), e);
                errors.add("菜品[" + dish.getDishName() + "]清洗失败：" + e.getMessage());
                invalidCount++;
            }
        }
        
        result.put("originalCount", dishes.size());
        result.put("cleanedCount", cleaned.size());
        result.put("duplicateCount", duplicateCount);
        result.put("dbDuplicateCount", dbDuplicateCount);
        result.put("invalidCount", invalidCount);
        result.put("errors", errors);
        result.put("cleanedData", cleaned);
        
        log.info("菜品数据清洗完成，清洗后：{}条，批次去重：{}条，数据库去重：{}条，无效：{}条", 
                cleaned.size(), duplicateCount, dbDuplicateCount, invalidCount);
        
        return result;
    }

    @Override
    public Map<String, Object> cleanOrders(List<Order> orders) {
        log.info("开始清洗订单数据，原始数量：{}", orders.size());
        
        Map<String, Object> result = new HashMap<>();
        List<Order> cleaned = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int duplicateCount = 0;
        int invalidCount = 0;
        
        // 用于去重的Set（基于订单编号）
        Set<String> orderNoSet = new HashSet<>();
        
        for (Order order : orders) {
            try {
                // 1. 数据标准化
                standardizeOrder(order);
                
                // 2. 数据验证
                String validationError = validateOrder(order);
                if (validationError != null) {
                    errors.add("订单[" + order.getOrderNo() + "]验证失败：" + validationError);
                    invalidCount++;
                    continue;
                }
                
                // 3. 异常值处理
                handleOrderOutliers(order);
                
                // 4. 去重检查
                if (orderNoSet.contains(order.getOrderNo())) {
                    duplicateCount++;
                    continue;
                }
                orderNoSet.add(order.getOrderNo());
                
                cleaned.add(order);
            } catch (Exception e) {
                log.error("清洗订单数据失败：{}", order.getOrderNo(), e);
                errors.add("订单[" + order.getOrderNo() + "]清洗失败：" + e.getMessage());
                invalidCount++;
            }
        }
        
        result.put("originalCount", orders.size());
        result.put("cleanedCount", cleaned.size());
        result.put("duplicateCount", duplicateCount);
        result.put("invalidCount", invalidCount);
        result.put("errors", errors);
        result.put("cleanedData", cleaned);
        
        log.info("订单数据清洗完成，清洗后数量：{}，去重：{}，无效：{}", 
                cleaned.size(), duplicateCount, invalidCount);
        
        return result;
    }

    @Override
    public <T> List<T> deduplicate(List<T> data, String key) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }
        
        Map<Object, T> map = new LinkedHashMap<>();
        // 这里需要根据实际类型实现去重逻辑
        return new ArrayList<>(map.values());
    }

    @Override
    public Map<String, Object> validateData(Object data, String dataType) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        
        if (data == null) {
            errors.add("数据不能为空");
            result.put("valid", false);
            result.put("errors", errors);
            return result;
        }
        
        String validationError = null;
        switch (dataType.toLowerCase()) {
            case "merchant":
                if (data instanceof Merchant) {
                    validationError = validateMerchant((Merchant) data);
                }
                break;
            case "dish":
                if (data instanceof Dish) {
                    validationError = validateDish((Dish) data);
                }
                break;
            case "order":
                if (data instanceof Order) {
                    validationError = validateOrder((Order) data);
                }
                break;
        }
        
        if (validationError != null) {
            errors.add(validationError);
        }
        
        result.put("valid", errors.isEmpty());
        result.put("errors", errors);
        return result;
    }

    /**
     * 标准化商户数据
     */
    private void standardizeMerchant(Merchant merchant) {
        // 去除前后空格
        if (merchant.getMerchantName() != null) {
            merchant.setMerchantName(merchant.getMerchantName().trim());
        }
        if (merchant.getMerchantType() != null) {
            merchant.setMerchantType(merchant.getMerchantType().trim());
        }
        if (merchant.getAddress() != null) {
            merchant.setAddress(merchant.getAddress().trim());
        }
        if (merchant.getPhone() != null) {
            merchant.setPhone(merchant.getPhone().trim().replaceAll("[^0-9]", ""));
        }
        
        // 设置默认值
        if (merchant.getStatus() == null) {
            merchant.setStatus(1);
        }
        if (merchant.getTotalSales() == null) {
            merchant.setTotalSales(0);
        }
        if (merchant.getMonthlySales() == null) {
            merchant.setMonthlySales(0);
        }
        if (merchant.getDeliveryFee() == null) {
            merchant.setDeliveryFee(BigDecimal.ZERO);
        }
    }

    /**
     * 标准化菜品数据
     */
    private void standardizeDish(Dish dish) {
        if (dish.getDishName() != null) {
            dish.setDishName(dish.getDishName().trim());
        }
        if (dish.getCategory() != null) {
            dish.setCategory(dish.getCategory().trim());
        }
        if (dish.getDescription() != null) {
            dish.setDescription(dish.getDescription().trim());
        }
        
        if (dish.getStatus() == null) {
            dish.setStatus(1);
        }
        if (dish.getSalesCount() == null) {
            dish.setSalesCount(0);
        }
        if (dish.getMonthlySales() == null) {
            dish.setMonthlySales(0);
        }
    }

    /**
     * 标准化订单数据
     */
    private void standardizeOrder(Order order) {
        if (order.getOrderNo() != null) {
            order.setOrderNo(order.getOrderNo().trim());
        }
        if (order.getPaymentMethod() != null) {
            order.setPaymentMethod(order.getPaymentMethod().trim());
        }
        if (order.getDeliveryAddress() != null) {
            order.setDeliveryAddress(order.getDeliveryAddress().trim());
        }
        if (order.getContactPhone() != null) {
            order.setContactPhone(order.getContactPhone().trim().replaceAll("[^0-9]", ""));
        }
        
        if (order.getOrderStatus() == null) {
            order.setOrderStatus(1);
        }
        if (order.getDeliveryFee() == null) {
            order.setDeliveryFee(BigDecimal.ZERO);
        }
        if (order.getDiscountAmount() == null) {
            order.setDiscountAmount(BigDecimal.ZERO);
        }
    }

    /**
     * 验证商户数据
     */
    private String validateMerchant(Merchant merchant) {
        if (StringUtils.isBlank(merchant.getMerchantName())) {
            return "商户名称不能为空";
        }
        
        if (merchant.getRating() != null) {
            if (merchant.getRating().compareTo(BigDecimal.valueOf(minRating)) < 0 ||
                merchant.getRating().compareTo(BigDecimal.valueOf(maxRating)) > 0) {
                return "评分超出有效范围[" + minRating + "-" + maxRating + "]";
            }
        }
        
        if (merchant.getMinOrderPrice() != null) {
            if (merchant.getMinOrderPrice().compareTo(BigDecimal.valueOf(minPrice)) < 0 ||
                merchant.getMinOrderPrice().compareTo(BigDecimal.valueOf(maxPrice)) > 0) {
                return "起送价超出有效范围[" + minPrice + "-" + maxPrice + "]";
            }
        }
        
        if (merchant.getTotalSales() != null) {
            if (merchant.getTotalSales() < minSales || merchant.getTotalSales() > maxSales) {
                return "总销量超出有效范围[" + minSales + "-" + maxSales + "]";
            }
        }
        
        return null;
    }

    /**
     * 验证菜品数据
     */
    private String validateDish(Dish dish) {
        if (StringUtils.isBlank(dish.getDishName())) {
            return "菜品名称不能为空";
        }
        
        if (dish.getMerchantId() == null || dish.getMerchantId() <= 0) {
            return "商户ID无效";
        }
        
        if (dish.getPrice() == null || dish.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return "菜品价格无效";
        }
        
        if (dish.getPrice().compareTo(BigDecimal.valueOf(minPrice)) < 0 ||
            dish.getPrice().compareTo(BigDecimal.valueOf(maxPrice)) > 0) {
            return "菜品价格超出有效范围[" + minPrice + "-" + maxPrice + "]";
        }
        
        if (dish.getSalesCount() != null) {
            if (dish.getSalesCount() < minSales || dish.getSalesCount() > maxSales) {
                return "销量超出有效范围[" + minSales + "-" + maxSales + "]";
            }
        }
        
        return null;
    }

    /**
     * 验证订单数据
     */
    private String validateOrder(Order order) {
        if (StringUtils.isBlank(order.getOrderNo())) {
            return "订单编号不能为空";
        }
        
        if (order.getUserId() == null || order.getUserId() <= 0) {
            return "用户ID无效";
        }
        
        if (order.getMerchantId() == null || order.getMerchantId() <= 0) {
            return "商户ID无效";
        }
        
        if (order.getActualAmount() == null || order.getActualAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return "订单金额无效";
        }
        
        if (order.getActualAmount().compareTo(BigDecimal.valueOf(maxPrice)) > 0) {
            return "订单金额超出有效范围";
        }
        
        return null;
    }

    /**
     * 处理商户异常值
     */
    private void handleMerchantOutliers(Merchant merchant) {
        // 评分异常值处理
        if (merchant.getRating() != null) {
            if (merchant.getRating().compareTo(BigDecimal.valueOf(minRating)) < 0) {
                merchant.setRating(BigDecimal.valueOf(minRating));
            } else if (merchant.getRating().compareTo(BigDecimal.valueOf(maxRating)) > 0) {
                merchant.setRating(BigDecimal.valueOf(maxRating));
            }
        }
        
        // 销量异常值处理
        if (merchant.getTotalSales() != null) {
            if (merchant.getTotalSales() < minSales) {
                merchant.setTotalSales(minSales);
            } else if (merchant.getTotalSales() > maxSales) {
                merchant.setTotalSales(maxSales);
            }
        }
        
        // 价格异常值处理
        if (merchant.getMinOrderPrice() != null) {
            if (merchant.getMinOrderPrice().compareTo(BigDecimal.valueOf(minPrice)) < 0) {
                merchant.setMinOrderPrice(BigDecimal.valueOf(minPrice));
            } else if (merchant.getMinOrderPrice().compareTo(BigDecimal.valueOf(maxPrice)) > 0) {
                merchant.setMinOrderPrice(BigDecimal.valueOf(maxPrice));
            }
        }
    }

    /**
     * 处理菜品异常值
     */
    private void handleDishOutliers(Dish dish) {
        // 价格异常值处理
        if (dish.getPrice() != null) {
            if (dish.getPrice().compareTo(BigDecimal.valueOf(minPrice)) < 0) {
                dish.setPrice(BigDecimal.valueOf(minPrice));
            } else if (dish.getPrice().compareTo(BigDecimal.valueOf(maxPrice)) > 0) {
                dish.setPrice(BigDecimal.valueOf(maxPrice));
            }
        }
        
        // 销量异常值处理
        if (dish.getSalesCount() != null) {
            if (dish.getSalesCount() < minSales) {
                dish.setSalesCount(minSales);
            } else if (dish.getSalesCount() > maxSales) {
                dish.setSalesCount(maxSales);
            }
        }
    }

    /**
     * 处理订单异常值
     */
    private void handleOrderOutliers(Order order) {
        // 金额异常值处理
        if (order.getActualAmount() != null) {
            if (order.getActualAmount().compareTo(BigDecimal.ZERO) < 0) {
                order.setActualAmount(BigDecimal.ZERO);
            } else if (order.getActualAmount().compareTo(BigDecimal.valueOf(maxPrice)) > 0) {
                order.setActualAmount(BigDecimal.valueOf(maxPrice));
            }
        }
    }
}

