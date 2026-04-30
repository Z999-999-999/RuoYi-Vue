package com.ruoyi.device.mapper;

import java.util.List;
import com.ruoyi.device.domain.DevDailyCheck;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DevDailyCheckMapper
{
    List<DevDailyCheck> selectCheckByDate(@Param("checkDate") String checkDate);

    DevDailyCheck selectCheckById(Long id);

    DevDailyCheck selectCheckByDateAndEmployee(@Param("checkDate") String checkDate, @Param("employeeId") Long employeeId);

    int insertCheck(DevDailyCheck check);

    int updateCheck(DevDailyCheck check);

    int deleteCheckById(Long id);
}
