package com.ruoyi.bitable.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.bitable.domain.BitableField;
import com.ruoyi.bitable.mapper.BitableFieldMapper;
import com.ruoyi.bitable.service.IBitableFieldService;

/**
 * 多维表格-字段Service实现
 */
@Service
public class BitableFieldServiceImpl implements IBitableFieldService
{
    @Autowired
    private BitableFieldMapper fieldMapper;

    @Override
    public BitableField selectFieldById(Long id)
    {
        return fieldMapper.selectFieldById(id);
    }

    @Override
    public BitableField selectFieldByTokenTableFieldId(String appToken, String tableId, String fieldId)
    {
        return fieldMapper.selectFieldByTokenTableFieldId(appToken, tableId, fieldId);
    }

    @Override
    public List<BitableField> selectFieldList(String appToken, String tableId)
    {
        return fieldMapper.selectFieldList(appToken, tableId);
    }

    @Override
    public int insertField(BitableField field)
    {
        if (field.getFieldId() == null || field.getFieldId().isEmpty())
        {
            // 自动生成 field_id: fld + 8位随机字符
            field.setFieldId("fld" + IdUtils.fastSimpleUUID().substring(0, 8));
        }
        if (field.getType() == null)
        {
            field.setType(1); // 默认多行文本
        }
        field.setCreatedTime(new Date());
        return fieldMapper.insertField(field);
    }

    @Override
    public int updateField(BitableField field)
    {
        field.setUpdatedTime(new Date());
        return fieldMapper.updateField(field);
    }

    @Override
    public int deleteFieldById(Long id)
    {
        return fieldMapper.deleteFieldById(id);
    }

    @Override
    public int deleteFieldsByTable(String appToken, String tableId)
    {
        return fieldMapper.deleteFieldsByTable(appToken, tableId);
    }
}
