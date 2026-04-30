package com.ruoyi.web.controller.device;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.device.domain.DevOutsideDevice;
import com.ruoyi.device.service.IDevOutsideDeviceService;
import com.ruoyi.device.mapper.DevWarehouseMapper;
import com.ruoyi.device.service.IDevLogService;
import com.ruoyi.device.service.IDevWarehouseService;
import com.ruoyi.device.domain.DevWarehouseCategory;
import com.ruoyi.device.domain.DevWarehouseStock;

/**
 * 设备管理 - 在外设备 Controller
 */
@RestController
@RequestMapping("/device/outside")
public class DevOutsideDeviceController extends BaseController
{
    @Autowired
    private IDevOutsideDeviceService outsideService;

    @Autowired
    private DevWarehouseMapper warehouseMapper;

    @Autowired
    private IDevLogService devLogService;

    @Autowired
    private IDevWarehouseService warehouseService;

    @PreAuthorize("@ss.hasPermi('device:dashboard:view')")
    @GetMapping("/list")
    public AjaxResult list(DevOutsideDevice device)
    {
        return AjaxResult.success(outsideService.selectOutsideList(device));
    }

    @PreAuthorize("@ss.hasPermi('device:settings:view')")
    @Log(title = "在外设备", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody DevOutsideDevice device)
    {
        return toAjax(outsideService.insertOutside(device));
    }

    @PreAuthorize("@ss.hasPermi('device:settings:view')")
    @Log(title = "在外设备", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DevOutsideDevice device)
    {
        return toAjax(outsideService.updateOutside(device));
    }

    /** 确认归还 */
    @PreAuthorize("@ss.hasPermi('device:settings:view')")
    @PutMapping("/return/{id}")
    @Log(title = "在外设备归还", businessType = BusinessType.UPDATE)
    public AjaxResult returnDevice(@PathVariable Long id,
            @RequestBody java.util.Map<String, String> body)
    {
        // 1. 查询该在外设备记录
        DevOutsideDevice outsideDevice = outsideService.selectOutsideById(id);
        if (outsideDevice == null)
        {
            return AjaxResult.error("在外设备记录不存在");
        }

        Long warehouseCategoryId = outsideDevice.getWarehouseCategoryId();
        int qty = outsideDevice.getQuantity() != null ? outsideDevice.getQuantity() : 1;

        // 2. 获取归还前的库存数量（用于日志）
        int oldStockQty = 0;
        if (warehouseCategoryId != null)
        {
            DevWarehouseStock stock = warehouseMapper.selectStockByCategoryId(warehouseCategoryId);
            if (stock != null) oldStockQty = stock.getQuantity() != null ? stock.getQuantity() : 0;
        }

        // 3. 恢复库存数量（+qty）
        if (warehouseCategoryId != null)
        {
            warehouseMapper.updateQuantity(warehouseCategoryId, qty);
        }

        // 4. 设置归还日期
        DevOutsideDevice device = new DevOutsideDevice();
        device.setId(id);
        try
        {
            device.setActualReturnDate(new java.text.SimpleDateFormat("yyyy-MM-dd")
                    .parse(body.get("actualReturnDate")));
        }
        catch (Exception e)
        {
            // 日期格式错误，回滚库存
            if (warehouseCategoryId != null)
            {
                warehouseMapper.updateQuantity(warehouseCategoryId, -qty);
            }
            return AjaxResult.error("日期格式错误");
        }

        // 5. 更新记录
        int rows = outsideService.updateOutside(device);

        // 6. 记录日志
        if (rows > 0)
        {
            String wareCatName = "";
            if (warehouseCategoryId != null)
            {
                DevWarehouseCategory wareCat = warehouseService.selectCategoryById(warehouseCategoryId);
                if (wareCat != null) wareCatName = wareCat.getName();
            }

            int newStockQty = oldStockQty + qty;

            StringBuilder msg = new StringBuilder();
            msg.append("归还 ").append(qty).append(" 台");
            if (!wareCatName.isEmpty())
            {
                msg.append(" | 库房(").append(wareCatName).append(")：").append(oldStockQty).append("→").append(newStockQty).append("台");
            }

            devLogService.insertLog("info", "在外设备归还", msg.toString(),
                "设备ID:" + id + ",数量:" + qty + ",库房分类:" + wareCatName);
        }

        return toAjax(rows);
    }

    @PreAuthorize("@ss.hasPermi('device:settings:view')")
    @Log(title = "在外设备", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        return toAjax(outsideService.deleteOutsideById(id));
    }
}
