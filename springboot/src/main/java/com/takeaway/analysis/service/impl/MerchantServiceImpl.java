package com.takeaway.analysis.service.impl;

import com.takeaway.analysis.common.PageResult;
import com.takeaway.analysis.entity.Merchant;
import com.takeaway.analysis.mapper.MerchantMapper;
import com.takeaway.analysis.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private MerchantMapper merchantMapper;

    @Override
    @Transactional
    public int saveMerchant(Merchant merchant) {
        return merchantMapper.insert(merchant);
    }

    @Override
    @Transactional
    public int batchSaveMerchants(List<Merchant> merchants) {
        if (merchants == null || merchants.isEmpty()) {
            return 0;
        }
        return merchantMapper.batchInsert(merchants);
    }

    @Override
    public Merchant getById(Long merchantId) {
        return merchantMapper.selectById(merchantId);
    }

    @Override
    public List<Merchant> getAll() {
        return merchantMapper.selectAll();
    }

    @Override
    public PageResult<Merchant> getPage(int page, int size, String merchantType, String merchantName) {
        int offset = (page - 1) * size;
        List<Merchant> list = merchantMapper.selectPage(offset, size, merchantType, merchantName);
        int total = merchantMapper.selectCount(merchantType, merchantName);
        return PageResult.of(list, total, page, size);
    }

    @Override
    public List<Merchant> getTopBySales(int limit) {
        return merchantMapper.selectTopBySales(limit);
    }

    @Override
    public List<Merchant> getByType(String merchantType) {
        return merchantMapper.selectByType(merchantType);
    }
}

