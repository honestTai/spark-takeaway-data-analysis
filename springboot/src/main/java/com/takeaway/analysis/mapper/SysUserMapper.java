package com.takeaway.analysis.mapper;

import com.takeaway.analysis.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

@Mapper
public interface SysUserMapper {
    int countAll();
    SysUser selectByUsername(String username);
    SysUser selectById(Long userId);
    int insert(SysUser user);
    int update(SysUser user);
    int updateLastLogin(@Param("userId") Long userId, 
                       @Param("lastLoginTime") LocalDateTime lastLoginTime,
                       @Param("lastLoginIp") String lastLoginIp);
}

