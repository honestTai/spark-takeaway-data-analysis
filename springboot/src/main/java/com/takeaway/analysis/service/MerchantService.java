package com.takeaway.analysis.service;

import com.takeaway.analysis.common.PageResult;
import com.takeaway.analysis.entity.Merchant;
import java.util.List;

public interface MerchantService {
    int saveMerchant(Merchant merchant);
    int batchSaveMerchants(List<Merchant> merchants);
    Merchant getById(Long merchantId);
    List<Merchant> getAll();
    PageResult<Merchant> getPage(int page, int size, String merchantType, String merchantName);
    List<Merchant> getTopBySales(int limit);
    List<Merchant> getByType(String merchantType);
}

