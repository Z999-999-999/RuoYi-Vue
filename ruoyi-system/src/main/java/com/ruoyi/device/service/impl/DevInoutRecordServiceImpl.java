package com.ruoyi.device.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.device.domain.DevInoutRecord;
import com.ruoyi.device.domain.DevOutsideDevice;
import com.ruoyi.device.domain.DevWarehouseStock;
import com.ruoyi.device.mapper.DevEmployeeMapper;
import com.ruoyi.device.mapper.DevInoutRecordMapper;
import com.ruoyi.device.mapper.DevOutsideDeviceMapper;
import com.ruoyi.device.mapper.DevWarehouseMapper;
import com.ruoyi.device.service.IDevInoutRecordService;

/**
 * 进出库记录 Service 实现
 *
 * 核心业务流转（#10 联动）：
 *   出库→员工：员工 phone_total += qty，库房库存 -= qty
 *   出库→在外：库房库存 -= qty，同时创建 dev_outside_device 记录（记录 warehouseCategoryId）
 *   入库←员工：员工 phone_total -= qty，库房库存 += qty
 *   入库←在外：关闭对应的 dev_outside_device（actualReturnDate=今天），库房库存 += qty（从在外设备取 warehouseCategoryId）
 *   删除出库记录：库房库存恢复（从在外设备取 warehouseCategoryId），关闭对应的在外设备（重新打开）
 *   删除入库记录：库房库存恢复（若是入库←在外则重新打开在外设备，并从在外设备取 warehouseCategoryId 恢复库存）
 */
@Service
public class DevInoutRecordServiceImpl implements IDevInoutRecordService
{
    @Autowired
    private DevInoutRecordMapper recordMapper;

    @Autowired
    private DevEmployeeMapper employeeMapper;

    @Autowired
    private DevWarehouseMapper warehouseMapper;

    @Autowired
    private DevOutsideDeviceMapper outsideMapper;

    @Override
    public List<DevInoutRecord> selectRecordList(DevInoutRecord record)
    {
        return recordMapper.selectRecordList(record);
    }

