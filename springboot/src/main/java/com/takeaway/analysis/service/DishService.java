package com.takeaway.analysis.service;

import com.takeaway.analysis.entity.Dish;
import java.util.List;

public interface DishService {
    int saveDish(Dish dish);
    int batchSaveDishes(List<Dish> dishes);
    Dish getById(Long dishId);
    List<Dish> getByMerchantId(Long merchantId);
    List<Dish> getTopBySales(int limit);
    List<Dish> getByCategory(String category);
}

