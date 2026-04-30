package com.ruoyi.web.controller.device;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.device.domain.DevWarehouseCategory;
import com.ruoyi.device.domain.DevWarehouseStock;
import com.ruoyi.device.service.IDevWarehouseService;
import com.ruoyi.device.service.IDevLogService;

/**
 * 设备管理 - 库房管理 Controller
 */
@RestController
@RequestMapping("/device/warehouse")
public class DevWarehouseController extends BaseController
{
    @Autowired
    private IDevWarehouseService warehouseService;

    @Autowired
    private IDevLogService devLogService;

    /** 库房分类列表 */
    @PreAuthorize("@ss.hasPermi('device:warehouse:view')")
    @GetMapping("/categories")
    public AjaxResult categories()
    {
        return AjaxResult.success(warehouseService.selectCategoryList(new DevWarehouseCategory()));
    }

    @PreAuthorize("@ss.hasPermi('device:settings:view')")
    @Log(title = "库房分类", businessType = BusinessType.INSERT)
    @PostMapping("/category")
    public AjaxResult addCategory(@Validated @RequestBody DevWarehouseCategory category)
    {
        return toAjax(warehouseService.insertCategory(category));
    }

    @PreAuthorize("@ss.hasPermi('device:settings:view')")
    @Log(title = "库房分类", businessType = BusinessType.UPDATE)
    @PutMapping("/category")
    public AjaxResult editCategory(@Validated @RequestBody DevWarehouseCategory category)
    {
        return toAjax(warehouseService.updateCategory(category));
    }

    @PreAuthorize("@ss.hasPermi('device:settings:view')")
    @Log(title = "库房分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/category/{id}")
    public AjaxResult removeCategory(@PathVariable Long id)
    {
        return toAjax(warehouseService.deleteCategoryById(id));
    }

    /** 库房库存列表 */
    @PreAuthorize("@ss.hasPermi('device:warehouse:view')")
    @GetMapping("/stocks")
    public AjaxResult stocks()
    {
        return AjaxResult.success(warehouseService.selectStockList());
    }

    /** 批量更新库存 */
    @PreAuthorize("@ss.hasPermi('device:warehouse:view')")
    @Log(title = "库房库存", businessType = BusinessType.UPDATE)
    @PostMapping("/stock/batch")
    public AjaxResult updateStockBatch(@RequestBody Map<String, Object> body)
    {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        warehouseService.updateStockBatch(items, getUsername());
        devLogService.insertLog("info", "库房管理", "更新库存: " + items.size() + "项", null);
        return AjaxResult.success();
    }
}