    @Override
    public DevInoutRecord selectRecordById(Long id)
    {
        return recordMapper.selectRecordById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRecord(DevInoutRecord record)
    {
        record.setCreateBy(SecurityUtils.getUsername());
        record.setCreateTime(DateUtils.getNowDate());
        if (record.getRecordTime() == null)
        {
            record.setRecordTime(DateUtils.getNowDate());
        }

        int qty = record.getQuantity();
        Long warehouseCategoryId = record.getWarehouseCategoryId();

        // ===== 分类讨论主逻辑 =====
        if ("out".equals(record.getType()))
        {
            if ("outside".equals(record.getReceiverType()))
            {
                // 出库→在外：先 INSERT 进出库记录（拿到真实 ID），再联动创建在外设备
                if (warehouseCategoryId != null)
                {
                    DevWarehouseStock stock = warehouseMapper.selectStockByCategoryId(warehouseCategoryId);
                    if (stock == null || stock.getQuantity() < qty)
                    {
                        throw new ServiceException("库房库存不足，当前库存：" +
                                (stock == null ? 0 : stock.getQuantity()) + "，出库数量：" + qty);
                    }
                    warehouseMapper.updateQuantity(warehouseCategoryId, -qty);
                }
                int n = recordMapper.insertRecord(record);
                if (n > 0 && record.getId() != null)
                {
                    DevOutsideDevice od = new DevOutsideDevice();
                    od.setReason(record.getReceiverOutsideNote() != null ? record.getReceiverOutsideNote() : "出库在外");
                    od.setQuantity(qty);
                    od.setWarehouseCategoryId(warehouseCategoryId); // 记录库房分类，用于归还时恢复库存
                    od.setNote(record.getNote());
                    od.setOutDate(new Date());
                    od.setInoutRecordId(record.getId());
                    od.setCreateBy(record.getCreateBy());
                    od.setCreateTime(record.getCreateTime());
                    outsideMapper.insertOutsideDevice(od);
                }
                return n;
            }
            else
            {
                // 出库→员工/库房/直接：校验并扣库存
                if (warehouseCategoryId != null)
                {
                    DevWarehouseStock stock = warehouseMapper.selectStockByCategoryId(warehouseCategoryId);
                    if (stock == null || stock.getQuantity() < qty)
                    {
                        throw new ServiceException("库房库存不足，当前库存：" +
                                (stock == null ? 0 : stock.getQuantity()) + "，出库数量：" + qty);
                    }
                    warehouseMapper.updateQuantity(warehouseCategoryId, -qty);
                }
                if ("employee".equals(record.getReceiverType()) && record.getReceiverEmployeeId() != null)
                {
                    employeeMapper.updatePhoneTotal(record.getReceiverEmployeeId(), qty);
                }
            }
        }
        else if ("in".equals(record.getType()))
        {
            // 入库←在外（归还）：关闭在外设备 + 还回库房
            if ("outside".equals(record.getSenderType()) && record.getSenderEmployeeId() != null)
            {
                // senderEmployeeId 存的是出库记录的 ID（即 inout_record_id）
                Long outRecordId = record.getSenderEmployeeId();
                // 查询对应的在外设备记录，获取库房分类
                List<DevOutsideDevice> outsideList = outsideMapper.selectByInoutRecordId(outRecordId);
                Long wCatId = null;
                if (outsideList != null && !outsideList.isEmpty())
                {
                    wCatId = outsideList.get(0).getWarehouseCategoryId();
                }
                // 关闭在外设备记录（归还）
                DevOutsideDevice od = new DevOutsideDevice();
                od.setInoutRecordId(outRecordId);
                od.setActualReturnDate(new Date());
                od.setWarehouseCategoryId(wCatId);
                od.setUpdateBy(record.getCreateBy());
                od.setUpdateTime(record.getCreateTime());
                outsideMapper.closeOutsideDevices(od);
                // 归还的设备回到库房（优先用在外设备记录的库房分类）
                Long restoreCatId = (wCatId != null) ? wCatId : warehouseCategoryId;
                if (restoreCatId != null)
                {
                    warehouseMapper.updateQuantity(restoreCatId, qty);
                }
                return recordMapper.insertRecord(record);
            }
            else
            {
                // 入库来自员工：校验员工持有数
                if ("employee".equals(record.getSenderType()) && record.getSenderEmployeeId() != null)
                {
                    var employee = employeeMapper.selectEmployeeById(record.getSenderEmployeeId());
                    if (employee == null || employee.getPhoneTotal() < qty)
                    {
                        throw new ServiceException("员工持有数不足，当前持有：" +
                                (employee == null ? 0 : employee.getPhoneTotal()) + "，入库数量：" + qty);
                    }
                    employeeMapper.updatePhoneTotal(record.getSenderEmployeeId(), -qty);
                }
                // 库房库存 +qty
                if (warehouseCategoryId != null)
                {
                    warehouseMapper.updateQuantity(warehouseCategoryId, qty);
                }
            }
        }

        return recordMapper.insertRecord(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRecordById(Long id)
    {
        DevInoutRecord record = recordMapper.selectRecordById(id);
        if (record == null)
        {
            throw new ServiceException("记录不存在");
        }

        int qty = record.getQuantity();

        if ("out".equals(record.getType()))
        {
            // 出库→在外：先从在外设备取库房分类恢复库存，再关闭记录
            if ("outside".equals(record.getReceiverType()))
            {
                List<DevOutsideDevice> outsideList = outsideMapper.selectByInoutRecordId(id);
                Long wCatId = null;
                if (outsideList != null && !outsideList.isEmpty())
                {
                    wCatId = outsideList.get(0).getWarehouseCategoryId();
                }
                // 恢复库房库存
                if (wCatId != null)
                {
                    warehouseMapper.updateQuantity(wCatId, qty);
                }
                else if (record.getWarehouseCategoryId() != null)
                {
                    warehouseMapper.updateQuantity(record.getWarehouseCategoryId(), qty);
                }
                // 重新打开在外设备（回滚）
                DevOutsideDevice od = new DevOutsideDevice();
                od.setInoutRecordId(id);
                od.setActualReturnDate(null);
                od.setUpdateBy(SecurityUtils.getUsername());
                od.setUpdateTime(DateUtils.getNowDate());
                outsideMapper.reopenOutsideDevices(od);
            }
            else
            {
                // 出库→员工/库房/直接：恢复库房库存
                if (record.getWarehouseCategoryId() != null)
                {
                    warehouseMapper.updateQuantity(record.getWarehouseCategoryId(), qty);
                }
                // 恢复员工持有
                if ("employee".equals(record.getReceiverType()) && record.getReceiverEmployeeId() != null)
                {
                    employeeMapper.updatePhoneTotal(record.getReceiverEmployeeId(), -qty);
                }
            }
        }
        else if ("in".equals(record.getType()))
        {
            // 入库←在外：重新打开在外设备，并恢复库房库存
            if ("outside".equals(record.getSenderType()) && record.getSenderEmployeeId() != null)
            {
                Long outRecordId = record.getSenderEmployeeId();
                List<DevOutsideDevice> outsideList = outsideMapper.selectByInoutRecordId(outRecordId);
                Long wCatId = null;
                if (outsideList != null && !outsideList.isEmpty())
                {
                    wCatId = outsideList.get(0).getWarehouseCategoryId();
                }
                // 恢复库房库存（减去）
                if (wCatId != null)
                {
                    warehouseMapper.updateQuantity(wCatId, -qty);
                }
                else if (record.getWarehouseCategoryId() != null)
                {
                    warehouseMapper.updateQuantity(record.getWarehouseCategoryId(), -qty);
                }
                // 重新打开在外设备（回滚）
                DevOutsideDevice od = new DevOutsideDevice();
                od.setInoutRecordId(outRecordId);
                od.setActualReturnDate(null);
                od.setUpdateBy(SecurityUtils.getUsername());
                od.setUpdateTime(DateUtils.getNowDate());
                outsideMapper.reopenOutsideDevices(od);
            }
            else
            {
                // 入库←员工/库房/直接：恢复库房库存
                if (record.getWarehouseCategoryId() != null)
                {
                    warehouseMapper.updateQuantity(record.getWarehouseCategoryId(), -qty);
                }
                // 恢复员工持有
                if ("employee".equals(record.getSenderType()) && record.getSenderEmployeeId() != null)
                {
                    employeeMapper.updatePhoneTotal(record.getSenderEmployeeId(), qty);
                }
            }
        }

        return recordMapper.deleteRecordById(id);
    }

    @Override
    public List<DevInoutRecord> selectRecordsByEmployee(Long employeeId, Integer limit, String startDate, String endDate)
    {
        return recordMapper.selectRecordsByEmployee(employeeId, limit, startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> selectMonthlyOutRank(String yearMonth, Long inoutCategoryId)
    {
        return recordMapper.selectMonthlyOutRank(yearMonth, inoutCategoryId);
    }

    @Override
    public List<Map<String, Object>> selectMonthlyInRank(String yearMonth, Long inoutCategoryId)
    {
        return recordMapper.selectMonthlyInRank(yearMonth, inoutCategoryId);
    }
}
