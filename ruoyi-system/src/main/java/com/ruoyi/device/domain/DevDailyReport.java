package com.ruoyi.device.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotNull;

/**
 * 设备管理 - 每日汇报实体
 */
public class DevDailyReport extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "汇报日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date reportDate;

    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    private Integer phoneCount = 0;

    /** 平台统计 JSON: PlatformAccountStat[] */
    private String platformStats;

    /** 非持久化：员工姓名 */
    private String employeeName;

    /** 非持久化：员工部门 */
    private String department;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Date getReportDate() { return reportDate; }
    public void setReportDate(Date reportDate) { this.reportDate = reportDate; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Integer getPhoneCount() { return phoneCount; }
    public void setPhoneCount(Integer phoneCount) { this.phoneCount = phoneCount; }

    public String getPlatformStats() { return platformStats; }
    public void setPlatformStats(String platformStats) { this.platformStats = platformStats; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
