package com.ruoyi.device.service;

import java.util.List;
import com.ruoyi.device.domain.DevAccountPlatform;
import com.ruoyi.device.domain.DevInoutCategory;

public interface IDevCategoryService
{
    // 账号平台
    List<DevAccountPlatform> selectPlatformList();
    int insertPlatform(DevAccountPlatform platform);
    int updatePlatform(DevAccountPlatform platform);
    int deletePlatformById(Long id);

    // 进出库原因
    List<DevInoutCategory> selectInoutCategoryList(DevInoutCategory category);
    int insertInoutCategory(DevInoutCategory category);
    int updateInoutCategory(DevInoutCategory category);
    int deleteInoutCategoryById(Long id);

    // 系统配置
    String getConfig(String key);
    void setConfig(String key, String value);
}
