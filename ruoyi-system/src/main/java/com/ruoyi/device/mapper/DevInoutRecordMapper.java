package com.ruoyi.device.mapper;

import java.util.List;
import com.ruoyi.device.domain.DevInoutRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DevInoutRecordMapper
{
    List<DevInoutRecord> selectRecordList(DevInoutRecord record);

    DevInoutRecord selectRecordById(Long id);

    int insertRecord(DevInoutRecord record);

    int deleteRecordById(Long id);

    /** 按员工查询进出库记录（支持日期范围） */
    List<DevInoutRecord> selectRecordsByEmployee(@Param("employeeId") Long employeeId, @Param("limit") Integer limit,
            @Param("startDate") String startDate, @Param("endDate") String endDate);

    /** 今日进出统计 */
    java.util.Map<String, Object> selectTodayStats();

    /** 月度出库排行（按员工，支持分类筛选） */
    java.util.List<java.util.Map<String, Object>> selectMonthlyOutRank(@Param("yearMonth") String yearMonth,
            @Param("inoutCategoryId") Long inoutCategoryId);

    /** 月度入库排行（按员工，支持分类筛选） */
    java.util.List<java.util.Map<String, Object>> selectMonthlyInRank(@Param("yearMonth") String yearMonth,
            @Param("inoutCategoryId") Long inoutCategoryId);

    /** 统计引用某进出库分类的记录数 */
    int countByInoutCategoryId(@Param("inoutCategoryId") Long inoutCategoryId);

    /** 统计引用某库房分类的记录数 */
    int countByWarehouseCategoryId(@Param("warehouseCategoryId") Long warehouseCategoryId);
}
