package com.ruoyi.web.controller.bitable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.bitable.domain.BitableApp;
import com.ruoyi.bitable.domain.BitableField;
import com.ruoyi.bitable.domain.BitableRecord;
import com.ruoyi.bitable.domain.BitableTable;
import com.ruoyi.bitable.service.IBitableAppService;
import com.ruoyi.bitable.service.IBitableFieldService;
import com.ruoyi.bitable.service.IBitableRecordService;
import com.ruoyi.bitable.service.IBitableTableService;

/**
 * 社媒助手数据上报接口
 * 
 * 供社媒助手等外部工具调用，不走RuoYi认证，使用api_key验证。
 * 路径: POST /api/bitable/reporting/{app_token}/{table_key}
 * 请求头: Authorization: Bearer {api_key}
 * 请求体: { extra, meta, list, remark, version }
 */
@Anonymous
@RestController
@RequestMapping("/api/bitable/reporting")
public class BitableReportingController
{
    @Autowired
    private IBitableAppService appService;

    @Autowired
    private IBitableTableService tableService;

    @Autowired
    private IBitableFieldService fieldService;

    @Autowired
    private IBitableRecordService recordService;

    /**
     * 接收社媒助手数据上报
     */
    @PostMapping("/{app_token}/{table_key}")
    public Map<String, Object> reporting(
            @PathVariable("app_token") String appToken,
            @PathVariable("table_key") String tableKey,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body)
    {
        try
        {
            // ==================== 参数校验（防御） ====================
            if (appToken == null || appToken.trim().isEmpty() || tableKey == null || tableKey.trim().isEmpty())
            {
                return error(400, "app_token 和 table_key 不能为空");
            }
            if (body == null)
            {
                return error(400, "请求体不能为空");
            }

            // 1. 验证 api_key
            String apiKey = extractApiKey(authHeader);
            if (apiKey == null)
            {
                return error(401, "缺少Authorization请求头");
            }
            BitableApp app = appService.selectAppByToken(appToken);
            if (app == null || !apiKey.equals(app.getApiKey()))
            {
                return error(403, "api_key无效或应用不存在");
            }

            // 2. 查找或自动创建数据表
        BitableTable table = tableService.selectTableByTokenAndId(appToken, tableKey);
        if (table == null)
        {
            // 先检查是否有已软删除的同名表，有则恢复
            BitableTable deletedTable = tableService.selectDeletedTableByTokenAndId(appToken, tableKey);
            if (deletedTable != null)
            {
                // 恢复软删除的表
                tableService.restoreTableById(deletedTable.getId());
                table = tableService.selectTableByTokenAndId(appToken, tableKey);
            }
            else
            {
                // 首次上报，自动创建数据表
                table = new BitableTable();
                table.setAppId(app.getId());
                table.setAppToken(appToken);
                table.setTableId(tableKey);
                table.setName("数据表-" + tableKey);
                table.setCreateBy("reporting");
                table.setCreateTime(new Date());
                tableService.insertTable(table);
                // 重新查询获取完整对象（含自增id）
                table = tableService.selectTableByTokenAndId(appToken, tableKey);
            }
        }

        // 3. 解析 meta → 自动创建/更新字段 + 收集日期字段名
        // 支持两种格式：(1) meta 直接是数组；(2) meta 是对象，内含 fields 数组
        Object metaRaw = body.get("meta");
        List<Map<String, Object>> metaList = null;
        Set<String> dateFieldKeys = new HashSet<>();
        if (metaRaw instanceof List)
        {
            metaList = (List<Map<String, Object>>) metaRaw;
        }
        else if (metaRaw instanceof Map)
        {
            Object fields = ((Map<?, ?>) metaRaw).get("fields");
            if (fields instanceof List)
            {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> tmp = (List<Map<String, Object>>) fields;
                metaList = tmp;
            }
        }
        if (metaList != null)
        {
            for (Map<String, Object> metaItem : metaList)
            {
                String fieldKey = (String) metaItem.get("key");
                String fieldName = (String) metaItem.get("name");
                if (fieldKey == null || fieldKey.isEmpty()) continue;

                int fieldType = inferFieldType(metaItem);
                // 记录日期类型字段
                if (fieldType == 5)
                {
                    dateFieldKeys.add(fieldKey);
                }

                // 检查字段是否已存在（用field_id = fieldKey来匹配）
                BitableField existingField = fieldService.selectFieldByTokenTableFieldId(appToken, tableKey, fieldKey);
                if (existingField == null)
                {
                    // 自动创建新字段
                    BitableField field = new BitableField();
                    field.setTablePk(table.getId());
                    field.setAppToken(appToken);
                    field.setTableId(tableKey);
                    field.setFieldId(fieldKey);
                    field.setFieldName(fieldName != null ? fieldName : fieldKey);
                    field.setType(fieldType);
                    field.setCreatedTime(new Date());
                    fieldService.insertField(field);
                }
                else if (fieldName != null && !fieldName.equals(existingField.getFieldName()))
                {
                    // 更新字段名称（如果meta中的name有变化）
                    existingField.setFieldName(fieldName);
                    existingField.setUpdatedTime(new Date());
                    fieldService.updateField(existingField);
                }
            }
        }

        // 4. 解析 list → 批量插入记录（先规范化日期字段，支持去重）
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) body.get("list");
        int insertCount = 0;
        int updateCount = 0;
        int skipCount = 0;
        if (dataList != null && !dataList.isEmpty())
        {
            List<BitableRecord> recordsToInsert = new ArrayList<>();
            List<BitableRecord> recordsToUpdate = new ArrayList<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            // 只加载 record_id 用于快速去重（不再加载整条 fields_json，避免大表 OOM）
            List<BitableRecord> existingRecordIds = recordService.selectAllRecords(appToken, tableKey);
            // 构建 record_id → existingRecord 的 HashMap（用于快速去重查找）
            java.util.Map<String, BitableRecord> recordIdMap = new java.util.HashMap<>();
            for (BitableRecord r : existingRecordIds) {
                if (r.getRecordId() != null && !r.getRecordId().isEmpty()) {
                    recordIdMap.put(r.getRecordId(), r);
                }
            }
            // 用 Set<String> 存储已有 fields_json 的哈希值，用于整行比对去重（限制内存占用）
            java.util.Set<String> existingJsonHashes = new java.util.HashSet<>();
            for (BitableRecord r : existingRecordIds) {
                if (r.getFieldsJson() != null) {
                    existingJsonHashes.add(String.valueOf(r.getFieldsJson().hashCode()));
                }
            }

            for (Map<String, Object> dataItem : dataList)
            {
                // 规范化日期字段：时间戳数字 → 格式化字符串
                normalizeDateFields(dataItem, dateFieldKeys, dateFormat);

                // 尝试从数据中提取唯一标识（智能去重：评论ID > 类型ID > cid > 整行比对）
                String recordId = extractRecordId(dataItem);
                String currentJson = toJsonString(dataItem);

                // 检查是否已存在：优先用 record_id 精确匹配
                BitableRecord existingRecord = null;
                if (recordId != null && !recordId.isEmpty())
                {
                    existingRecord = recordIdMap.get(recordId);
                }
                // 无法提取唯一标识时，用 JSON 哈希值比对去重（避免全表扫描）
                if (existingRecord == null && (recordId == null || recordId.isEmpty()))
                {
                    if (existingJsonHashes.contains(String.valueOf(currentJson.hashCode()))) {
                        // 哈希命中，再精确比对（避免哈希碰撞误判）
                        for (BitableRecord oldRec : existingRecordIds)
                        {
                            if (currentJson.equals(oldRec.getFieldsJson()))
                            {
                                existingRecord = oldRec;
                                break;
                            }
                        }
                    }
                }
                
                if (existingRecord != null)
                {
                    // 已存在，更新记录
                    existingRecord.setFieldsJson(currentJson);
                    existingRecord.setUpdatedBy("reporting");
                    existingRecord.setUpdatedTime(new Date());
                    recordsToUpdate.add(existingRecord);
                    updateCount++;
                }
                else
                {
                    // 不存在，新增记录
                    BitableRecord record = new BitableRecord();
                    record.setTablePk(table.getId());
                    record.setAppToken(appToken);
                    record.setTableId(tableKey);
                    record.setRecordId(recordId); // 使用提取的ID，可能为null
                    record.setFieldsJson(currentJson);
                    record.setCreatedBy("reporting");
                    record.setCreatedTime(new Date());
                    recordsToInsert.add(record);
                    insertCount++;
                }
            }
            
            // 批量插入新记录
            if (!recordsToInsert.isEmpty())
            {
                int batchSize = 500;
                for (int i = 0; i < recordsToInsert.size(); i += batchSize)
                {
                    int end = Math.min(i + batchSize, recordsToInsert.size());
                    List<BitableRecord> batch = recordsToInsert.subList(i, end);
                    recordService.insertRecords(batch);
                }
            }
            
            // 逐条更新已存在的记录
            for (BitableRecord record : recordsToUpdate)
            {
                recordService.updateRecord(record);
            }
        }

        // 5. 返回成功响应
        String msg = "成功处理" + (insertCount + updateCount) + "条数据";
        if (insertCount > 0) msg += "（新增" + insertCount + "条";
        if (updateCount > 0) msg += "，更新" + updateCount + "条";
        if (insertCount > 0 || updateCount > 0) msg += "）";
        return success(msg);
        }
        catch (java.lang.OutOfMemoryError oome)
        {
            System.err.println("[BitableReporting] 数据量过大，内存溢出: " + oome.getMessage());
            return error(413, "数据量过大，请减少每次上报的记录数量（建议不超过5000条）");
        }
        catch (Exception e)
        {
            System.err.println("[BitableReporting] 处理数据时发生异常: " + e.getClass().getName() + ": " + e.getMessage());
            // 不泄露堆栈，只返回友好错误
            return error(500, "服务器处理数据时发生异常，请稍后重试");
        }
    }

    /**
     * 规范化日期字段：时间戳数字 → 格式化字符串
     * 递归处理嵌套的 Map 和 List
     */
    private void normalizeDateFields(Object obj, Set<String> dateFieldKeys, SimpleDateFormat dateFormat)
    {
        if (obj instanceof Map)
        {
            Map<String, Object> map = (Map<String, Object>) obj;
            for (Map.Entry<String, Object> entry : map.entrySet())
            {
                String key = entry.getKey();
                Object value = entry.getValue();
                // 如果是日期字段且值是数字，转换为格式化字符串
                if (dateFieldKeys.contains(key) && value instanceof Number)
                {
                    long timestamp = ((Number) value).longValue();
                    // 只处理看起来像毫秒时间戳的数字（大于10^12）
                    if (timestamp > 1000000000000L)
                    {
                        map.put(key, dateFormat.format(new Date(timestamp)));
                    }
                }
                else if (value instanceof Map || value instanceof List)
                {
                    normalizeDateFields(value, dateFieldKeys, dateFormat);
                }
            }
        }
        else if (obj instanceof List)
        {
            for (Object item : (List<?>) obj)
            {
                if (item instanceof Map || item instanceof List)
                {
                    normalizeDateFields(item, dateFieldKeys, dateFormat);
                }
            }
        }
    }

    /**
     * 从数据中提取唯一标识（用于去重）
     * 优先级：
     * 1. 字段名含"评论ID" → 用评论ID作为唯一标识
     * 2. 字段名含"类型"（如笔记类型/视频类型）→ 用对应的ID字段（如笔记ID/视频ID）作为唯一标识
     * 3. cid / id / _id 固定字段
     * 4. 都没有则返回 null（后续用整行内容比对去重）
     */
    private String extractRecordId(Map<String, Object> dataItem)
    {
        // 优先级1：查找"评论ID"字段
        for (String key : dataItem.keySet())
        {
            Object val = dataItem.get(key);
            if (val != null && key.contains("评论ID") && !String.valueOf(val).isEmpty())
            {
                return "comment_" + String.valueOf(val);
            }
        }

        // 优先级2：查找"类型"字段，提取对应ID
        for (String key : dataItem.keySet())
        {
            if (key.contains("类型"))
            {
                Object typeVal = dataItem.get(key);
                if (typeVal == null) continue;
                String typeName = String.valueOf(typeVal).trim();
                // 提取类型名前缀，如"笔记类型"→"笔记"，"视频类型"→"视频"
                String prefix = typeName.replace("类型", "").trim();
                if (!prefix.isEmpty())
                {
                    // 查找 "笔记ID" / "视频ID" 等对应字段
                    String idFieldName = prefix + "ID";
                    for (String k : dataItem.keySet())
                    {
                        if (k.contains(idFieldName))
                        {
                            Object idVal = dataItem.get(k);
                            if (idVal != null && !String.valueOf(idVal).isEmpty())
                            {
                                return prefix + "_" + String.valueOf(idVal);
                            }
                        }
                    }
                }
            }
        }

        // 优先级3：固定字段
        Object cid = dataItem.get("cid");
        if (cid != null) return String.valueOf(cid);
        
        Object id = dataItem.get("id");
        if (id != null) return String.valueOf(id);
        
        Object _id = dataItem.get("_id");
        if (_id != null) return String.valueOf(_id);
        
        return null;
    }

    /**
     * 从 Authorization 头提取 api_key
     * 支持格式: "Bearer xxx" 或直接 "xxx"
     */
    private String extractApiKey(String authHeader)
    {
        if (authHeader == null || authHeader.isEmpty())
        {
            return null;
        }
        if (authHeader.startsWith("Bearer "))
        {
            return authHeader.substring(7).trim();
        }
        return authHeader.trim();
    }

    /**
     * 根据 meta 信息推断字段类型
     */
    private int inferFieldType(Map<String, Object> metaItem)
    {
        String uiType = (String) metaItem.get("ui_type");
        if (uiType != null)
        {
            switch (uiType)
            {
                case "number": return 2;
                case "single_select": return 3;
                case "multi_select": return 4;
                case "date": return 5;
                case "checkbox": return 7;
                case "phone": return 13;
                case "url": return 15;
                default: return 1; // 多行文本
            }
        }
        // 没有 ui_type 时，根据字段名称关键词推断类型
        String name = (String) metaItem.get("name");
        if (name != null)
        {
            String lowerName = name.toLowerCase();
            // 数字类型关键词
            if (lowerName.contains("数") || lowerName.contains("量") || lowerName.contains("count")
                || lowerName.contains("amount") || lowerName.contains("total") || lowerName.contains("num")
                || lowerName.contains("score") || lowerName.contains("rate") || lowerName.contains("ratio"))
            {
                return 2; // 数字
            }
            // 日期类型关键词
            if (lowerName.contains("时间") || lowerName.contains("日期") || lowerName.contains("date")
                || lowerName.contains("time") || lowerName.contains("at"))
            {
                return 5; // 日期
            }
        }
        return 1; // 默认多行文本
    }

    /**
     * 完整JSON序列化（支持嵌套Map/List/Collection）
     */
    private String toJsonString(Object data)
    {
        if (data == null) return "null";
        if (data instanceof Map)
        {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Object entryObj : ((Map<?, ?>) data).entrySet())
            {
                if (!first) sb.append(",");
                first = false;
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) entryObj;
                sb.append('\"').append(escapeJson(String.valueOf(entry.getKey()))).append('\"').append(':');
                sb.append(toJsonString(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }
        if (data instanceof Iterable)
        {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : (Iterable<?>) data)
            {
                if (!first) sb.append(",");
                first = false;
                sb.append(toJsonString(item));
            }
            sb.append("]");
            return sb.toString();
        }
        if (data instanceof Number || data instanceof Boolean)
        {
            return String.valueOf(data);
        }
        return "\"" + escapeJson(String.valueOf(data)) + "\"";
    }

    private String escapeJson(String s)
    {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private Map<String, Object> success(String msg)
    {
        return Map.of("code", 0, "msg", msg);
    }

    private Map<String, Object> error(int code, String msg)
    {
        return Map.of("code", code, "msg", msg);
    }
}
