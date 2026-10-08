package com.takeaway.analysis.controller;

import com.takeaway.analysis.common.PageResult;
import com.takeaway.analysis.common.Result;
import com.takeaway.analysis.entity.Merchant;
import com.takeaway.analysis.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/merchants")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @GetMapping("/{id}")
    public Result<Merchant> getById(@PathVariable Long id) {
        Merchant merchant = merchantService.getById(id);
        return Result.success(merchant);
    }

    @GetMapping
    public Result<PageResult<Merchant>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String merchantType,
            @RequestParam(required = false) String merchantName) {
        PageResult<Merchant> pageResult = merchantService.getPage(page, size, merchantType, merchantName);
        return Result.success(pageResult);
    }

    @GetMapping("/top")
    public Result<List<Merchant>> getTopBySales(@RequestParam(defaultValue = "10") int limit) {
        List<Merchant> merchants = merchantService.getTopBySales(limit);
        return Result.success(merchants);
    }

    @GetMapping("/type/{type}")
    public Result<List<Merchant>> getByType(@PathVariable String type) {
        List<Merchant> merchants = merchantService.getByType(type);
        return Result.success(merchants);
    }
}

