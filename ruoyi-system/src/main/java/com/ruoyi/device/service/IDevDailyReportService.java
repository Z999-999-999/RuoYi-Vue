package com.ruoyi.device.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.device.domain.DevDailyReport;

public interface IDevDailyReportService
{
    List<DevDailyReport> selectReportByDate(String reportDate);

    DevDailyReport selectReportById(Long id);

    /** 提交或更新汇报（UPSERT） */
    int submitReport(DevDailyReport report);

    int deleteReportById(Long id);

    List<DevDailyReport> selectRecentReports(Long employeeId, Integer limit);

    /** 获取每个员工最新的平台统计 */
    List<Map<String, Object>> selectLatestPlatformStatsByEmployee();
}
