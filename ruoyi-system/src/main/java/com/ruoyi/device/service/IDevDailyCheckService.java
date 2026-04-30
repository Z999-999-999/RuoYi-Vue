package com.ruoyi.device.service;

import java.util.List;
import com.ruoyi.device.domain.DevDailyCheck;

public interface IDevDailyCheckService
{
    List<DevDailyCheck> selectCheckByDate(String checkDate);

    DevDailyCheck selectCheckByDateAndEmployee(String checkDate, Long employeeId);

    /** 更新核对状态（UPSERT） */
    int updateCheck(DevDailyCheck check);

    int deleteCheckById(Long id);
}
