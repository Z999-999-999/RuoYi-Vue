package com.ruoyi.bitable.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 多维表格应用对象 bitable_app
 */
public class BitableApp extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private String appToken;
    private String name;
    private String description;
    private String apiKey;
    private Long tableCount;
    private Long recordCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAppToken() { return appToken; }
    public void setAppToken(String appToken) { this.appToken = appToken; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public Long getTableCount() { return tableCount; }
    public void setTableCount(Long tableCount) { this.tableCount = tableCount; }

    public Long getRecordCount() { return recordCount; }
    public void setRecordCount(Long recordCount) { this.recordCount = recordCount; }
}
