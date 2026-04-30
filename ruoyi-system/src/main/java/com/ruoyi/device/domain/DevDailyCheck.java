package com.ruoyi.device.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotNull;

/**
 * 设备管理 - 每日核对实体
 */
public class DevDailyCheck extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "核对日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date checkDate;

    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    /** pending / confirmed / absent */
    private String status = "pending";

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkedAt;

    private String note;

    /** 非持久化：员工姓名 */
    private String employeeName;

    /** 非持久化：员工部门 */
    private String department;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Date getCheckDate() { return checkDate; }
    public void setCheckDate(Date checkDate) { this.checkDate = checkDate; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCheckedAt() { return checkedAt; }
    public void setCheckedAt(Date checkedAt) { this.checkedAt = checkedAt; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
