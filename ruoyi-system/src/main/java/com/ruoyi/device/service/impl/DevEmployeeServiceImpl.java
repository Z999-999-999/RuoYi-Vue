package com.ruoyi.device.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.device.domain.DevEmployee;
import com.ruoyi.device.mapper.DevEmployeeMapper;
import com.ruoyi.device.service.IDevEmployeeService;

@Service
public class DevEmployeeServiceImpl implements IDevEmployeeService
{
    @Autowired
    private DevEmployeeMapper employeeMapper;

    @Override
    public List<DevEmployee> selectEmployeeList(DevEmployee employee)
    {
        return employeeMapper.selectEmployeeList(employee);
    }

    @Override
    public DevEmployee selectEmployeeById(Long id)
    {
        return employeeMapper.selectEmployeeById(id);
    }

    @Override
    public int insertEmployee(DevEmployee employee)
    {
        employee.setCreateBy(SecurityUtils.getUsername());
        employee.setCreateTime(DateUtils.getNowDate());
        return employeeMapper.insertEmployee(employee);
    }

    @Override
    public int updateEmployee(DevEmployee employee)
    {
        employee.setUpdateBy(SecurityUtils.getUsername());
        employee.setUpdateTime(DateUtils.getNowDate());
        return employeeMapper.updateEmployee(employee);
    }

    @Override
    public int deleteEmployeeById(Long id)
    {
        // 检查是否有关联的每日汇报记录
        int reportCount = employeeMapper.countDailyReportsByEmployeeId(id);
        if (reportCount > 0)
        {
            throw new ServiceException("该员工有 " + reportCount + " 条每日汇报记录，无法删除");
        }
        // 检查是否有关联的每日核对记录
        int checkCount = employeeMapper.countDailyChecksByEmployeeId(id);
        if (checkCount > 0)
        {
            throw new ServiceException("该员工有 " + checkCount + " 条每日核对记录，无法删除");
        }
        return employeeMapper.deleteEmployeeById(id);
    }

    @Override
    public int sumPhoneTotal()
    {
        return employeeMapper.sumPhoneTotal();
    }
}
