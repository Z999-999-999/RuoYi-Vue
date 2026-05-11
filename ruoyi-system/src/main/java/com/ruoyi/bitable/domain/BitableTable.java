package com.ruoyi.bitable.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 多维表格-数据表对象 bitable_table
 */
public class BitableTable extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long appId;
    private String appToken;
    private String tableId;
    private String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAppId() { return appId; }
    public void setAppId(Long appId) { this.appId = appId; }

    public String getAppToken() { return appToken; }
    public void setAppToken(String appToken) { this.appToken = appToken; }

    public String getTableId() { return tableId; }
    public void setTableId(String tableId) { this.tableId = tableId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
