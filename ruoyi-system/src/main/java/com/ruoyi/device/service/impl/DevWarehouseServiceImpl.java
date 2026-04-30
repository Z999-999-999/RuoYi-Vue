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
import com.ruoyi.device.domain.DevWarehouseCategory;
import com.ruoyi.device.domain.DevWarehouseStock;
import com.ruoyi.device.mapper.DevInoutRecordMapper;
import com.ruoyi.device.mapper.DevWarehouseMapper;
import com.ruoyi.device.service.IDevWarehouseService;

@Service
public class DevWarehouseServiceImpl implements IDevWarehouseService
{
    @Autowired
    private DevWarehouseMapper warehouseMapper;

    @Autowired
    private DevInoutRecordMapper recordMapper;

    @Override
    public List<DevWarehouseCategory> selectCategoryList(DevWarehouseCategory category)
    {
        return warehouseMapper.selectCategoryList(category);
    }

    @Override
    public DevWarehouseCategory selectCategoryById(Long id)
    {
        return warehouseMapper.selectCategoryById(id);
    }

    @Override
    public int insertCategory(DevWarehouseCategory category)
    {
        category.setCreateBy(SecurityUtils.getUsername());
        category.setCreateTime(DateUtils.getNowDate());
        int rows = warehouseMapper.insertCategory(category);
        // 同时初始化库存记录
        if (rows > 0)
        {
            DevWarehouseStock stock = new DevWarehouseStock();
            stock.setCategoryId(category.getId());
            stock.setQuantity(0);
            stock.setUpdateBy(SecurityUtils.getUsername());
            stock.setUpdateTime(DateUtils.getNowDate());
            warehouseMapper.insertStock(stock);
        }
        return rows;
    }

    @Override
    public int updateCategory(DevWarehouseCategory category)
    {
        category.setUpdateBy(SecurityUtils.getUsername());
        category.setUpdateTime(DateUtils.getNowDate());
        return warehouseMapper.updateCategory(category);
    }

    @Override
    public int deleteCategoryById(Long id)
    {
        // 检查库存：有库存则禁止删除
        DevWarehouseStock stock = warehouseMapper.selectStockByCategoryId(id);
        if (stock != null && stock.getQuantity() != null && stock.getQuantity() > 0)
        {
            throw new ServiceException("该分类下还有 " + stock.getQuantity() + " 台库存设备，请先清空库存再删除");
        }
        // 检查是否有进出库记录引用了该分类
        int refCount = recordMapper.countByWarehouseCategoryId(id);
        if (refCount > 0)
        {
            throw new ServiceException("该库房分类已被 " + refCount + " 条进出库记录引用，无法删除");
        }
        return warehouseMapper.deleteCategoryById(id);
    }

    @Override
    public List<DevWarehouseStock> selectStockList()
    {
        return warehouseMapper.selectStockList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockBatch(List<Map<String, Object>> items, String updateBy)
    {
        Date now = DateUtils.getNowDate();
        for (Map<String, Object> item : items)
        {
            Long categoryId = Long.valueOf(item.get("categoryId").toString());
            Integer quantity = Integer.valueOf(item.get("quantity").toString());
            String subType = item.containsKey("subType") ? (String) item.get("subType") : null;
            String note = item.containsKey("note") ? (String) item.get("note") : null;

            DevWarehouseStock stock = warehouseMapper.selectStockByCategoryId(categoryId);
            if (stock != null)
            {
                stock.setQuantity(Math.max(quantity, 0));
                stock.setSubType(subType);
                stock.setNote(note);
                stock.setUpdateBy(updateBy);
                stock.setUpdateTime(now);
                warehouseMapper.updateStock(stock);
            }
            else
            {
                stock = new DevWarehouseStock();
                stock.setCategoryId(categoryId);
                stock.setQuantity(Math.max(quantity, 0));
                stock.setSubType(subType);
                stock.setNote(note);
                stock.setUpdateBy(updateBy);
                stock.setUpdateTime(now);
                warehouseMapper.insertStock(stock);
            }
        }
    }

    @Override
    public int sumStock()
    {
        return warehouseMapper.sumStock();
    }
}
