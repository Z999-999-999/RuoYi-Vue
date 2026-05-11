package com.ruoyi.bitable.mapper;

import java.util.List;
import com.ruoyi.bitable.domain.BitableTable;

/**
 * 多维表格-数据表Mapper接口
 */
public interface BitableTableMapper
{
    public BitableTable selectTableById(Long id);
    public BitableTable selectTableByTokenAndId(String appToken, String tableId);
    public BitableTable selectDeletedTableByTokenAndId(String appToken, String tableId);
    public int restoreTableById(Long id);
    public List<BitableTable> selectTableList(String appToken);
    public int insertTable(BitableTable table);
    public int updateTable(BitableTable table);
    public int deleteTableById(Long id);
    public int deleteTableByTokenAndId(String appToken, String tableId);
}
