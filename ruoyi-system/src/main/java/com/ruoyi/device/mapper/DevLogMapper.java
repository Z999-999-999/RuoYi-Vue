package com.ruoyi.device.mapper;

import java.util.List;
import com.ruoyi.device.domain.DevLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DevLogMapper
{
    List<DevLog> selectLogList(DevLog log);

    int insertLog(DevLog log);

    /** 清空指定级别以上日志 */
    int clearLogs(@Param("scope") String scope);

    /** 统计error级别日志条数 */
    int countErrorLogs();

    List<String> selectDistinctModules();
}
