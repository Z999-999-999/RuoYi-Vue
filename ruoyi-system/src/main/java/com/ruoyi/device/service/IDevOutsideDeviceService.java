package com.ruoyi.device.service;

import java.util.List;
import com.ruoyi.device.domain.DevOutsideDevice;

public interface IDevOutsideDeviceService
{
    List<DevOutsideDevice> selectOutsideList(DevOutsideDevice device);

    DevOutsideDevice selectOutsideById(Long id);

    int insertOutside(DevOutsideDevice device);

    int updateOutside(DevOutsideDevice device);

    int deleteOutsideById(Long id);

    int sumUnreturnedQuantity();
}
