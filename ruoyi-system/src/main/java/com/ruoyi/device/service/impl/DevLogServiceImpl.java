package com.ruoyi.device.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.device.domain.DevLog;
import com.ruoyi.device.domain.DevOutsideDevice;
import com.ruoyi.device.domain.DevWarehouseStock;
import com.ruoyi.device.mapper.DevLogMapper;
import com.ruoyi.device.mapper.DevOutsideDeviceMapper;
import com.ruoyi.device.mapper.DevEmployeeMapper;
import com.ruoyi.device.mapper.DevWarehouseMapper;
import com.ruoyi.device.mapper.DevInoutRecordMapper;
import com.ruoyi.device.service.IDevLogService;

@Service
public class DevLogServiceImpl implements IDevLogService
{
    /** 允许的日志清理范围 */
    private static final Set<String> ALLOWED_CLEAR_SCOPES = new HashSet<>(
        Arrays.asList("info", "warning", "error", "all"));

    @Autowired
    private DevLogMapper logMapper;

    @Autowired
    private DevEmployeeMapper employeeMapper;

    @Autowired
    private DevWarehouseMapper warehouseMapper;

    @Autowired
    private DevOutsideDeviceMapper outsideMapper;

    @Autowired
    private DevInoutRecordMapper recordMapper;

    @Override
    public List<DevLog> selectLogList(DevLog log)
    {
        return logMapper.selectLogList(log);
    }

    @Override
    public void insertLog(String level, String module, String message, String detail)
    {
        DevLog log = new DevLog();
        log.setLogTime(DateUtils.getNowDate());
        log.setLevel(level);
        log.setModule(module);
        log.setMessage(message);
        log.setDetail(detail);
        logMapper.insertLog(log);
    }

    @Override
    public int clearLogs(String scope)
    {
        if (scope == null || !ALLOWED_CLEAR_SCOPES.contains(scope))
        {
            throw new ServiceException("无效的清理范围: " + scope + "，允许值: info, warning, error, all");
        }
        return logMapper.clearLogs(scope);
    }

    @Override
    public int countErrorLogs()
    {
        return logMapper.countErrorLogs();
    }

    @Override
    public List<String> selectDistinctModules()
    {
        return logMapper.selectDistinctModules();
    }

    @Override
    public Map<String, Object> getDashboardData()
    {
        Map<String, Object> data = new HashMap<>();

        // 数字统计
        int employeeTotal = employeeMapper.sumPhoneTotal();
        int warehouseTotal = warehouseMapper.sumStock();
        int outsideTotal = outsideMapper.sumUnreturnedQuantity();

        data.put("employeeTotal", employeeTotal);
        data.put("warehouseTotal", warehouseTotal);
        data.put("outsideTotal", outsideTotal);

        // 今日进出统计
        Map<String, Object> todayStats = recordMapper.selectTodayStats();
        data.put("todayStats", todayStats);

        // 库房库存明细
        List<DevWarehouseStock> stocks = warehouseMapper.selectStockList();
        data.put("warehouseStocks", stocks);

        // 在外设备列表
        DevOutsideDevice outsideQuery = new DevOutsideDevice();
        List<DevOutsideDevice> outsideList = outsideMapper.selectOutsideList(outsideQuery);
        data.put("outsideList", outsideList);

        // 即将到期（7天内）
        List<DevOutsideDevice> soonReturn = new ArrayList<>();
        Date now = new Date();
        for (DevOutsideDevice device : outsideList)
        {
            if (device.getActualReturnDate() == null && device.getExpectedReturnDate() != null)
            {
                long diff = device.getExpectedReturnDate().getTime() - now.getTime();
                long days = diff / (1000 * 60 * 60 * 24);
                if (days >= 0 && days <= 7)
                {
                    soonReturn.add(device);
                }
            }
        }
        data.put("soonReturn", soonReturn);

        // error日志数量
        data.put("errorCount", countErrorLogs());

        // 近10条日志
        DevLog logQuery = new DevLog();
        List<DevLog> logs = logMapper.selectLogList(logQuery);
        data.put("recentLogs", logs.size() > 10 ? logs.subList(0, 10) : logs);

        return data;
    }
}
