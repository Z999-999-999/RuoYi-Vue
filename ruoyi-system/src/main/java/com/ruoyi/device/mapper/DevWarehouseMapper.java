package com.ruoyi.device.mapper;

import java.util.List;
import com.ruoyi.device.domain.DevWarehouseCategory;
import com.ruoyi.device.domain.DevWarehouseStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DevWarehouseMapper
{
    // ---- 库房分类 ----
    List<DevWarehouseCategory> selectCategoryList(DevWarehouseCategory category);

    DevWarehouseCategory selectCategoryById(Long id);

    int insertCategory(DevWarehouseCategory category);

    int updateCategory(DevWarehouseCategory category);

    int deleteCategoryById(Long id);

    // ---- 库房库存 ----
    List<DevWarehouseStock> selectStockList();

    DevWarehouseStock selectStockByCategoryId(Long categoryId);

    int insertStock(DevWarehouseStock stock);

    int updateStock(DevWarehouseStock stock);

    /** 库存增减（进出库时调用，delta 可正可负） */
    int updateQuantity(@Param("categoryId") Long categoryId, @Param("delta") int delta);

    /** 获取库房库存总数 */
    int sumStock();
}
