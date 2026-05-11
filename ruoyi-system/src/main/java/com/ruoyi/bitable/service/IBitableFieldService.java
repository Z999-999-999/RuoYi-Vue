package com.ruoyi.bitable.service;

import java.util.List;
import com.ruoyi.bitable.domain.BitableField;

/**
 * 多维表格-字段Service接口
 */
public interface IBitableFieldService
{
    public BitableField selectFieldById(Long id);
    public BitableField selectFieldByTokenTableFieldId(String appToken, String tableId, String fieldId);
    public List<BitableField> selectFieldList(String appToken, String tableId);
    public int insertField(BitableField field);
    public int updateField(BitableField field);
    public int deleteFieldById(Long id);
    public int deleteFieldsByTable(String appToken, String tableId);
}
