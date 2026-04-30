package com.ruoyi.device.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;

/**
 * 设备管理 - 账号平台配置实体
 */
public class DevAccountPlatform extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "平台编码不能为空")
    private String code;

    @NotBlank(message = "平台名称不能为空")
    private String label;

    private String color = "#94a3b8";

    /** 在用关键词 JSON数组字符串 */
    private String activeAliases = "[]";

    /** 不能用关键词 JSON数组字符串 */
    private String disabledAliases = "[]";

    /** 发作品关键词 JSON数组字符串 */
    private String postingAliases = "[]";

    private Integer sort = 0;

    private String delFlag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getActiveAliases() { return activeAliases; }
    public void setActiveAliases(String activeAliases) { this.activeAliases = activeAliases; }

    public String getDisabledAliases() { return disabledAliases; }
    public void setDisabledAliases(String disabledAliases) { this.disabledAliases = disabledAliases; }

    public String getPostingAliases() { return postingAliases; }
    public void setPostingAliases(String postingAliases) { this.postingAliases = postingAliases; }

    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
