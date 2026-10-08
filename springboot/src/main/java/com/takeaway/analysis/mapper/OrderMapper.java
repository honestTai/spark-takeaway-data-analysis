package com.takeaway.analysis.mapper;

import com.takeaway.analysis.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    int insert(Order order);
    int update(Order order);
    Order selectById(Long orderId);
    Order selectByOrderNo(String orderNo);
    List<Order> selectByUserId(Long userId);
    List<Order> selectByMerchantId(Long merchantId);
    List<Order> selectByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                  @Param("endTime") LocalDateTime endTime);
    List<Map<String, Object>> selectPriceDistribution();
    int countAll();
    int countDistinctUserCount();
    int countByTimeRange(@Param("startTime") LocalDateTime startTime, 
                        @Param("endTime") LocalDateTime endTime);
    int batchInsert(List<Order> orders);
}

