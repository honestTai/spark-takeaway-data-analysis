package com.takeaway.analysis.service.impl;

import com.takeaway.analysis.entity.Dish;
import com.takeaway.analysis.mapper.DishMapper;
import com.takeaway.analysis.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Override
    @Transactional
    public int saveDish(Dish dish) {
        return dishMapper.insert(dish);
    }

    @Override
    @Transactional
    public int batchSaveDishes(List<Dish> dishes) {
        if (dishes == null || dishes.isEmpty()) {
            return 0;
        }
        return dishMapper.batchInsert(dishes);
    }

    @Override
    public Dish getById(Long dishId) {
        return dishMapper.selectById(dishId);
    }

    @Override
    public List<Dish> getByMerchantId(Long merchantId) {
        return dishMapper.selectByMerchantId(merchantId);
    }

    @Override
    public List<Dish> getTopBySales(int limit) {
        return dishMapper.selectTopBySales(limit);
    }

    @Override
    public List<Dish> getByCategory(String category) {
        return dishMapper.selectByCategory(category);
    }
}

