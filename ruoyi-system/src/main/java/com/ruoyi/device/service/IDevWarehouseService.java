package com.ruoyi.device.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.device.domain.DevWarehouseCategory;
import com.ruoyi.device.domain.DevWarehouseStock;

public interface IDevWarehouseService
{
    List<DevWarehouseCategory> selectCategoryList(DevWarehouseCategory category);

    DevWarehouseCategory selectCategoryById(Long id);

    int insertCategory(DevWarehouseCategory category);

    int updateCategory(DevWarehouseCategory category);

    int deleteCategoryById(Long id);

    List<DevWarehouseStock> selectStockList();

    /** 批量更新库存（事务） */
    void updateStockBatch(List<Map<String, Object>> items, String updateBy);

    int sumStock();
}
