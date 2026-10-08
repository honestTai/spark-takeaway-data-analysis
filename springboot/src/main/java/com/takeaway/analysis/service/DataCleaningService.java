package com.takeaway.analysis.service;

import com.takeaway.analysis.entity.Merchant;
import com.takeaway.analysis.entity.Dish;
import com.takeaway.analysis.entity.Order;
import java.util.List;
import java.util.Map;

/**
 * 数据清洗服务接口
 */
public interface DataCleaningService {
    
    /**
     * 清洗商户数据
     */
    Map<String, Object> cleanMerchants(List<Merchant> merchants);
    
    /**
     * 清洗菜品数据
     */
    Map<String, Object> cleanDishes(List<Dish> dishes);
    
    /**
     * 清洗订单数据
     */
    Map<String, Object> cleanOrders(List<Order> orders);
    
    /**
     * 去重处理
     */
    <T> List<T> deduplicate(List<T> data, String key);
    
    /**
     * 验证数据
     */
    Map<String, Object> validateData(Object data, String dataType);
}

