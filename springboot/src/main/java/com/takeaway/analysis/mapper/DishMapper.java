package com.takeaway.analysis.mapper;

import com.takeaway.analysis.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DishMapper {
    int insert(Dish dish);
    int update(Dish dish);
    Dish selectById(Long dishId);
    List<Dish> selectByMerchantId(Long merchantId);
    List<Dish> selectTopBySales(@Param("limit") int limit);
    List<Dish> selectByCategory(String category);
    int countAll();
    int deleteById(Long dishId);
    int batchInsert(List<Dish> dishes);
    
    /**
     * 根据商户ID和菜品名称查询（用于去重检查）
     */
    Dish selectByMerchantIdAndName(@Param("merchantId") Long merchantId, @Param("dishName") String dishName);
}

