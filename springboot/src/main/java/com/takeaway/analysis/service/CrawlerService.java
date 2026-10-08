package com.takeaway.analysis.service;

import com.takeaway.analysis.entity.Dish;
import com.takeaway.analysis.entity.Merchant;
import java.util.List;

/**
 * 爬虫服务接口
 */
public interface CrawlerService {
    
    /**
     * 爬取美团商户信息
     * @param keyword 搜索关键词（如：地区、商圈）
     * @param pageSize 每页数量
     * @return 商户列表
     */
    List<Merchant> crawlMerchants(String keyword, int pageSize);
    
    /**
     * 爬取指定商户的菜品信息
     * @param merchantId 商户ID
     * @return 菜品列表
     */
    List<Dish> crawlDishes(Long merchantId);
    
    /**
     * 批量爬取商户和菜品数据
     * @param keyword 搜索关键词
     * @param merchantLimit 商户数量限制
     */
    void batchCrawlData(String keyword, int merchantLimit);
}

