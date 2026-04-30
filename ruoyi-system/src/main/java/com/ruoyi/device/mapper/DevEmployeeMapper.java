package com.ruoyi.device.mapper;

import java.util.List;
import com.ruoyi.device.domain.DevEmployee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 员工 Mapper
 */
@Mapper
public interface DevEmployeeMapper
{
    List<DevEmployee> selectEmployeeList(DevEmployee employee);

    DevEmployee selectEmployeeById(Long id);

    int insertEmployee(DevEmployee employee);

    int updateEmployee(DevEmployee employee);

    int deleteEmployeeById(Long id);

    /** 批量更新员工持有数（进出库时调用） */
    int updatePhoneTotal(@Param("id") Long id, @Param("delta") int delta);

    /** 查询员工持有总数之和 */
    int sumPhoneTotal();

    /** 统计员工的每日汇报记录数 */
    int countDailyReportsByEmployeeId(@Param("employeeId") Long employeeId);

    /** 统计员工的每日核对记录数 */
    int countDailyChecksByEmployeeId(@Param("employeeId") Long employeeId);
}
