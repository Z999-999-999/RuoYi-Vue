package com.ruoyi.bitable.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.bitable.domain.BitableRecord;
import com.ruoyi.bitable.mapper.BitableRecordMapper;
import com.ruoyi.bitable.service.IBitableRecordService;

/**
 * 多维表格-记录Service实现
 */
@Service
public class BitableRecordServiceImpl implements IBitableRecordService
{
    @Autowired
    private BitableRecordMapper recordMapper;

    @Override
    public BitableRecord selectRecordById(Long id)
    {
        return recordMapper.selectRecordById(id);
    }

    @Override
    public BitableRecord selectRecordByTokenTableRecordId(String appToken, String tableId, String recordId)
    {
        return recordMapper.selectRecordByTokenTableRecordId(appToken, tableId, recordId);
    }

    @Override
    public List<BitableRecord> selectRecordList(String appToken, String tableId)
    {
        BitableRecord query = new BitableRecord();
        query.setAppToken(appToken);
        query.setTableId(tableId);
        return recordMapper.selectRecordList(query);
    }

    @Override
    public List<BitableRecord> selectRecordList(BitableRecord query)
    {
        return recordMapper.selectRecordList(query);
    }

    @Override
    public int countRecords(String appToken, String tableId)
    {
        return recordMapper.countRecords(appToken, tableId);
    }

    @Override
    public int insertRecord(BitableRecord record)
    {
        if (record.getRecordId() == null || record.getRecordId().isEmpty())
        {
            record.setRecordId("rec" + IdUtils.fastSimpleUUID().substring(0, 8));
        }
        record.setCreatedTime(new Date());
        return recordMapper.insertRecord(record);
    }

    @Override
    public int insertRecords(List<BitableRecord> records)
    {
        for (BitableRecord record : records)
        {
            if (record.getRecordId() == null || record.getRecordId().isEmpty())
            {
                record.setRecordId("rec" + IdUtils.fastSimpleUUID().substring(0, 8));
            }
            record.setCreatedTime(new Date());
        }
        return recordMapper.insertRecords(records);
    }

    @Override
    public int updateRecord(BitableRecord record)
    {
        record.setUpdatedTime(new Date());
        return recordMapper.updateRecord(record);
    }

    @Override
    public int deleteRecordById(Long id)
    {
        return recordMapper.deleteRecordById(id);
    }

    @Override
    public int deleteRecordByTokenTableRecordId(String appToken, String tableId, String recordId)
    {
        return recordMapper.deleteRecordByTokenTableRecordId(appToken, tableId, recordId);
    }

    @Override
    public int deleteRecordsByTable(String appToken, String tableId)
    {
        return recordMapper.deleteRecordsByTable(appToken, tableId);
    }

    @Override
    public int deleteRecordByIds(List<Long> ids)
    {
        return recordMapper.deleteRecordByIds(ids);
    }

    @Override
    public List<BitableRecord> selectAllRecords(String appToken, String tableId)
    {
        return recordMapper.selectAllRecords(appToken, tableId);
    }
}
