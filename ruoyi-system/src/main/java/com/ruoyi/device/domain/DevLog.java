package com.ruoyi.device.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 设备管理 - 操作日志实体
 */
public class DevLog
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date logTime;

    /** info / warning / error */
    private String level = "info";

    private String module;

    private String message;

    private String detail;

    // 查询参数（非持久化）
    private String startTime;
    private String endTime;
    private String keyword;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Date getLogTime() { return logTime; }
    public void setLogTime(Date logTime) { this.logTime = logTime; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}
