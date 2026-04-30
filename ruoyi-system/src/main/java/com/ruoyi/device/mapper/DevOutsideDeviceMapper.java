package com.ruoyi.device.mapper;

import java.util.List;
import com.ruoyi.device.domain.DevOutsideDevice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DevOutsideDeviceMapper
{
    List<DevOutsideDevice> selectOutsideList(DevOutsideDevice device);

    DevOutsideDevice selectOutsideById(Long id);

    int insertOutside(DevOutsideDevice device);

    int updateOutside(DevOutsideDevice device);

    int deleteOutsideById(Long id);

    /** 获取在外未归还设备总数 */
    int sumUnreturnedQuantity();

    /** 创建在外设备记录（联动：出库→在外时自动调用） */
    int insertOutsideDevice(DevOutsideDevice device);

    /** 关闭在外设备记录（联动：入库←在外时自动调用） */
    int closeOutsideDevices(DevOutsideDevice device);

    /** 重新打开在外设备记录（联动：删除进出库记录时回滚） */
    int reopenOutsideDevices(DevOutsideDevice device);

    /** 按 inout_record_id 查询在外设备记录 */
    List<DevOutsideDevice> selectByInoutRecordId(Long inoutRecordId);
}
