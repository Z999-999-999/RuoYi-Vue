package com.ruoyi.device.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.device.domain.DevDailyReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DevDailyReportMapper
{
    List<DevDailyReport> selectReportByDate(@Param("reportDate") String reportDate);

    DevDailyReport selectReportById(Long id);

    DevDailyReport selectReportByDateAndEmployee(@Param("reportDate") String reportDate, @Param("employeeId") Long employeeId);

    int insertReport(DevDailyReport report);

    int updateReport(DevDailyReport report);

    int deleteReportById(Long id);

    /** 查询员工最近N条汇报记录 */
    List<DevDailyReport> selectRecentReports(@Param("employeeId") Long employeeId, @Param("limit") Integer limit);

    /** 获取每个员工最新的平台统计 */
    List<Map<String, Object>> selectLatestPlatformStatsByEmployee();
}
