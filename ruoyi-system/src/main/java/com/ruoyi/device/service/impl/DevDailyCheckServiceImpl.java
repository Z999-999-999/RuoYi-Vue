package com.ruoyi.device.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.device.domain.DevDailyCheck;
import com.ruoyi.device.mapper.DevDailyCheckMapper;
import com.ruoyi.device.service.IDevDailyCheckService;

@Service
public class DevDailyCheckServiceImpl implements IDevDailyCheckService
{
    @Autowired
    private DevDailyCheckMapper checkMapper;

    @Override
    public List<DevDailyCheck> selectCheckByDate(String checkDate)
    {
        return checkMapper.selectCheckByDate(checkDate);
    }

    @Override
    public DevDailyCheck selectCheckByDateAndEmployee(String checkDate, Long employeeId)
    {
        return checkMapper.selectCheckByDateAndEmployee(checkDate, employeeId);
    }

    @Override
    public int updateCheck(DevDailyCheck check)
    {
        String username = SecurityUtils.getUsername();
        Date now = DateUtils.getNowDate();
        check.setCreateBy(username);
        check.setCreateTime(now);
        check.setUpdateBy(username);
        check.setUpdateTime(now);
        if ("confirmed".equals(check.getStatus()) || "absent".equals(check.getStatus()))
        {
            check.setCheckedAt(now);
        }
        return checkMapper.insertCheck(check);  // ON DUPLICATE KEY UPDATE
    }

    @Override
    public int deleteCheckById(Long id)
    {
        return checkMapper.deleteCheckById(id);
    }
}
