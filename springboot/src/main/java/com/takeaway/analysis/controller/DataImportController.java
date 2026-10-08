package com.takeaway.analysis.controller;

import com.takeaway.analysis.common.Result;
import com.takeaway.analysis.entity.ImportLog;
import com.takeaway.analysis.service.DataImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 数据导入控制器
 */
@Slf4j
@RestController
@RequestMapping("/import")
public class DataImportController {

    @Autowired
    private DataImportService dataImportService;

    /**
     * 导入商户数据
     */
    @PostMapping("/merchants")
    public Result<ImportLog> importMerchants(@RequestParam("file") MultipartFile file) {
        try {
            ImportLog importLog = dataImportService.importMerchants(file);
            return Result.success(importLog);
        } catch (Exception e) {
            log.error("导入商户数据失败", e);
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    /**
     * 导入菜品数据
     */
    @PostMapping("/dishes")
    public Result<ImportLog> importDishes(@RequestParam("file") MultipartFile file) {
        try {
            ImportLog importLog = dataImportService.importDishes(file);
            return Result.success(importLog);
        } catch (Exception e) {
            log.error("导入菜品数据失败", e);
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    /**
     * 导入订单数据
     */
    @PostMapping("/orders")
    public Result<ImportLog> importOrders(@RequestParam("file") MultipartFile file) {
        try {
            ImportLog importLog = dataImportService.importOrders(file);
            return Result.success(importLog);
        } catch (Exception e) {
            log.error("导入订单数据失败", e);
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有导入日志
     */
    @GetMapping("/logs")
    public Result<List<ImportLog>> getAllImportLogs() {
        try {
            List<ImportLog> logs = dataImportService.getAllImportLogs();
            return Result.success(logs);
        } catch (Exception e) {
            log.error("获取导入日志失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取导入日志详情
     */
    @GetMapping("/logs/{logId}")
    public Result<ImportLog> getImportLog(@PathVariable Long logId) {
        try {
            ImportLog log = dataImportService.getImportLogById(logId);
            return Result.success(log);
        } catch (Exception e) {
            log.error("获取导入日志详情失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}

