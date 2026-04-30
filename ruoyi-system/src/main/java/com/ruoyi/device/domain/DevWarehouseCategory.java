package com.ruoyi.device.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;

/**
 * 设备管理 - 库房分类实体
 */
public class DevWarehouseCategory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    /** work_account/intercept_account/xiaohongshu/abnormal/pending_online/maintenance/custom */
    private String type = "custom";

    private String color = "#3b82f6";

    private Integer sort = 0;

    private String delFlag;

    /** 关联库存数量（非持久化字段，查询时填充） */
    private Integer quantity = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
