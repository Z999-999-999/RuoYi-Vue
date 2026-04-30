package com.ruoyi.web.controller.device;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.device.domain.DevEmployee;
import com.ruoyi.device.domain.DevDailyReport;
import com.ruoyi.device.domain.DevInoutRecord;
import com.ruoyi.device.domain.DevAccountPlatform;
import com.ruoyi.device.service.IDevLogService;
import com.ruoyi.device.service.IDevEmployeeService;
import com.ruoyi.device.service.IDevWarehouseService;
import com.ruoyi.device.service.IDevOutsideDeviceService;
import com.ruoyi.device.service.IDevCategoryService;
import com.ruoyi.device.service.IDevInoutRecordService;
import com.ruoyi.device.service.IDevDailyReportService;

/**
 * 设备管理 - 总览仪表盘 Controller
 */
@RestController
@RequestMapping("/device/dashboard")
public class DevDashboardController extends BaseController
{
    @Autowired
    private IDevLogService logService;

    @Autowired
    private IDevEmployeeService employeeService;

    @Autowired
    private IDevWarehouseService warehouseService;

    @Autowired
    private IDevOutsideDeviceService outsideService;

    @Autowired
    private IDevCategoryService categoryService;

    @Autowired
    private IDevInoutRecordService inoutRecordService;

    @Autowired
    private IDevDailyReportService dailyReportService;

    /** 总览数据 */
    @PreAuthorize("@ss.hasPermi('device:dashboard:view')")
    @GetMapping
    public AjaxResult dashboard()
    {
        Map<String, Object> data = logService.getDashboardData();

        // 补充公司设定总数
        String totalStr = categoryService.getConfig("company_phone_total");
        int companyTotal = (totalStr != null && !totalStr.isEmpty()) ? Integer.parseInt(totalStr) : 0;
        data.put("companyTotal", companyTotal);

        return AjaxResult.success(data);
    }

    /** 全量基础数据（前端初始化加载） */
    @PreAuthorize("@ss.hasPermi('device:dashboard:view')")
    @GetMapping("/base")
    public AjaxResult baseData()
    {
        Map<String, Object> data = new HashMap<>();
        data.put("employees", employeeService.selectEmployeeList(new DevEmployee()));
        data.put("warehouseCategories", warehouseService.selectCategoryList(new com.ruoyi.device.domain.DevWarehouseCategory()));
        data.put("warehouseStocks", warehouseService.selectStockList());
        com.ruoyi.device.domain.DevInoutCategory inoutQuery = new com.ruoyi.device.domain.DevInoutCategory();
        inoutQuery.setType(null); // 不按类型过滤，获取全部分类
        data.put("inoutCategories", categoryService.selectInoutCategoryList(inoutQuery));
        data.put("accountPlatforms", categoryService.selectPlatformList());
        data.put("outsideDevices", outsideService.selectOutsideList(new com.ruoyi.device.domain.DevOutsideDevice()));

        String totalStr = categoryService.getConfig("company_phone_total");
        data.put("companyPhoneTotal", (totalStr != null && !totalStr.isEmpty()) ? Integer.parseInt(totalStr) : 0);

        return AjaxResult.success(data);
    }

    /** 月度出库排行 */
    @PreAuthorize("@ss.hasPermi('device:dashboard:view')")
    @GetMapping("/outRank")
    public AjaxResult outRank(@RequestParam(defaultValue = "") String yearMonth,
            @RequestParam(required = false) Long categoryId)
    {
        if (yearMonth == null || yearMonth.isEmpty())
        {
            java.time.YearMonth ym = java.time.YearMonth.now();
            yearMonth = ym.toString();
        }
        else
        {
            // 校验格式 YYYY-MM
            if (!yearMonth.matches("^\\d{4}-(0[1-9]|1[0-2])$"))
            {
                return AjaxResult.error("月份格式错误，请使用 YYYY-MM 格式");
            }
        }
        return AjaxResult.success(inoutRecordService.selectMonthlyOutRank(yearMonth, categoryId));
    }

