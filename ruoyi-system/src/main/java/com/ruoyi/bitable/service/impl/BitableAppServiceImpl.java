package com.ruoyi.bitable.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.AesUtils;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.bitable.domain.BitableApp;
import com.ruoyi.bitable.domain.BitableTable;
import com.ruoyi.bitable.mapper.BitableAppMapper;
import com.ruoyi.bitable.mapper.BitableTableMapper;
import com.ruoyi.bitable.service.IBitableAppService;
import com.ruoyi.bitable.service.IBitableFieldService;
import com.ruoyi.bitable.service.IBitableRecordService;
import com.ruoyi.bitable.service.IBitableTableService;

/**
 * 多维表格应用Service实现
 */
@Service
public class BitableAppServiceImpl implements IBitableAppService
{
    @Value("${aes.secretKey}")
    private String aesSecretKey;

    @Autowired
    private BitableAppMapper appMapper;

    @Autowired
    private IBitableTableService tableService;

    @Autowired
    private IBitableFieldService fieldService;

    @Autowired
    private IBitableRecordService recordService;

    /** 解密 API Key（加密存储的自动解密，未加密的原样返回） */
    private void decryptApiKey(BitableApp app)
    {
        if (app != null && app.getApiKey() != null && AesUtils.isEncrypted(app.getApiKey()))
        {
            try { app.setApiKey(AesUtils.decrypt(app.getApiKey(), aesSecretKey)); } catch (Exception e) { /* 解密失败保留原值 */ }
        }
    }

    /** 批量解密 API Key */
    private void decryptApiKeys(List<BitableApp> apps)
    {
        if (apps != null) apps.forEach(this::decryptApiKey);
    }

    @Override
    public BitableApp selectAppById(Long id)
    {
        BitableApp app = appMapper.selectAppById(id);
        decryptApiKey(app);
        return app;
    }

    @Override
    public BitableApp selectAppByToken(String appToken)
    {
        BitableApp app = appMapper.selectAppByToken(appToken);
        // 注意：selectAppByToken 用于上报接口验证，不解密（直接与加密值比对）
        return app;
    }

    @Override
    public List<BitableApp> selectAppList(BitableApp app)
    {
        List<BitableApp> list = appMapper.selectAppList(app);
        decryptApiKeys(list);
        return list;
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
        // 自动生成 api_key: ak_ + 20位随机字符，加密存储
        String rawApiKey = "ak_" + IdUtils.fastSimpleUUID().substring(0, 20);
        app.setApiKey(AesUtils.encrypt(rawApiKey, aesSecretKey));
        app.setCreateBy(safeGetUsername());
        app.setCreateTime(DateUtils.getNowDate());
        return appMapper.insertApp(app);
    }

    @Override
    public int updateApp(BitableApp app)
    {
        // 如果前端传了新的 apiKey（明文），加密后再存储
        if (app.getApiKey() != null && app.getApiKey().startsWith("ak_"))
        {
            app.setApiKey(AesUtils.encrypt(app.getApiKey(), aesSecretKey));
        }
        app.setUpdateBy(safeGetUsername());
        app.setUpdateTime(DateUtils.getNowDate());
        return appMapper.updateApp(app);
    }

    @Override
    public int resetApiKey(Long id)
    {
        BitableApp app = appMapper.selectAppById(id);
        if (app == null) return 0;
        String rawApiKey = "ak_" + IdUtils.fastSimpleUUID().substring(0, 20);
        app.setApiKey(AesUtils.encrypt(rawApiKey, aesSecretKey));
        app.setUpdateBy(safeGetUsername());
        app.setUpdateTime(DateUtils.getNowDate());
        return appMapper.updateApp(app);
    }

    @Override
    public int deleteAppById(Long id)
    {
        // 级联硬删除：先查所有关联表，逐个硬删 record + field + table，最后删 app
        BitableApp app = appMapper.selectAppById(id);
        if (app != null)
        {
            List<BitableTable> tables = tableService.selectTableList(app.getAppToken());
            for (BitableTable table : tables)
            {
                recordService.deleteRecordsByTable(table.getAppToken(), table.getTableId());
                fieldService.deleteFieldsByTable(table.getAppToken(), table.getTableId());
            }
            // 硬删所有表
            tableService.deleteTablesByAppToken(app.getAppToken());
        }
        return appMapper.deleteAppById(id);
    }
}
