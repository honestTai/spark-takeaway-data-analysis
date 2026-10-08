package com.takeaway.analysis.mapper;

import com.takeaway.analysis.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface MerchantMapper {
    int insert(Merchant merchant);
    int update(Merchant merchant);
    Merchant selectById(Long merchantId);
    List<Merchant> selectAll();
    int countAll();
    List<Map<String, Object>> countByType();
    List<Merchant> selectPage(@Param("offset") int offset,
                              @Param("pageSize") int pageSize,
                              @Param("merchantType") String merchantType,
                              @Param("merchantName") String merchantName);
    int selectCount(@Param("merchantType") String merchantType,
                    @Param("merchantName") String merchantName);
    List<Merchant> selectByType(String merchantType);
    List<Merchant> selectTopBySales(@Param("limit") int limit);
    int deleteById(Long merchantId);
    int batchInsert(List<Merchant> merchants);
    
    Merchant selectByName(@Param("merchantName") String merchantName);
    
    List<String> selectAllNames();

    /** 评分与销量关系（直接 SQL，不走 Spark） */
    List<Map<String, Object>> selectRatingVsSales(@Param("limit") int limit);

    /** 商户类型对比聚合（直接 SQL，不走 Spark） */
    List<Map<String, Object>> selectTypeComparison();
}

