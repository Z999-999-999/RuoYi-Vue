package com.ruoyi.device.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;

/**
 * 设备管理 - 进出库原因分类实体
 */
public class DevInoutCategory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "原因名称不能为空")
    private String name;

    /** 类型: in / out / both */
    private String type;

    private Integer sort = 0;

    private String delFlag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
