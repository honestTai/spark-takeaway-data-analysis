package com.takeaway.analysis.controller;

import com.takeaway.analysis.common.Result;
import com.takeaway.analysis.mapper.DishMapper;
import com.takeaway.analysis.mapper.MerchantMapper;
import com.takeaway.analysis.mapper.OrderMapper;
import com.takeaway.analysis.entity.Merchant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private DishMapper dishMapper;

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("merchantCount", merchantMapper.countAll());
        stats.put("orderCount", orderMapper.countAll());
        stats.put("dishCount", dishMapper.countAll());
        stats.put("userCount", orderMapper.countDistinctUserCount());
        return Result.success(stats);
    }

    @GetMapping("/merchant-type-dist")
    public Result<List<Map<String, Object>>> getMerchantTypeDist() {
        List<Map<String, Object>> dist = merchantMapper.countByType();
        return Result.success(dist);
    }

    @GetMapping("/top-merchants")
    public Result<List<Merchant>> getTopMerchants(
            @RequestParam(defaultValue = "10") int limit) {
        List<Merchant> merchants = merchantMapper.selectTopBySales(limit);
        return Result.success(merchants);
    }
}
