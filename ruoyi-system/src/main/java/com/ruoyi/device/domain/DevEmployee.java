package com.ruoyi.device.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 设备管理 - 员工实体
 */
public class DevEmployee extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "员工姓名不能为空")
    @Size(max = 50, message = "员工姓名长度不能超过50个字符")
    private String name;

    @Size(max = 100, message = "部门名称长度不能超过100个字符")
    private String department;

    private Integer phoneTotal = 0;

    private Integer sort = 0;

    private String delFlag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Integer getPhoneTotal() { return phoneTotal; }
    public void setPhoneTotal(Integer phoneTotal) { this.phoneTotal = phoneTotal; }

    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