    /** 月度入库排行 */
    @PreAuthorize("@ss.hasPermi('device:dashboard:view')")
    @GetMapping("/inRank")
    public AjaxResult inRank(@RequestParam(defaultValue = "") String yearMonth,
            @RequestParam(required = false) Long categoryId)
    {
        if (yearMonth == null || yearMonth.isEmpty())
        {
            java.time.YearMonth ym = java.time.YearMonth.now();
            yearMonth = ym.toString();
        }
        else
        {
            // 校验格式 YYYY-MM
            if (!yearMonth.matches("^\\d{4}-(0[1-9]|1[0-2])$"))
            {
                return AjaxResult.error("月份格式错误，请使用 YYYY-MM 格式");
            }
        }
        return AjaxResult.success(inoutRecordService.selectMonthlyInRank(yearMonth, categoryId));
    }

    /** 员工详情 */
    @PreAuthorize("@ss.hasPermi('device:dashboard:view')")
    @GetMapping("/employee/{id}")
    public AjaxResult employeeDetail(@PathVariable Long id,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate)
    {
        Map<String, Object> data = new HashMap<>();

        // 员工基本信息
        DevEmployee emp = employeeService.selectEmployeeById(id);
        if (emp == null)
        {
            return AjaxResult.error("员工不存在");
        }
        data.put("employee", emp);

        // 进出库记录（支持日期范围筛选，有日期时不限制条数）
        Integer limit = (startDate != null || endDate != null) ? null : 30;
        java.util.List<DevInoutRecord> records = inoutRecordService.selectRecordsByEmployee(id, limit, startDate, endDate);
        data.put("inoutRecords", records);

        // 最近30天汇报记录
        java.util.List<DevDailyReport> reports = dailyReportService.selectRecentReports(id, 30);
        data.put("dailyReports", reports);

        return AjaxResult.success(data);
    }

    /** 员工设备分类标签 */
    @PreAuthorize("@ss.hasPermi('device:dashboard:view')")
    @GetMapping("/employeePlatformTags")
    public AjaxResult employeePlatformTags()
    {
        // 1. 获取每个员工最新的平台统计
        java.util.List<Map<String, Object>> statsList = dailyReportService.selectLatestPlatformStatsByEmployee();

        // 2. 获取平台配置（用于获取label和color）
        java.util.List<DevAccountPlatform> platforms = categoryService.selectPlatformList();
        Map<String, DevAccountPlatform> platformMap = new HashMap<>();
        for (DevAccountPlatform p : platforms) {
            platformMap.put(p.getCode(), p);
        }

        // 3. 构建返回数据
        java.util.List<Map<String, Object>> result = new ArrayList<>();
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

        for (Map<String, Object> row : statsList) {
            Long employeeId = ((Number) row.get("employeeId")).longValue();
            String platformStatsJson = (String) row.get("platformStats");

            if (platformStatsJson == null || platformStatsJson.isEmpty()) {
                continue;
            }

            try {
                // 解析 platformStats JSON 数组
                java.util.List<Map<String, Object>> stats = mapper.readValue(platformStatsJson, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<Map<String, Object>>>() {});

                java.util.List<Map<String, Object>> tags = new ArrayList<>();
                for (Map<String, Object> stat : stats) {
                    String platformCode = (String) stat.get("platform");
                    Integer active = (Integer) stat.get("active");
                    // 只统计使用中的设备
                    if (active != null && active > 0) {
                        DevAccountPlatform platform = platformMap.get(platformCode);
                        Map<String, Object> tag = new HashMap<>();
                        tag.put("code", platformCode);
                        tag.put("label", platform != null ? platform.getLabel() : platformCode);
                        tag.put("color", platform != null ? platform.getColor() : "#94a3b8");
                        tag.put("count", active);
                        tags.add(tag);
                    }
                }

                if (!tags.isEmpty()) {
                    Map<String, Object> empTags = new HashMap<>();
                    empTags.put("employeeId", employeeId);
                    empTags.put("tags", tags);
                    result.add(empTags);
                }
            } catch (Exception e) {
                // 解析失败，跳过
            }
        }

        return AjaxResult.success(result);
    }
}
