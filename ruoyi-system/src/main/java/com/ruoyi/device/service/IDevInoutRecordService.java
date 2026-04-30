package com.ruoyi.device.service;

import java.util.List;
import com.ruoyi.device.domain.DevInoutRecord;

public interface IDevInoutRecordService
{
    List<DevInoutRecord> selectRecordList(DevInoutRecord record);

    DevInoutRecord selectRecordById(Long id);

    /**
     * 新增进出库记录（含事务：自动更新员工持有数和库房库存）
     */
    int insertRecord(DevInoutRecord record);

    /**
     * 删除进出库记录（含事务：自动回滚库存）
     */
    int deleteRecordById(Long id);

    List<DevInoutRecord> selectRecordsByEmployee(Long employeeId, Integer limit, String startDate, String endDate);

    /** 月度出库排行（支持分类筛选） */
    java.util.List<java.util.Map<String, Object>> selectMonthlyOutRank(String yearMonth, Long inoutCategoryId);

    /** 月度入库排行（支持分类筛选） */
    java.util.List<java.util.Map<String, Object>> selectMonthlyInRank(String yearMonth, Long inoutCategoryId);
}
