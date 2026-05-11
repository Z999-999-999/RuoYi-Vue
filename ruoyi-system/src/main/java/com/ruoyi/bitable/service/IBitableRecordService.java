package com.ruoyi.bitable.service;

import java.util.List;
import com.ruoyi.bitable.domain.BitableRecord;

/**
 * 多维表格-记录Service接口
 */
public interface IBitableRecordService
{
    public BitableRecord selectRecordById(Long id);
    public BitableRecord selectRecordByTokenTableRecordId(String appToken, String tableId, String recordId);
    public List<BitableRecord> selectRecordList(String appToken, String tableId);
    public List<BitableRecord> selectRecordList(BitableRecord query);
    public int countRecords(String appToken, String tableId);
    public int insertRecord(BitableRecord record);
    public int insertRecords(List<BitableRecord> records);
    public int updateRecord(BitableRecord record);
    public int deleteRecordById(Long id);
    public int deleteRecordByTokenTableRecordId(String appToken, String tableId, String recordId);
    public int deleteRecordsByTable(String appToken, String tableId);
    public int deleteRecordByIds(List<Long> ids);
    public List<BitableRecord> selectAllRecords(String appToken, String tableId);
}
