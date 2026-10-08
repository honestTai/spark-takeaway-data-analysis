package com.takeaway.analysis.mapper;

import com.takeaway.analysis.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface OrderItemMapper {
    int insert(OrderItem orderItem);
    List<OrderItem> selectByOrderId(Long orderId);
    int batchInsert(List<OrderItem> orderItems);
}

