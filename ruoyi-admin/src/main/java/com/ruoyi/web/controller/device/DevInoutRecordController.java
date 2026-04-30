package com.ruoyi.web.controller.device;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.device.domain.DevInoutRecord;
import com.ruoyi.device.domain.DevEmployee;
import com.ruoyi.device.domain.DevInoutCategory;
import com.ruoyi.device.domain.DevWarehouseCategory;
import com.ruoyi.device.domain.DevWarehouseStock;
import com.ruoyi.device.service.IDevInoutRecordService;
import com.ruoyi.device.service.IDevLogService;
import com.ruoyi.device.service.IDevEmployeeService;
import com.ruoyi.device.service.IDevCategoryService;
import com.ruoyi.device.service.IDevWarehouseService;
import com.ruoyi.device.mapper.DevInoutCategoryMapper;
import com.ruoyi.device.mapper.DevWarehouseMapper;

/**
 * 设备管理 - 进出库记录 Controller
 */
@RestController
@RequestMapping("/device/inout")
public class DevInoutRecordController extends BaseController
{
    @Autowired
    private IDevInoutRecordService inoutService;

    @Autowired
    private IDevLogService devLogService;

    @Autowired
    private IDevEmployeeService employeeService;

    @Autowired
    private IDevWarehouseService warehouseService;

    @Autowired
    private DevInoutCategoryMapper inoutCategoryMapper;

    @Autowired
    private DevWarehouseMapper warehouseMapper;

    @PreAuthorize("@ss.hasPermi('device:inout:view')")
    @GetMapping("/list")
    public TableDataInfo list(DevInoutRecord record)
    {
        startPage();
        List<DevInoutRecord> list = inoutService.selectRecordList(record);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('device:inout:view')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return AjaxResult.success(inoutService.selectRecordById(id));
    }

    @PreAuthorize("@ss.hasPermi('device:inout:add')")
    @Log(title = "进出库记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody DevInoutRecord record)
    {
        // 记录操作前的库存和员工持有数
        String typeName = "out".equals(record.getType()) ? "出库" : "入库";
        int qty = record.getQuantity() != null ? record.getQuantity() : 1;

        // 获取进出库原因名称
        String inoutCatName = "";
        if (record.getInoutCategoryId() != null)
        {
            DevInoutCategory inoutCat = inoutCategoryMapper.selectCategoryById(record.getInoutCategoryId());
            if (inoutCat != null) inoutCatName = inoutCat.getName();
        }

        // 获取库房分类名称和操作前库存
        String wareCatName = "";
        int oldStockQty = 0;
        if (record.getWarehouseCategoryId() != null)
        {
            DevWarehouseCategory wareCat = warehouseService.selectCategoryById(record.getWarehouseCategoryId());
            if (wareCat != null) wareCatName = wareCat.getName();
            DevWarehouseStock stock = warehouseMapper.selectStockByCategoryId(record.getWarehouseCategoryId());
            if (stock != null) oldStockQty = stock.getQuantity() != null ? stock.getQuantity() : 0;
        }

        // 获取员工名和操作前持有数
        String empName = "";
        int oldEmpQty = 0;
        Long empId = "out".equals(record.getType()) ? record.getReceiverEmployeeId() : record.getSenderEmployeeId();
        if (empId != null)
        {
            DevEmployee emp = employeeService.selectEmployeeById(empId);
            if (emp != null)
            {
                empName = emp.getName();
                oldEmpQty = emp.getPhoneTotal() != null ? emp.getPhoneTotal() : 0;
            }
        }

        // 执行进出库操作
        int rows = inoutService.insertRecord(record);

        if (rows > 0)
        {
            // 计算操作后的库存和员工持有数
            int newStockQty;
            int newEmpQty;
            if ("out".equals(record.getType()))
            {
                newStockQty = oldStockQty - qty;
                newEmpQty = oldEmpQty + qty;
            }
            else
            {
                newStockQty = oldStockQty + qty;
                newEmpQty = oldEmpQty - qty;
            }

            // 构建详细日志消息：出库 1 台 [作品号] | 库房(作品号)：9→8台 | 接收人(吴豪杰)：9→10台
            StringBuilder msg = new StringBuilder();
            msg.append(typeName).append(" ").append(qty).append(" 台");
            if (!inoutCatName.isEmpty())
            {
                msg.append(" [").append(inoutCatName).append("]");
            }

            // 库房变化
            if (!wareCatName.isEmpty())
            {
                msg.append(" | 库房(").append(wareCatName).append(")：").append(oldStockQty).append("→").append(newStockQty).append("台");
            }

            // 员工变化
            if (!empName.isEmpty())
            {
                String roleLabel = "out".equals(record.getType()) ? "接收人" : "归还人";
                msg.append(" | ").append(roleLabel).append("(").append(empName).append(")：").append(oldEmpQty).append("→").append(newEmpQty).append("台");
            }

            devLogService.insertLog("info", "进出库", msg.toString(),
                "类型:" + record.getType() + ",数量:" + qty + ",库房分类:" + wareCatName + ",员工:" + empName);
        }
        return toAjax(rows);
    }

    @PreAuthorize("@ss.hasPermi('device:inout:remove')")
    @Log(title = "进出库记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        // 删除前记录日志
        DevInoutRecord record = inoutService.selectRecordById(id);
        if (record != null)
        {
            String typeName = "out".equals(record.getType()) ? "出库" : "入库";
            int qty = record.getQuantity() != null ? record.getQuantity() : 0;
            devLogService.insertLog("warning", "进出库", "删除" + typeName + "记录: " + qty + "台 (回滚库存)",
                "记录ID:" + id + ",类型:" + record.getType() + ",数量:" + qty);
        }
        return toAjax(inoutService.deleteRecordById(id));
    }

    @PreAuthorize("@ss.hasPermi('device:inout:view')")
    @GetMapping("/employee/{employeeId}")
    public AjaxResult getByEmployee(@PathVariable Long employeeId,
            @RequestParam(defaultValue = "20") Integer limit)
    {
        return AjaxResult.success(inoutService.selectRecordsByEmployee(employeeId, limit, null, null));
    }
}
