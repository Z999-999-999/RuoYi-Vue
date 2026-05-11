package com.ruoyi.bitable.domain;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 多维表格-记录对象 bitable_record
 */
public class BitableRecord
{
    private Long id;
    private Long tablePk;
    private String appToken;
    private String tableId;
    private String recordId;
    private String fieldsJson;
    private String createdBy;
    private Date createdTime;
    private String updatedBy;
    private Date updatedTime;

    // 查询参数（非数据库字段）
    private String keyword;
    private String startDate;
    private String endDate;
    private String filterField;
    private String filterValue;
    private String sortField;
    private String sortOrder;
    // 多条件筛选 [{field, operator, value}]
    private List<Map<String, String>> filters;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTablePk() { return tablePk; }
    public void setTablePk(Long tablePk) { this.tablePk = tablePk; }

    public String getAppToken() { return appToken; }
    public void setAppToken(String appToken) { this.appToken = appToken; }

    public String getTableId() { return tableId; }
    public void setTableId(String tableId) { this.tableId = tableId; }

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }

    public String getFieldsJson() { return fieldsJson; }
    public void setFieldsJson(String fieldsJson) { this.fieldsJson = fieldsJson; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Date getCreatedTime() { return createdTime; }
    public void setCreatedTime(Date createdTime) { this.createdTime = createdTime; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public Date getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(Date updatedTime) { this.updatedTime = updatedTime; }

    // 查询参数 getter/setter
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getFilterField() { return filterField; }
    public void setFilterField(String filterField) { this.filterField = filterField; }

    public String getFilterValue() { return filterValue; }
    public void setFilterValue(String filterValue) { this.filterValue = filterValue; }

    public String getSortField() { return sortField; }
    public void setSortField(String sortField) { this.sortField = sortField; }

    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }

    public List<Map<String, String>> getFilters() { return filters; }
    public void setFilters(List<Map<String, String>> filters) { this.filters = filters; }
}
