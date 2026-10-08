package com.takeaway.analysis.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.takeaway.analysis.entity.*;
import com.takeaway.analysis.mapper.*;
import com.takeaway.analysis.service.DataImportService;
import com.takeaway.analysis.service.DataCleaningService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DataImportServiceImpl implements DataImportService {

    @Autowired
    private ImportLogMapper importLogMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private DataCleaningService dataCleaningService;

    @Override
    @Transactional
    public ImportLog importMerchants(MultipartFile file) {
        ImportLog importLog = createImportLog("merchant", file.getOriginalFilename());
        
        try {
            List<Merchant> merchants = parseMerchantsFromFile(file);
            
            // 数据清洗
            Map<String, Object> cleaningResult = dataCleaningService.cleanMerchants(merchants);
            @SuppressWarnings("unchecked")
            List<Merchant> cleanedMerchants = (List<Merchant>) cleaningResult.get("cleanedData");
            int duplicateCount = (Integer) cleaningResult.get("duplicateCount");
            int invalidCount = (Integer) cleaningResult.get("invalidCount");
            
            int successCount = 0;
            int failCount = 0;
            
            for (Merchant merchant : cleanedMerchants) {
                try {
                    merchantMapper.insert(merchant);
                    successCount++;
                } catch (Exception e) {
                    log.error("导入商户失败：{}", merchant.getMerchantName(), e);
                    failCount++;
                }
            }
            
            importLog.setTotalCount(merchants.size());
            importLog.setSuccessCount(successCount);
            importLog.setFailCount(failCount + invalidCount);
            
            // 记录清洗信息
            String cleaningInfo = String.format("去重：%d条，无效：%d条", duplicateCount, invalidCount);
            if (importLog.getErrorMessage() != null) {
                importLog.setErrorMessage(importLog.getErrorMessage() + "；" + cleaningInfo);
            } else {
                importLog.setErrorMessage(cleaningInfo);
            }
            
            importLog.setImportStatus(2); // 成功
        } catch (Exception e) {
            log.error("导入商户数据失败", e);
            importLog.setImportStatus(3); // 失败
            importLog.setErrorMessage(e.getMessage());
        } finally {
            importLog.setEndTime(LocalDateTime.now());
            importLogMapper.update(importLog);
        }
        
        return importLog;
    }

    @Override
    @Transactional
    public ImportLog importDishes(MultipartFile file) {
        ImportLog importLog = createImportLog("dish", file.getOriginalFilename());
        
        try {
            List<Dish> dishes = parseDishesFromFile(file);
            
            // 数据清洗
            Map<String, Object> cleaningResult = dataCleaningService.cleanDishes(dishes);
            @SuppressWarnings("unchecked")
            List<Dish> cleanedDishes = (List<Dish>) cleaningResult.get("cleanedData");
            int duplicateCount = (Integer) cleaningResult.get("duplicateCount");
            int invalidCount = (Integer) cleaningResult.get("invalidCount");
            
            int successCount = 0;
            int failCount = 0;
            
            for (Dish dish : cleanedDishes) {
                try {
                    dishMapper.insert(dish);
                    successCount++;
                } catch (Exception e) {
                    log.error("导入菜品失败：{}", dish.getDishName(), e);
                    failCount++;
                }
            }
            
            importLog.setTotalCount(dishes.size());
            importLog.setSuccessCount(successCount);
            importLog.setFailCount(failCount + invalidCount);
            
            // 记录清洗信息
            String cleaningInfo = String.format("去重：%d条，无效：%d条", duplicateCount, invalidCount);
            if (importLog.getErrorMessage() != null) {
                importLog.setErrorMessage(importLog.getErrorMessage() + "；" + cleaningInfo);
            } else {
                importLog.setErrorMessage(cleaningInfo);
            }
            
            importLog.setImportStatus(2);
        } catch (Exception e) {
            log.error("导入菜品数据失败", e);
            importLog.setImportStatus(3);
            importLog.setErrorMessage(e.getMessage());
        } finally {
            importLog.setEndTime(LocalDateTime.now());
            importLogMapper.update(importLog);
        }
        
        return importLog;
    }

    @Override
    @Transactional
    public ImportLog importOrders(MultipartFile file) {
        ImportLog importLog = createImportLog("order", file.getOriginalFilename());
        
        try {
            List<Order> orders = parseOrdersFromFile(file);
            
            // 数据清洗
            Map<String, Object> cleaningResult = dataCleaningService.cleanOrders(orders);
            @SuppressWarnings("unchecked")
            List<Order> cleanedOrders = (List<Order>) cleaningResult.get("cleanedData");
            int duplicateCount = (Integer) cleaningResult.get("duplicateCount");
            int invalidCount = (Integer) cleaningResult.get("invalidCount");
            
            int successCount = 0;
            int failCount = 0;
            
            for (Order order : cleanedOrders) {
                try {
                    orderMapper.insert(order);
                    successCount++;
                } catch (Exception e) {
                    log.error("导入订单失败：{}", order.getOrderNo(), e);
                    failCount++;
                }
            }
            
            importLog.setTotalCount(orders.size());
            importLog.setSuccessCount(successCount);
            importLog.setFailCount(failCount + invalidCount);
            
            // 记录清洗信息
            String cleaningInfo = String.format("去重：%d条，无效：%d条", duplicateCount, invalidCount);
            if (importLog.getErrorMessage() != null) {
                importLog.setErrorMessage(importLog.getErrorMessage() + "；" + cleaningInfo);
            } else {
                importLog.setErrorMessage(cleaningInfo);
            }
            
            importLog.setImportStatus(2);
        } catch (Exception e) {
            log.error("导入订单数据失败", e);
            importLog.setImportStatus(3);
            importLog.setErrorMessage(e.getMessage());
        } finally {
            importLog.setEndTime(LocalDateTime.now());
            importLogMapper.update(importLog);
        }
        
        return importLog;
    }

    @Override
    public List<ImportLog> getAllImportLogs() {
        return importLogMapper.selectAll();
    }

    @Override
    public ImportLog getImportLogById(Long logId) {
        return importLogMapper.selectById(logId);
    }

    private ImportLog createImportLog(String importType, String fileName) {
        ImportLog importLog = new ImportLog();
        importLog.setImportType(importType);
        importLog.setFileName(fileName);
        importLog.setImportStatus(1); // 进行中
        importLog.setStartTime(LocalDateTime.now());
        importLogMapper.insert(importLog);
        return importLog;
    }

    private List<Merchant> parseMerchantsFromFile(MultipartFile file) throws Exception {
        List<Merchant> merchants = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // 跳过表头
                }
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                
                // 解析CSV或JSON格式
                if (line.trim().startsWith("{")) {
                    // JSON格式
                    Merchant merchant = JSON.parseObject(line, Merchant.class);
                    merchants.add(merchant);
                } else {
                    // CSV格式
                    String[] fields = line.split(",");
                    if (fields.length >= 5) {
                        Merchant merchant = new Merchant();
                        merchant.setMerchantName(fields[0]);
                        merchant.setMerchantType(fields[1]);
                        if (StringUtils.isNotBlank(fields[2])) {
                            merchant.setRating(new java.math.BigDecimal(fields[2]));
                        }
                        if (StringUtils.isNotBlank(fields[3])) {
                            merchant.setTotalSales(Integer.parseInt(fields[3]));
                        }
                        if (StringUtils.isNotBlank(fields[4])) {
                            merchant.setMinOrderPrice(new java.math.BigDecimal(fields[4]));
                        }
                        merchant.setStatus(1);
                        merchants.add(merchant);
                    }
                }
            }
        }
        return merchants;
    }

    private List<Dish> parseDishesFromFile(MultipartFile file) throws Exception {
        List<Dish> dishes = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                
                if (line.trim().startsWith("{")) {
                    Dish dish = JSON.parseObject(line, Dish.class);
                    dishes.add(dish);
                } else {
                    String[] fields = line.split(",");
                    if (fields.length >= 5) {
                        Dish dish = new Dish();
                        dish.setMerchantId(Long.parseLong(fields[0]));
                        dish.setDishName(fields[1]);
                        dish.setCategory(fields[2]);
                        dish.setPrice(new java.math.BigDecimal(fields[3]));
                        if (StringUtils.isNotBlank(fields[4])) {
                            dish.setSalesCount(Integer.parseInt(fields[4]));
                        }
                        dish.setStatus(1);
                        dishes.add(dish);
                    }
                }
            }
        }
        return dishes;
    }

    private List<Order> parseOrdersFromFile(MultipartFile file) throws Exception {
        List<Order> orders = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                
                if (line.trim().startsWith("{")) {
                    Order order = JSON.parseObject(line, Order.class);
                    orders.add(order);
                }
            }
        }
        return orders;
    }
}

