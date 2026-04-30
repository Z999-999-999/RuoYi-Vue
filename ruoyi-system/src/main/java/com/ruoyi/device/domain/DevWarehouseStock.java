package com.ruoyi.device.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 设备管理 - 库房库存实体
 */
public class DevWarehouseStock
{
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long categoryId;

    private Integer quantity = 0;

    /** 异常设备子类型: pending_verify/pending_unban/other */
    private String subType;

    private String note;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 非持久化：分类名称 */
    private String categoryName;

    /** 非持久化：分类颜色 */
    private String categoryColor;

    /** 非持久化：分类类型 */
    private String categoryType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getSubType() { return subType; }
    public void setSubType(String subType) { this.subType = subType; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getCategoryColor() { return categoryColor; }
    public void setCategoryColor(String categoryColor) { this.categoryColor = categoryColor; }

    public String getCategoryType() { return categoryType; }
    public void setCategoryType(String categoryType) { this.categoryType = categoryType; }
}
