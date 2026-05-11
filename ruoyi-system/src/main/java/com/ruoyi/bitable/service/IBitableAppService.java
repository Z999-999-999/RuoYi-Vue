package com.ruoyi.bitable.service;

import java.util.List;
import com.ruoyi.bitable.domain.BitableApp;

/**
 * 多维表格应用Service接口
 */
public interface IBitableAppService
{
    public BitableApp selectAppById(Long id);
    public BitableApp selectAppByToken(String appToken);
    public List<BitableApp> selectAppList(BitableApp app);
    public int insertApp(BitableApp app);
    public int updateApp(BitableApp app);
    public int deleteAppById(Long id);
}
