package com.ruoyi.bitable.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.bitable.domain.BitableRecord;

/**
 * 多维表格-记录Mapper接口
 */
public interface BitableRecordMapper
{
    public BitableRecord selectRecordById(Long id);
    public BitableRecord selectRecordByTokenTableRecordId(String appToken, String tableId, String recordId);
    public List<BitableRecord> selectRecordList(BitableRecord query);
    public int countRecords(String appToken, String tableId);
    public int insertRecord(BitableRecord record);
    public int insertRecords(List<BitableRecord> records);
    public int updateRecord(BitableRecord record);
    public int deleteRecordById(Long id);
    public int deleteRecordByTokenTableRecordId(String appToken, String tableId, String recordId);
    public int deleteRecordsByTable(String appToken, String tableId);
    public int deleteRecordByIds(List<Long> ids);
    public List<BitableRecord> selectAllRecords(@Param("appToken") String appToken, @Param("tableId") String tableId);
}
