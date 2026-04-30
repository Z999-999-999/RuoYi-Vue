package com.ruoyi.web.controller.device;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.device.domain.DevDailyReport;
import com.ruoyi.device.domain.DevDailyCheck;
import com.ruoyi.device.domain.DevEmployee;
import com.ruoyi.device.service.IDevDailyReportService;
import com.ruoyi.device.service.IDevDailyCheckService;
import com.ruoyi.device.service.IDevLogService;
import com.ruoyi.device.service.IDevEmployeeService;

/**
 * 设备管理 - 每日汇报/核对 Controller
 */
@RestController
@RequestMapping("/device/daily")
public class DevDailyController extends BaseController
{
    @Autowired
    private IDevDailyReportService reportService;

    @Autowired
    private IDevDailyCheckService checkService;

    @Autowired
    private IDevLogService devLogService;

    @Autowired
    private IDevEmployeeService employeeService;

    // ---- 每日汇报 ----

    @PreAuthorize("@ss.hasPermi('device:report:view')")
    @GetMapping("/report")
    public AjaxResult getReports(@RequestParam String reportDate)
    {
        return AjaxResult.success(reportService.selectReportByDate(reportDate));
    }

    @PreAuthorize("@ss.hasPermi('device:report:view')")
    @PostMapping("/report")
    @Log(title = "每日汇报", businessType = BusinessType.INSERT)
    public AjaxResult submitReport(@RequestBody DevDailyReport report)
    {
        int rows = reportService.submitReport(report);
        if (rows > 0)
        {
            DevEmployee emp = employeeService.selectEmployeeById(report.getEmployeeId());
            String empName = emp != null ? emp.getName() : String.valueOf(report.getEmployeeId());
            devLogService.insertLog("info", "每日汇报", "提交汇报: " + empName + " 手机" + report.getPhoneCount() + "台",
                "日期:" + report.getReportDate());
        }
        return toAjax(rows);
    }

    @PreAuthorize("@ss.hasPermi('device:report:view')")
    @DeleteMapping("/report/{id}")
    @Log(title = "每日汇报", businessType = BusinessType.DELETE)
    public AjaxResult deleteReport(@PathVariable Long id)
    {
        return toAjax(reportService.deleteReportById(id));
    }

    @PreAuthorize("@ss.hasPermi('device:report:view')")
    @GetMapping("/report/recent/{employeeId}")
    public AjaxResult recentReports(@PathVariable Long employeeId,
            @RequestParam(defaultValue = "30") Integer limit)
    {
        return AjaxResult.success(reportService.selectRecentReports(employeeId, limit));
    }

    // ---- 每日核对 ----

    @PreAuthorize("@ss.hasPermi('device:report:view')")
    @GetMapping("/check")
    public AjaxResult getChecks(@RequestParam String checkDate)
    {
        return AjaxResult.success(checkService.selectCheckByDate(checkDate));
    }

    @PreAuthorize("@ss.hasPermi('device:report:view')")
    @PostMapping("/check")
    @Log(title = "每日核对", businessType = BusinessType.UPDATE)
    public AjaxResult updateCheck(@RequestBody DevDailyCheck check)
    {
        return toAjax(checkService.updateCheck(check));
    }
}
