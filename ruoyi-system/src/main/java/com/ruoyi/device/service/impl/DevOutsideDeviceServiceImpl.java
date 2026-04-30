package com.ruoyi.device.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.device.domain.DevOutsideDevice;
import com.ruoyi.device.mapper.DevOutsideDeviceMapper;
import com.ruoyi.device.service.IDevOutsideDeviceService;

@Service
public class DevOutsideDeviceServiceImpl implements IDevOutsideDeviceService
{
    @Autowired
    private DevOutsideDeviceMapper outsideMapper;

    @Override
    public List<DevOutsideDevice> selectOutsideList(DevOutsideDevice device)
    {
        return outsideMapper.selectOutsideList(device);
    }

    @Override
    public DevOutsideDevice selectOutsideById(Long id)
    {
        return outsideMapper.selectOutsideById(id);
    }

    @Override
    public int insertOutside(DevOutsideDevice device)
    {
        device.setCreateBy(SecurityUtils.getUsername());
        device.setCreateTime(DateUtils.getNowDate());
        return outsideMapper.insertOutside(device);
    }

    @Override
    public int updateOutside(DevOutsideDevice device)
    {
        device.setUpdateBy(SecurityUtils.getUsername());
        device.setUpdateTime(DateUtils.getNowDate());
        return outsideMapper.updateOutside(device);
    }

    @Override
    public int deleteOutsideById(Long id)
    {
        return outsideMapper.deleteOutsideById(id);
    }

    @Override
    public int sumUnreturnedQuantity()
    {
        return outsideMapper.sumUnreturnedQuantity();
    }
}
