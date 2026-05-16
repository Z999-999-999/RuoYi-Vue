package com.ruoyi.bitable.domain;

/**
 * 多维表格字段类型枚举
 */
public enum FieldType
{
    TEXT(1, "多行文本"),
    NUMBER(2, "数字"),
    SINGLE_SELECT(3, "单选"),
    MULTI_SELECT(4, "多选"),
    DATE(5, "日期"),
    CHECKBOX(7, "复选框"),
    PHONE(13, "电话"),
    URL(15, "链接");

    private final int code;
    private final String label;

    FieldType(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    /** 根据 code 获取枚举，未知 code 返回 TEXT */
    public static FieldType fromCode(int code)
    {
        for (FieldType ft : values())
        {
            if (ft.code == code) return ft;
        }
        return TEXT;
    }

    /** 判断 code 是否为数字类型 */
    public static boolean isNumber(int code) { return code == NUMBER.code; }
}
