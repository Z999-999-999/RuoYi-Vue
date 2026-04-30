package com.ruoyi.device.mapper;

import java.util.List;
import com.ruoyi.device.domain.DevAccountPlatform;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DevAccountPlatformMapper
{
    List<DevAccountPlatform> selectPlatformList(DevAccountPlatform platform);

    DevAccountPlatform selectPlatformById(Long id);

    DevAccountPlatform selectPlatformByCode(String code);

    int insertPlatform(DevAccountPlatform platform);

    int updatePlatform(DevAccountPlatform platform);

    int deletePlatformById(Long id);

    int countReportsWithPlatformInStats(Long platformId);
}
