package com.ruoyi.web.controller.bitable;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.bitable.domain.BitableApp;
import com.ruoyi.bitable.domain.BitableField;
import com.ruoyi.bitable.domain.BitableRecord;
import com.ruoyi.bitable.domain.BitableTable;
import com.ruoyi.bitable.service.IBitableAppService;
import com.ruoyi.bitable.service.IBitableFieldService;
import com.ruoyi.bitable.service.IBitableRecordService;
import com.ruoyi.bitable.service.IBitableTableService;

/**
 * 多维表格管理 Controller（RuoYi认证）
 */
@RestController
@RequestMapping("/bitable")
public class BitableAppController extends BaseController
{
    @Autowired
    private IBitableAppService appService;

    @Autowired
    private IBitableTableService tableService;

    @Autowired
    private IBitableFieldService fieldService;

    @Autowired
    private IBitableRecordService recordService;

    // ==================== 应用管理 ====================

    @PreAuthorize("@ss.hasPermi('bitable:app:list')")
    @GetMapping("/app/list")
    public TableDataInfo appList(BitableApp app)
    {
        startPage();
        List<BitableApp> list = appService.selectAppList(app);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('bitable:app:list')")
    @GetMapping("/app/{id}")
    public AjaxResult getApp(@PathVariable Long id)
    {
        return AjaxResult.success(appService.selectAppById(id));
    }

    @PreAuthorize("@ss.hasPermi('bitable:app:add')")
    @Log(title = "多维表格应用", businessType = BusinessType.INSERT)
    @PostMapping("/app")
    public AjaxResult addApp(@RequestBody BitableApp app)
    {
        return toAjax(appService.insertApp(app));
    }

    @PreAuthorize("@ss.hasPermi('bitable:app:edit')")
    @Log(title = "多维表格应用", businessType = BusinessType.UPDATE)
    @PutMapping("/app")
    public AjaxResult editApp(@RequestBody BitableApp app)
    {
        return toAjax(appService.updateApp(app));
    }

    @PreAuthorize("@ss.hasPermi('bitable:app:remove')")
    @Log(title = "多维表格应用", businessType = BusinessType.DELETE)
    @DeleteMapping("/app/{id}")
    public AjaxResult removeApp(@PathVariable Long id)
    {
        return toAjax(appService.deleteAppById(id));
    }

    // ==================== 数据表管理 ====================

    /**
     * 校验 appToken 是否属于一个真实应用，若不通过直接抛异常
     */
    private void validateAppToken(String appToken) {
        if (appToken == null || appToken.isBlank()) {
            throw new IllegalArgumentException("应用令牌不能为空");
        }
        if (appService.selectAppByToken(appToken) == null) {
            throw new IllegalArgumentException("应用不存在");
        }
    }

    /**
     * 校验 tableId 是否属于给定应用的真实数据表
     */
    private void validateTableId(String appToken, String tableId) {
        if (tableId == null || tableId.isBlank()) {
            throw new IllegalArgumentException("数据表ID不能为空");
        }
        List<BitableTable> tables = tableService.selectTableList(appToken);
        boolean found = tables.stream().anyMatch(t -> tableId.equals(t.getTableId()));
        if (!found) {
            throw new IllegalArgumentException("数据表不存在或不属于该应用");
        }
    }

    @PreAuthorize("@ss.hasPermi('bitable:app:list')")
    @GetMapping("/table/list/{appToken}")
    public AjaxResult tableList(@PathVariable String appToken)
    {
        validateAppToken(appToken);
        List<BitableTable> list = tableService.selectTableList(appToken);
        return AjaxResult.success(list);
    }

    @PreAuthorize("@ss.hasPermi('bitable:app:add')")
    @Log(title = "多维表格-数据表", businessType = BusinessType.INSERT)
    @PostMapping("/table")
    public AjaxResult addTable(@RequestBody BitableTable table)
    {
        return toAjax(tableService.insertTable(table));
    }

    @PreAuthorize("@ss.hasPermi('bitable:app:edit')")
    @Log(title = "多维表格-数据表", businessType = BusinessType.UPDATE)
    @PutMapping("/table")
    public AjaxResult editTable(@RequestBody BitableTable table)
    {
        return toAjax(tableService.updateTable(table));
    }

    @PreAuthorize("@ss.hasPermi('bitable:app:remove')")
    @Log(title = "多维表格-数据表", businessType = BusinessType.DELETE)
    @DeleteMapping("/table/{id}")
    public AjaxResult removeTable(@PathVariable Long id)
    {
        return toAjax(tableService.deleteTableById(id));
    }

    // ==================== 字段管理 ====================

    @PreAuthorize("@ss.hasPermi('bitable:app:list')")
    @GetMapping("/field/list/{appToken}/{tableId}")
    public AjaxResult fieldList(@PathVariable String appToken, @PathVariable String tableId)
    {
        validateAppToken(appToken);
        validateTableId(appToken, tableId);
        List<BitableField> list = fieldService.selectFieldList(appToken, tableId);
        return AjaxResult.success(list);
    }

    @PreAuthorize("@ss.hasPermi('bitable:app:edit')")
    @Log(title = "多维表格-字段", businessType = BusinessType.UPDATE)
    @PutMapping("/field")
    public AjaxResult editField(@RequestBody BitableField field)
    {
        return toAjax(fieldService.updateField(field));
    }

    @PreAuthorize("@ss.hasPermi('bitable:app:remove')")
    @Log(title = "多维表格-字段", businessType = BusinessType.DELETE)
    @DeleteMapping("/field/{id}")
    public AjaxResult removeField(@PathVariable Long id)
    {
        return toAjax(fieldService.deleteFieldById(id));
    }

    // ==================== 记录管理 ====================

    @PreAuthorize("@ss.hasPermi('bitable:app:list')")
    @GetMapping("/record/list/{appToken}/{tableId}")
    public TableDataInfo recordList(
            @PathVariable String appToken,
            @PathVariable String tableId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String filterField,
            @RequestParam(required = false) String filterValue,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) String filters)
    {
        validateAppToken(appToken);
        validateTableId(appToken, tableId);
        startPage();
        BitableRecord query = new BitableRecord();
        query.setAppToken(appToken);
        query.setTableId(tableId);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setFilterField(filterField);
        query.setFilterValue(filterValue);
        query.setSortField(sortField);
        query.setSortOrder(sortOrder);
        // 多条件筛选：前端传JSON数组字符串，后端解析
        if (filters != null && !filters.isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                List<Map<String, String>> filterList = mapper.readValue(filters,
                    mapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                query.setFilters(filterList);
            } catch (Exception e) {
                // 解析失败，忽略
            }
        }
        List<BitableRecord> list = recordService.selectRecordList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('bitable:app:list')")
    @GetMapping("/record/count/{appToken}/{tableId}")
    public AjaxResult recordCount(@PathVariable String appToken, @PathVariable String tableId)
    {
        validateAppToken(appToken);
        validateTableId(appToken, tableId);
        return AjaxResult.success(recordService.countRecords(appToken, tableId));
    }

    @PreAuthorize("@ss.hasPermi('bitable:app:remove')")
    @Log(title = "多维表格-记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/record/{id}")
    public AjaxResult removeRecord(@PathVariable Long id)
    {
        return toAjax(recordService.deleteRecordById(id));
    }

    @PreAuthorize("@ss.hasPermi('bitable:app:remove')")
    @Log(title = "多维表格-记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/record/clear/{appToken}/{tableId}")
    public AjaxResult clearRecords(@PathVariable String appToken, @PathVariable String tableId)
    {
        validateAppToken(appToken);
        validateTableId(appToken, tableId);
        return toAjax(recordService.deleteRecordsByTable(appToken, tableId));
    }

    // ==================== 统计信息 ====================

    /**
     * 获取应用的完整概览（含数据表数量、记录数量等统计）
     */
    @PreAuthorize("@ss.hasPermi('bitable:app:list')")
    @GetMapping("/overview/{appToken}")
    public AjaxResult overview(@PathVariable String appToken)
    {
        BitableApp app = appService.selectAppByToken(appToken);
        if (app == null)
        {
            return AjaxResult.error("应用不存在");
        }
        List<BitableTable> tables = tableService.selectTableList(appToken);
        Map<String, Object> overview = new HashMap<>();
        overview.put("app", app);
        overview.put("tables", tables);
        // 每个表的记录数
        Map<String, Integer> recordCounts = new HashMap<>();
        for (BitableTable t : tables)
        {
            int count = recordService.countRecords(appToken, t.getTableId());
            recordCounts.put(t.getTableId(), count);
        }
        overview.put("recordCounts", recordCounts);
        return AjaxResult.success(overview);
    }

    // ==================== 数据导出 ====================

    /**
     * 导出数据表记录为 XLSX
     */
    @PreAuthorize("@ss.hasPermi('bitable:app:list')")
    @GetMapping("/record/export/{appToken}/{tableId}")
    public void exportRecords(
            @PathVariable String appToken,
            @PathVariable String tableId,
            HttpServletResponse response) throws Exception
    {
        validateAppToken(appToken);
        validateTableId(appToken, tableId);
        // 查询所有记录（限制 10000 条，防止内存溢出）
        List<BitableRecord> records = recordService.selectAllRecords(appToken, tableId);
        // 查询字段定义
        List<BitableField> fields = fieldService.selectFieldList(appToken, tableId);
        // 查询表名
        List<BitableTable> tables = tableService.selectTableList(appToken);
        String tableName = tableId;
        for (BitableTable t : tables)
        {
            if (tableId.equals(t.getTableId())) { tableName = t.getName(); break; }
        }
        // 生成 XLSX
        try (Workbook workbook = new XSSFWorkbook())
        {
            Sheet sheet = workbook.createSheet(tableName);
            // 表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            // 写表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < fields.size(); i++)
            {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(fields.get(i).getFieldName());
                cell.setCellStyle(headerStyle);
            }
            Cell timeCell = headerRow.createCell(fields.size());
            timeCell.setCellValue("上报时间");
            timeCell.setCellStyle(headerStyle);
            // 数据行
            com.fasterxml.jackson.databind.ObjectMapper jsonMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (int rowIdx = 0; rowIdx < records.size(); rowIdx++)
            {
                BitableRecord r = records.get(rowIdx);
                Row row = sheet.createRow(rowIdx + 1);
                Map<String, Object> fieldsMap = new HashMap<>();
                if (r.getFieldsJson() != null && !r.getFieldsJson().isEmpty())
                {
                    try { fieldsMap = jsonMapper.readValue(r.getFieldsJson(), Map.class); } catch (Exception e) {}
                }
                for (int i = 0; i < fields.size(); i++)
                {
                    Cell cell = row.createCell(i);
                    String fid = fields.get(i).getFieldId();
                    Object val = getNestedValue(fieldsMap, fid);
                    if (val != null)
                    {
                        String strVal = val instanceof Map || val instanceof List ? jsonMapper.writeValueAsString(val) : String.valueOf(val);
                        cell.setCellValue(strVal);
                    }
                }
                // 上报时间
                Cell cTime = row.createCell(fields.size());
                if (r.getCreatedTime() != null)
                {
                    cTime.setCellValue(sdf.format(r.getCreatedTime()));
                }
            }
            // 自动列宽
            for (int i = 0; i <= fields.size(); i++)
            {
                sheet.autoSizeColumn(i);
            }
            // 写入响应
            String fileName = URLEncoder.encode(tableName + "_导出.xlsx", "UTF-8").replace("+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            OutputStream os = response.getOutputStream();
            workbook.write(os);
            os.flush();
            os.close();
        }
    }

    // ==================== 批量删除 ====================

    /**
     * 批量删除记录
     */
    @PreAuthorize("@ss.hasPermi('bitable:app:remove')")
    @Log(title = "多维表格-记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/record/batch")
    public AjaxResult batchRemoveRecord(@RequestBody List<Long> ids)
    {
        return toAjax(recordService.deleteRecordByIds(ids));
    }
    
    // 嵌套值解析（支持 "user.uid" 这种点号路径）
    private Object getNestedValue(Map<String, Object> map, String path) {
        if (map == null || path == null) return null;
        String[] parts = path.split("\\.", 2);
        Object val = map.get(parts[0]);
        if (parts.length == 1) return val;
        if (val instanceof Map) {
            return getNestedValue((Map<String, Object>) val, parts[1]);
        }
        return null;
    }
}
