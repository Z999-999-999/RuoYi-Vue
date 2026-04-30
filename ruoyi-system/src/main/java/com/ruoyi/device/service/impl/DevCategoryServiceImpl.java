package com.ruoyi.device.service.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.device.domain.DevAccountPlatform;
import com.ruoyi.device.domain.DevInoutCategory;
import com.ruoyi.device.mapper.DevAccountPlatformMapper;
import com.ruoyi.device.mapper.DevConfigMapper;
import com.ruoyi.device.mapper.DevInoutCategoryMapper;
import com.ruoyi.device.mapper.DevInoutRecordMapper;
import com.ruoyi.device.service.IDevCategoryService;

@Service
public class DevCategoryServiceImpl implements IDevCategoryService
{
    /** 允许写入的配置 key 白名单 */
    private static final Set<String> ALLOWED_CONFIG_KEYS = new HashSet<>(
        Arrays.asList("company_phone_total"));

    @Autowired
    private DevAccountPlatformMapper platformMapper;

    @Autowired
    private DevInoutCategoryMapper inoutCategoryMapper;

    @Autowired
    private DevInoutRecordMapper recordMapper;

    @Autowired
    private DevConfigMapper configMapper;

    @Override
    public List<DevAccountPlatform> selectPlatformList()
    {
        return platformMapper.selectPlatformList(new DevAccountPlatform());
    }

    @Override
    public int insertPlatform(DevAccountPlatform platform)
    {
        platform.setCreateBy(SecurityUtils.getUsername());
        platform.setCreateTime(DateUtils.getNowDate());
        return platformMapper.insertPlatform(platform);
    }

    @Override
    public int updatePlatform(DevAccountPlatform platform)
    {
        platform.setUpdateBy(SecurityUtils.getUsername());
        platform.setUpdateTime(DateUtils.getNowDate());
        return platformMapper.updatePlatform(platform);
    }

    @Override
    public int deletePlatformById(Long id)
    {
        // 检查是否有日报引用了该平台（platform_stats JSON 中包含该平台 ID）
        int refCount = platformMapper.countReportsWithPlatformInStats(id);
        if (refCount > 0)
        {
            throw new ServiceException("该平台已在 " + refCount + " 条每日汇报的统计中引用，无法删除");
        }
        return platformMapper.deletePlatformById(id);
    }

    @Override
    public List<DevInoutCategory> selectInoutCategoryList(DevInoutCategory category)
    {
        return inoutCategoryMapper.selectCategoryList(category);
    }

    @Override
    public int insertInoutCategory(DevInoutCategory category)
    {
        category.setCreateBy(SecurityUtils.getUsername());
        category.setCreateTime(DateUtils.getNowDate());
        return inoutCategoryMapper.insertCategory(category);
    }

    @Override
    public int updateInoutCategory(DevInoutCategory category)
    {
        category.setUpdateBy(SecurityUtils.getUsername());
        category.setUpdateTime(DateUtils.getNowDate());
        return inoutCategoryMapper.updateCategory(category);
    }

    @Override
    public int deleteInoutCategoryById(Long id)
    {
        // 检查是否有进出库记录引用了该分类
        int refCount = recordMapper.countByInoutCategoryId(id);
        if (refCount > 0)
        {
            throw new ServiceException("该进出库原因已被 " + refCount + " 条记录引用，无法删除");
        }
        return inoutCategoryMapper.deleteCategoryById(id);
    }

    @Override
    public String getConfig(String key)
    {
        return configMapper.selectValueByKey(key);
    }

    @Override
    public void setConfig(String key, String value)
    {
        if (key == null || !ALLOWED_CONFIG_KEYS.contains(key))
        {
            throw new ServiceException("不允许修改配置项: " + key);
        }
        configMapper.upsertConfig(key, value);
    }
}
