package com.ruoyi.bitable.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.bitable.domain.BitableApp;
import com.ruoyi.bitable.mapper.BitableAppMapper;
import com.ruoyi.bitable.service.IBitableAppService;

/**
 * 多维表格应用Service实现
 */
@Service
public class BitableAppServiceImpl implements IBitableAppService
{
    @Autowired
    private BitableAppMapper appMapper;

    @Override
    public BitableApp selectAppById(Long id)
    {
        return appMapper.selectAppById(id);
    }

    @Override
    public BitableApp selectAppByToken(String appToken)
    {
        return appMapper.selectAppByToken(appToken);
    }

    @Override
    public List<BitableApp> selectAppList(BitableApp app)
    {
        return appMapper.selectAppList(app);
    }

    /** 安全获取当前用户名，匿名接口时返回null */
    private String safeGetUsername()
    {
        try
        {
            return com.ruoyi.common.utils.SecurityUtils.getUsername();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public int insertApp(BitableApp app)
    {
        // 自动生成 app_token: basc + 14位随机字符
        app.setAppToken("basc" + IdUtils.fastSimpleUUID().substring(0, 14));
        // 自动生成 api_key: ak_ + 20位随机字符
        app.setApiKey("ak_" + IdUtils.fastSimpleUUID().substring(0, 20));
        app.setCreateBy(safeGetUsername());
        app.setCreateTime(DateUtils.getNowDate());
        return appMapper.insertApp(app);
    }

    @Override
    public int updateApp(BitableApp app)
    {
        app.setUpdateBy(safeGetUsername());
        app.setUpdateTime(DateUtils.getNowDate());
        return appMapper.updateApp(app);
    }

    @Override
    public int deleteAppById(Long id)
    {
        return appMapper.deleteAppById(id);
    }
}
