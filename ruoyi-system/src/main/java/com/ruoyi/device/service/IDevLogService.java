package com.ruoyi.device.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.device.domain.DevLog;

public interface IDevLogService
{
    List<DevLog> selectLogList(DevLog log);

    void insertLog(String level, String module, String message, String detail);

    int clearLogs(String scope);

    int countErrorLogs();

    List<String> selectDistinctModules();

    /** 获取总览仪表盘聚合数据 */
    Map<String, Object> getDashboardData();
}
