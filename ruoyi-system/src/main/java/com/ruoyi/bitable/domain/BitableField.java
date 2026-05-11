package com.ruoyi.bitable.domain;

import java.util.Date;

/**
 * 多维表格-字段对象 bitable_field
 */
public class BitableField
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long tablePk;
    private String appToken;
    private String tableId;
    private String fieldId;
    private String fieldName;
    private Integer type;
    private String propertyJson;
    private String uiType;
    private Date createdTime;
    private Date updatedTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTablePk() { return tablePk; }
    public void setTablePk(Long tablePk) { this.tablePk = tablePk; }

    public String getAppToken() { return appToken; }
    public void setAppToken(String appToken) { this.appToken = appToken; }

    public String getTableId() { return tableId; }
    public void setTableId(String tableId) { this.tableId = tableId; }

    public String getFieldId() { return fieldId; }
    public void setFieldId(String fieldId) { this.fieldId = fieldId; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }

    public String getPropertyJson() { return propertyJson; }
    public void setPropertyJson(String propertyJson) { this.propertyJson = propertyJson; }

    public String getUiType() { return uiType; }
    public void setUiType(String uiType) { this.uiType = uiType; }

    public Date getCreatedTime() { return createdTime; }
    public void setCreatedTime(Date createdTime) { this.createdTime = createdTime; }

    public Date getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(Date updatedTime) { this.updatedTime = updatedTime; }
}
