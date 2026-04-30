package com.ruoyi.device.mapper;

import java.util.List;
import com.ruoyi.device.domain.DevInoutCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DevInoutCategoryMapper
{
    List<DevInoutCategory> selectCategoryList(DevInoutCategory category);

    DevInoutCategory selectCategoryById(Long id);

    int insertCategory(DevInoutCategory category);

    int updateCategory(DevInoutCategory category);

    int deleteCategoryById(Long id);
}
