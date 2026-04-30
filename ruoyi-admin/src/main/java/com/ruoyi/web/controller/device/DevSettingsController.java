package com.ruoyi.web.controller.device;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.device.domain.DevLog;
import com.ruoyi.device.domain.DevAccountPlatform;
import com.ruoyi.device.domain.DevInoutCategory;
import com.ruoyi.device.service.IDevLogService;
import com.ruoyi.device.service.IDevCategoryService;

/**
 * 设备管理 - 系统设置 Controller
 * 包含：日志管理、账号平台、进出库原因、系统配置
 */
@RestController
@RequestMapping("/device/settings")
public class DevSettingsController extends BaseController
{
    @Autowired
    private IDevLogService logService;

    @Autowired
    private IDevCategoryService categoryService;

    // ---- 操作日志 ----

    @PreAuthorize("@ss.hasPermi('device:log:view')")
    @GetMapping("/logs")
    public TableDataInfo logs(DevLog log)
    {
        startPage();
        List<DevLog> list = logService.selectLogList(log);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('device:log:view')")
    @GetMapping("/logs/modules")
    public AjaxResult logModules()
    {
        return AjaxResult.success(logService.selectDistinctModules());
    }

    @PreAuthorize("@ss.hasPermi('device:log:view')")
    @GetMapping("/logs/errorCount")
    public AjaxResult errorCount()
    {
        return AjaxResult.success(logService.countErrorLogs());
    }

    @PreAuthorize("@ss.hasPermi('device:settings:view')")
    @Log(title = "设备日志清空", businessType = BusinessType.CLEAN)
    @DeleteMapping("/logs/clear")
    public AjaxResult clearLogs(@RequestParam(defaultValue = "info") String scope)
    {
        return toAjax(logService.clearLogs(scope));
    }

    // ---- 账号平台 ----

    @PreAuthorize("@ss.hasPermi('device:settings:view')")
    @GetMapping("/platforms")
    public AjaxResult platforms()
    {
        return AjaxResult.success(categoryService.selectPlatformList());
    }

    @PreAuthorize("@ss.hasPermi('device:settings:view')")
    @PostMapping("/platform")
    public AjaxResult addPlatform(@Validated @RequestBody DevAccountPlatform platform)
    {
        return toAjax(categoryService.insertPlatform(platform));
    }

    @PreAuthorize("@ss.hasPermi('device:settings:view')")
    @PutMapping("/platform")
    public AjaxResult editPlatform(@Validated @RequestBody DevAccountPlatform platform)
    {
        return toAjax(categoryService.updatePlatform(platform));
    }

    @PreAuthorize("@ss.hasPermi('device:settings:view')")
    @DeleteMapping("/platform/{id}")
    public AjaxResult removePlatform(@PathVariable Long id)
    {
        return toAjax(categoryService.deletePlatformById(id));
    }

    // ---- 进出库原因 ----

    @PreAuthorize("@ss.hasPermi('device:settings:view')")
    @GetMapping("/inoutCategories")
    public AjaxResult inoutCategories(@RequestParam(required = false) String type)
    {
        DevInoutCategory query = new DevInoutCategory();
        query.setType(type);
        return AjaxResult.success(categoryService.selectInoutCategoryList(query));
    }

    @PreAuthorize("@ss.hasPermi('device:settings:view')")
    @PostMapping("/inoutCategory")
    public AjaxResult addInoutCategory(@Validated @RequestBody DevInoutCategory category)
    {
        return toAjax(categoryService.insertInoutCategory(category));
    }

    @PreAuthorize("@ss.hasPermi('device:settings:view')")
    @PutMapping("/inoutCategory")
    public AjaxResult editInoutCategory(@Validated @RequestBody DevInoutCategory category)
    {
        return toAjax(categoryService.updateInoutCategory(category));
    }

    @PreAuthorize("@ss.hasPermi('device:settings:view')")
    @DeleteMapping("/inoutCategory/{id}")
    public AjaxResult removeInoutCategory(@PathVariable Long id)
    {
        return toAjax(categoryService.deleteInoutCategoryById(id));
    }

    // ---- 系统配置 ----

    @PreAuthorize("@ss.hasPermi('device:settings:view')")
    @GetMapping("/config")
    public AjaxResult getConfig(@RequestParam String key)
    {
        String value = categoryService.getConfig(key);
        Map<String, Object> data = new HashMap<>();
        data.put("key", key);
        data.put("value", value);
        return AjaxResult.success(data);
    }

    @PreAuthorize("@ss.hasPermi('device:settings:view')")
    @PostMapping("/config")
    public AjaxResult setConfig(@RequestBody Map<String, String> body)
    {
        categoryService.setConfig(body.get("key"), body.get("value"));
        return AjaxResult.success();
    }
}
