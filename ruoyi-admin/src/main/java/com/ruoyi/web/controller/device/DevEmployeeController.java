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
import com.ruoyi.device.domain.DevEmployee;
import com.ruoyi.device.service.IDevEmployeeService;
import com.ruoyi.device.service.IDevLogService;

/**
 * 设备管理 - 员工管理 Controller
 */
@RestController
@RequestMapping("/device/employee")
public class DevEmployeeController extends BaseController
{
    @Autowired
    private IDevEmployeeService employeeService;

    @Autowired
    private IDevLogService devLogService;

    @PreAuthorize("@ss.hasPermi('device:dashboard:view')")
    @GetMapping("/list")
    public TableDataInfo list(DevEmployee employee)
    {
        startPage();
        List<DevEmployee> list = employeeService.selectEmployeeList(employee);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('device:dashboard:view')")
    @GetMapping("/all")
    public AjaxResult all()
    {
        return AjaxResult.success(employeeService.selectEmployeeList(new DevEmployee()));
    }

    @PreAuthorize("@ss.hasPermi('device:dashboard:view')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return AjaxResult.success(employeeService.selectEmployeeById(id));
    }

    @PreAuthorize("@ss.hasPermi('device:employee:add')")
    @Log(title = "员工管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody DevEmployee employee)
    {
        int rows = employeeService.insertEmployee(employee);
        if (rows > 0)
        {
            devLogService.insertLog("info", "员工管理", "添加员工: " + employee.getName(), null);
        }
        return toAjax(rows);
    }

    @PreAuthorize("@ss.hasPermi('device:employee:edit')")
    @Log(title = "员工管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody DevEmployee employee)
    {
        int rows = employeeService.updateEmployee(employee);
        if (rows > 0)
        {
            devLogService.insertLog("info", "员工管理", "编辑员工: " + employee.getName(), null);
        }
        return toAjax(rows);
    }

    @PreAuthorize("@ss.hasPermi('device:employee:remove')")
    @Log(title = "员工管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        DevEmployee emp = employeeService.selectEmployeeById(id);
        if (emp != null && emp.getPhoneTotal() != null && emp.getPhoneTotal() > 0)
        {
            return AjaxResult.error("该员工名下还有 " + emp.getPhoneTotal() + " 台设备未归还，请先回收设备再删除");
        }
        int rows = employeeService.deleteEmployeeById(id);
        if (rows > 0 && emp != null)
        {
            devLogService.insertLog("warning", "员工管理", "删除员工: " + emp.getName(), "ID:" + id);
        }
        return toAjax(rows);
    }
}
