package com.takeaway.analysis.controller;

import com.takeaway.analysis.entity.Dish;
import com.takeaway.analysis.entity.Merchant;
import com.takeaway.analysis.service.CrawlerService;
import com.takeaway.analysis.service.DishService;
import com.takeaway.analysis.service.MerchantService;
import com.takeaway.analysis.service.DataCleaningService;
import com.takeaway.analysis.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 爬虫控制器
 */
@Slf4j
@RestController
@RequestMapping("/crawler")
public class CrawlerController {

    @Autowired
    private CrawlerService crawlerService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private DishService dishService;

    @Autowired
    private DataCleaningService dataCleaningService;

    /**
     * 爬取商户信息
     */
    @PostMapping("/merchants")
    public Result<List<Merchant>> crawlMerchants(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            // 1. 爬取原始数据
            List<Merchant> merchants = crawlerService.crawlMerchants(keyword, pageSize);
            log.info("爬取到{}条商户数据，开始数据清洗", merchants.size());
            
            // 2. 数据清洗
            Map<String, Object> cleaningResult = dataCleaningService.cleanMerchants(merchants);
            @SuppressWarnings("unchecked")
            List<Merchant> cleanedMerchants = (List<Merchant>) cleaningResult.get("cleanedData");
            int duplicateCount = (Integer) cleaningResult.get("duplicateCount");
            int invalidCount = (Integer) cleaningResult.get("invalidCount");
            
            log.info("数据清洗完成，原始：{}条，清洗后：{}条，去重：{}条，无效：{}条", 
                    merchants.size(), cleanedMerchants.size(), duplicateCount, invalidCount);
            
            // 3. 保存清洗后的数据
            if (!cleanedMerchants.isEmpty()) {
                merchantService.batchSaveMerchants(cleanedMerchants);
                log.info("成功保存{}条商户数据", cleanedMerchants.size());
            }
            
            return Result.success(cleanedMerchants);
        } catch (Exception e) {
            log.error("爬取商户信息失败", e);
            return Result.error("爬取失败：" + e.getMessage());
        }
    }

    /**
     * 爬取菜品信息
     */
    @PostMapping("/dishes/{merchantId}")
    public Result<List<Dish>> crawlDishes(@PathVariable Long merchantId) {
        try {
            // 1. 爬取原始数据
            List<Dish> dishes = crawlerService.crawlDishes(merchantId);
            log.info("爬取到{}条菜品数据，开始数据清洗", dishes.size());
            
            // 2. 数据清洗
            Map<String, Object> cleaningResult = dataCleaningService.cleanDishes(dishes);
            @SuppressWarnings("unchecked")
            List<Dish> cleanedDishes = (List<Dish>) cleaningResult.get("cleanedData");
            int duplicateCount = (Integer) cleaningResult.get("duplicateCount");
            int invalidCount = (Integer) cleaningResult.get("invalidCount");
            
            log.info("数据清洗完成，原始：{}条，清洗后：{}条，去重：{}条，无效：{}条", 
                    dishes.size(), cleanedDishes.size(), duplicateCount, invalidCount);
            
            // 3. 保存清洗后的数据
            if (!cleanedDishes.isEmpty()) {
                dishService.batchSaveDishes(cleanedDishes);
                log.info("成功保存{}条菜品数据", cleanedDishes.size());
            }
            
            return Result.success(cleanedDishes);
        } catch (Exception e) {
            log.error("爬取菜品信息失败", e);
            return Result.error("爬取失败：" + e.getMessage());
        }
    }

    /**
     * 批量爬取数据
     */
    @PostMapping("/batch")
    public Result<String> batchCrawl(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int merchantLimit) {
        try {
            crawlerService.batchCrawlData(keyword, merchantLimit);
            return Result.success("批量爬取任务已启动");
        } catch (Exception e) {
            log.error("批量爬取失败", e);
            return Result.error("批量爬取失败：" + e.getMessage());
        }
    }
}

