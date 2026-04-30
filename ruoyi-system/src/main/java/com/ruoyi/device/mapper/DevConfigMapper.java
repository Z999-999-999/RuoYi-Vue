package com.ruoyi.device.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DevConfigMapper
{
    String selectValueByKey(@Param("key") String key);
    void upsertConfig(@Param("key") String key, @Param("value") String value);
}
