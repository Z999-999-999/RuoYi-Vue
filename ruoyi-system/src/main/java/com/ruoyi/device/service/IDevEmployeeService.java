package com.ruoyi.device.service;

import java.util.List;
import com.ruoyi.device.domain.DevEmployee;

public interface IDevEmployeeService
{
    List<DevEmployee> selectEmployeeList(DevEmployee employee);

    DevEmployee selectEmployeeById(Long id);

    int insertEmployee(DevEmployee employee);

    int updateEmployee(DevEmployee employee);

    int deleteEmployeeById(Long id);

    int sumPhoneTotal();
}
