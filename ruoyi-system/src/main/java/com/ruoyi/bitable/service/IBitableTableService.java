package com.ruoyi.bitable.service;

import java.util.List;
import com.ruoyi.bitable.domain.BitableTable;

/**
 * 多维表格-数据表Service接口
 */
public interface IBitableTableService
{
    public BitableTable selectTableById(Long id);
    public BitableTable selectTableByIdForDelete(Long id);
    public BitableTable selectTableByTokenAndId(String appToken, String tableId);
    public BitableTable selectDeletedTableByTokenAndId(String appToken, String tableId);
    public int restoreTableById(Long id);
    public List<BitableTable> selectTableList(String appToken);
    public int insertTable(BitableTable table);
    public int updateTable(BitableTable table);
    public int deleteTableById(Long id);
    public int deleteTablesByAppToken(String appToken);
}
