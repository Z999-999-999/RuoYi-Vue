package com.ruoyi.bitable.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.bitable.domain.BitableTable;
import com.ruoyi.bitable.mapper.BitableTableMapper;
import com.ruoyi.bitable.service.IBitableFieldService;
import com.ruoyi.bitable.service.IBitableRecordService;
import com.ruoyi.bitable.service.IBitableTableService;

/**
 * 多维表格-数据表Service实现
 */
@Service
public class BitableTableServiceImpl implements IBitableTableService
{
    @Autowired
    private BitableTableMapper tableMapper;

    @Autowired
    private IBitableRecordService recordService;

    @Autowired
    private IBitableFieldService fieldService;

    @Override
    public BitableTable selectTableById(Long id)
    {
        return tableMapper.selectTableById(id);
    }

    @Override
    public BitableTable selectTableByIdForDelete(Long id)
    {
        return tableMapper.selectTableByIdForDelete(id);
    }

    @Override
    public BitableTable selectTableByTokenAndId(String appToken, String tableId)
    {
        return tableMapper.selectTableByTokenAndId(appToken, tableId);
    }

    @Override
    public BitableTable selectDeletedTableByTokenAndId(String appToken, String tableId)
    {
        return tableMapper.selectDeletedTableByTokenAndId(appToken, tableId);
    }

    @Override
    public int restoreTableById(Long id)
    {
        return tableMapper.restoreTableById(id);
    }

    @Override
    public List<BitableTable> selectTableList(String appToken)
    {
        return tableMapper.selectTableList(appToken);
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
    public int insertTable(BitableTable table)
    {
        // 如果table_id为空，自动生成: tbls + 10位随机字符
        if (table.getTableId() == null || table.getTableId().isEmpty())
        {
            table.setTableId("tbls" + IdUtils.fastSimpleUUID().substring(0, 10));
        }
        // 如果createBy为空（非显式设置），使用当前登录用户
        if (table.getCreateBy() == null || table.getCreateBy().isEmpty())
        {
            table.setCreateBy(safeGetUsername());
        }
        if (table.getCreateTime() == null)
        {
            table.setCreateTime(DateUtils.getNowDate());
        }
        return tableMapper.insertTable(table);
    }

    @Override
    public int updateTable(BitableTable table)
    {
        if (table.getUpdateBy() == null || table.getUpdateBy().isEmpty())
        {
            table.setUpdateBy(safeGetUsername());
        }
        table.setUpdateTime(DateUtils.getNowDate());
        return tableMapper.updateTable(table);
    }

    @Override
    public int deleteTableById(Long id)
    {
        // 使用不带 del_flag 过滤的查询，确保软删除的表也能级联清理字段和记录
        BitableTable table = tableMapper.selectTableByIdForDelete(id);
        if (table != null)
        {
            recordService.deleteRecordsByTable(table.getAppToken(), table.getTableId());
            fieldService.deleteFieldsByTable(table.getAppToken(), table.getTableId());
        }
        return tableMapper.deleteTableById(id);
    }

    @Override
    public int deleteTablesByAppToken(String appToken)
    {
        return tableMapper.deleteTablesByAppToken(appToken);
    }
}
