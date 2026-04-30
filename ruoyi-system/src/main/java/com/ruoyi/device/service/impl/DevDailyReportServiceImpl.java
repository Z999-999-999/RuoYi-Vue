package com.ruoyi.device.service.impl;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.device.domain.DevDailyReport;
import com.ruoyi.device.mapper.DevDailyReportMapper;
import com.ruoyi.device.service.IDevDailyReportService;

@Service
public class DevDailyReportServiceImpl implements IDevDailyReportService
{
    @Autowired
    private DevDailyReportMapper reportMapper;

    @Override
    public List<DevDailyReport> selectReportByDate(String reportDate)
    {
        return reportMapper.selectReportByDate(reportDate);
    }

    @Override
    public DevDailyReport selectReportById(Long id)
    {
        return reportMapper.selectReportById(id);
    }

    @Override
    public int submitReport(DevDailyReport report)
    {
        String username = SecurityUtils.getUsername();
        report.setCreateBy(username);
        report.setCreateTime(DateUtils.getNowDate());
        report.setUpdateBy(username);
        report.setUpdateTime(DateUtils.getNowDate());
        return reportMapper.insertReport(report);  // ON DUPLICATE KEY UPDATE
    }

    @Override
    public int deleteReportById(Long id)
    {
        return reportMapper.deleteReportById(id);
    }

    @Override
    public List<DevDailyReport> selectRecentReports(Long employeeId, Integer limit)
    {
        return reportMapper.selectRecentReports(employeeId, limit);
    }

    @Override
    public List<Map<String, Object>> selectLatestPlatformStatsByEmployee()
    {
        return reportMapper.selectLatestPlatformStatsByEmployee();
    }
}
