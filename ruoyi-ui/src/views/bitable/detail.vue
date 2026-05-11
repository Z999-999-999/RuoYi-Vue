<template>
  <div class="app-container">
    <!-- ========== 顶部导航 ========== -->
    <div class="detail-header">
      <el-button type="text" icon="el-icon-back" @click="goBack">返回应用列表</el-button>
      <div class="header-center">
        <h2 class="app-name">{{ appInfo.name || '加载中...' }}</h2>
        <span class="app-token">{{ appToken }}</span>
      </div>
      <el-button size="small" icon="el-icon-plus" @click="handleAddTable">新建数据表</el-button>
    </div>

    <!-- ========== 标签页：数据表切换 ========== -->
    <div class="detail-body">
      <!-- 骨架屏 -->
      <template v-if="skeletonLoading">
        <div class="skeleton-tabs">
          <div class="skeleton-tab active" />
          <div class="skeleton-tab" />
          <div class="skeleton-tab" />
        </div>
        <div class="skeleton-toolbar">
          <div class="skeleton-line w-20" />
          <div class="skeleton-line w-40" />
          <div class="skeleton-line w-16" />
        </div>
        <div class="skeleton-table">
          <div v-for="i in 5" :key="i" class="skeleton-row">
            <div class="skeleton-cell" />
            <div class="skeleton-cell w-30" />
            <div class="skeleton-cell w-50" />
            <div class="skeleton-cell w-20" />
          </div>
        </div>
      </template>

      <el-empty v-if="!skeletonLoading && tables.length === 0" description="暂无数据表">
        <template slot="image">
          <svg width="120" height="120" viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="20" y="30" width="80" height="60" rx="8" fill="#f0f2f5" stroke="#dcdfe6" stroke-width="2"/>
            <line x1="20" y1="48" x2="100" y2="48" stroke="#dcdfe6" stroke-width="1.5"/>
            <line x1="20" y1="66" x2="100" y2="66" stroke="#dcdfe6" stroke-width="1.5"/>
            <line x1="40" y1="30" x2="40" y2="90" stroke="#dcdfe6" stroke-width="1.5"/>
            <circle cx="60" cy="105" r="8" fill="#e6f7ff" stroke="#91d5ff" stroke-width="1.5"/>
            <line x1="57" y1="105" x2="63" y2="105" stroke="#409EFF" stroke-width="2" stroke-linecap="round"/>
            <line x1="60" y1="102" x2="60" y2="108" stroke="#409EFF" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </template>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="handleAddTable">新建数据表</el-button>
      </el-empty>

      <template v-if="!skeletonLoading && tables.length > 0">
        <!-- 数据表标签栏 -->
        <el-tabs v-model="activeTableId" @tab-click="onTableChange">
          <el-tab-pane
            v-for="t in tables"
            :key="t.tableId"
            :label="t.name"
            :name="t.tableId"
          >
            <span slot="label">
              {{ t.name }}
              <el-badge :value="recordCounts[t.tableId] || 0" class="tab-badge" type="info" />
            </span>
          </el-tab-pane>
        </el-tabs>

        <!-- 当前表工具栏 -->
        <div v-if="currentTable" class="table-toolbar">
          <!-- 统计概览 -->
          <div class="table-stats">
            <span class="stats-label"><i class="el-icon-coin" /> {{ records.length }} 条</span>
            <span v-if="total > records.length" class="stats-label">
              （共 {{ total }} 条{{ activeFilterCount > 0 ? '，已筛选' : '' }}）
            </span>
            <span v-if="lastRefreshTime" class="stats-time">
              <i class="el-icon-time" /> {{ lastRefreshTime }}
            </span>
          </div>
          <div class="toolbar-left">
            <span class="table-id">ID: {{ currentTable.tableId }}</span>
            <el-button type="text" size="mini" @click="editTableName">
              <i class="el-icon-edit" /> 重命名
            </el-button>
            <el-popconfirm title="确认删除此数据表及所有记录？" @confirm="handleDeleteTable">
              <el-button slot="reference" type="text" size="mini" style="color:#F56C6C">
                <i class="el-icon-delete" /> 删除
              </el-button>
            </el-popconfirm>
          </div>
          <div class="toolbar-right">
            <!-- 搜索框 -->
            <el-input
              v-model="searchKeyword"
              placeholder="关键词搜索"
              size="mini"
              clearable
              style="width: 160px; margin-right: 8px"
              @keyup.enter.native="handleSearch"
              @clear="handleSearch"
            />
            <el-button size="mini" icon="el-icon-search" @click="handleSearch">搜索</el-button>
            <!-- 高级筛选（多条件） -->
            <el-popover placement="bottom" width="540" trigger="click">
              <div class="filter-panel">
                <div class="filter-title">
                  筛选条件
                  <el-button type="text" size="mini" style="float:right" @click="addFilterRow">
                    <i class="el-icon-plus" /> 添加条件
                  </el-button>
                </div>
                <div v-if="filterRows.length === 0" style="color:#909399;font-size:12px;text-align:center;padding:8px 0">
                  点击「添加条件」新建筛选
                </div>
                <div v-for="(row, index) in filterRows" :key="index" class="filter-row">
                  <el-select v-model="row.field" placeholder="字段" clearable style="width:140px" size="mini">
                    <el-option label="发布时间" value="__createdTime__" />
                    <el-option v-for="f in fields" :key="f.fieldId" :label="f.fieldName" :value="f.fieldId" />
                  </el-select>
                  <el-select v-model="row.operator" placeholder="操作" style="width:110px" size="mini">
                    <el-option label="等于" value="equals" />
                    <el-option label="不等于" value="notEquals" />
                    <el-option v-if="!isDateField(row.field)" label="包含" value="contains" />
                    <el-option v-if="!isDateField(row.field)" label="不包含" value="notContains" />
                    <el-option label="为空" value="isEmpty" />
                    <el-option label="不为空" value="isNotEmpty" />
                    <el-option v-if="isDateField(row.field)" label="晚于" value="isGreater" />
                    <el-option v-if="isDateField(row.field)" label="早于" value="isLess" />
                  </el-select>
                  <!-- 日期选择器（日期字段 + 等于/晚于/早于） -->
                  <el-date-picker
                    v-if="row.operator !== 'isEmpty' && row.operator !== 'isNotEmpty' && isDateField(row.field)"
                    v-model="row.dateValue"
                    type="date"
                    placeholder="选择日期"
                    size="mini"
                    style="width:130px"
                    value-format="yyyy-MM-dd"
                    :clearable="true"
                  />
                  <!-- 普通文本输入（非日期字段） -->
                  <el-input
                    v-else-if="row.operator !== 'isEmpty' && row.operator !== 'isNotEmpty'"
                    v-model="row.value"
                    placeholder="值"
                    size="mini"
                    style="width:130px"
                  />
                  <span v-else style="width:130px;display:inline-block" />
                  <el-button
                    type="danger"
                    icon="el-icon-delete"
                    circle
                    size="mini"
                    style="padding:4px;margin-left:4px"
                    @click="removeFilterRow(index)"
                  />
                </div>
                <div class="filter-footer">
                  <el-button size="mini" @click="resetFilters">重置</el-button>
                  <el-button type="primary" size="mini" @click="applyFilters">应用</el-button>
                </div>
              </div>
              <el-button slot="reference" size="mini" icon="el-icon-filter">
                筛选
                <el-badge v-if="activeFilterCount > 0" :value="activeFilterCount" class="filter-badge" />
              </el-button>
            </el-popover>
            <el-divider direction="vertical" />
            <!-- 列显隐控制 -->
            <el-popover placement="bottom" width="260" trigger="click">
              <div class="column-vis-panel">
                <div class="column-vis-title">显示列 <span style="color:#909399;font-size:12px">({{ visibleFieldCount }}/{{ fields.length }})</span></div>
                <el-checkbox
                  v-for="f in fields"
                  :key="f.fieldId"
                  v-model="columnVisible[f.fieldId]"
                  class="column-vis-item"
                >{{ f.fieldName }}</el-checkbox>
              </div>
              <el-button slot="reference" size="mini" icon="el-icon-setting">列设置</el-button>
            </el-popover>
            <el-button size="mini" icon="el-icon-download" @click="handleExport">导出</el-button>
            <el-button size="mini" icon="el-icon-refresh" @click="loadRecords">刷新</el-button>
            <el-button v-if="selectedRows.length > 0" size="mini" type="warning" plain @click="handleBatchDelete">
              批量删除 ({{ selectedRows.length }})
            </el-button>
            <el-popconfirm title="确认清空所有记录？" @confirm="handleClearRecords">
              <el-button slot="reference" size="mini" type="danger" plain>清空记录</el-button>
            </el-popconfirm>
          </div>
        </div>

        <!-- 字段配置行 -->
        <div v-if="fields.length > 0" class="field-bar">
          <span class="field-label">字段：</span>
          <el-tag
            v-for="f in fields"
            :key="f.id"
            size="small"
            :type="fieldTagType(f.type)"
            class="field-tag"
          >
            {{ f.fieldName }}
            <span class="field-key">({{ f.fieldId }})</span>
          </el-tag>
        </div>

        <!-- 无记录空状态 -->
        <div v-if="!skeletonLoading && tables.length > 0 && fields.length > 0 && records.length === 0" class="empty-records">
          <el-empty :description="searchKeyword || filterRows.length > 0 || filterField ? '未找到匹配条件的数据' : '暂无记录'">
            <el-button v-if="searchKeyword || filterRows.length > 0 || filterField" size="small" @click="resetFilters">清除筛选</el-button>
            <el-button v-else size="small" type="primary" icon="el-icon-plus" @click="$router.push('/bitable')">上报数据</el-button>
          </el-empty>
        </div>
        <!-- 数据表格 -->
        <el-table
          :resizable="true"
          v-if="!skeletonLoading && tables.length > 0 && fields.length > 0 && records.length > 0"
          :data="records"
          border
          size="small"
          class="record-table"
          @sort-change="onSortChange"
          @selection-change="onSelectionChange"
        >
          <el-table-column type="selection" width="45" fixed />
          <el-table-column label="#" width="60" fixed>
            <template slot-scope="{ row, $index }">
              {{ $index + 1 }}
            </template>
          </el-table-column>
          <el-table-column
            v-for="f in displayFields"
            :key="f.fieldId"
            :label="f.fieldName"
            :min-width="colWidth(f)"
            show-overflow-tooltip
            :class-name="fieldAlignClass(f)"
          >
            <template slot-scope="{ row }">
              <!-- 多选/单选类型：彩色标签 -->
              <template v-if="f.type === 3 || f.type === 4">
                <el-tag v-for="opt in parseOptions(getFieldValue(row, f.fieldId))" :key="opt" size="mini" :type="optionTagType(opt)" style="margin-right:4px;margin-bottom:2px">{{ opt }}</el-tag>
              </template>
              <!-- 日期类型：带图标 -->  
              <template v-else-if="f.type === 5">
                <span><i class="el-icon-date" style="color:#909399;margin-right:4px" />{{ formatDateField(getFieldValue(row, f.fieldId)) }}</span>
              </template>
              <!-- 复选框类型 -->
              <template v-else-if="f.type === 7">
                <el-tag :type="getFieldValue(row, f.fieldId) === 'true' || getFieldValue(row, f.fieldId) === '1' ? 'success' : 'info'" size="mini">{{ getFieldValue(row, f.fieldId) === 'true' || getFieldValue(row, f.fieldId) === '1' ? '✓' : '✗' }}</el-tag>
              </template>
              <!-- 超链接类型 -->
              <template v-else-if="f.type === 15">
                <a :href="getFieldValue(row, f.fieldId)" target="_blank" style="color:#409EFF;text-decoration:none">{{ getFieldValue(row, f.fieldId) }}</a>
              </template>
              <!-- 数字类型 -->
              <template v-else-if="f.type === 2">
                <span style="font-variant-numeric:tabular-nums;font-weight:500">{{ getFieldValue(row, f.fieldId) }}</span>
              </template>
              <!-- 默认文本（兜底：13位时间戳自动格式化） -->
              <template v-else>
                <span>{{ formatTimestampText(getFieldValue(row, f.fieldId)) }}</span>
              </template>
            </template>
          </el-table-column>
          <el-table-column label="发布时间" width="160" prop="createdTime" sortable="custom">
            <template slot-scope="{ row }">
              <span><i class="el-icon-time" style="color:#909399;margin-right:4px" />{{ formatCreatedTime(row.createdTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template slot-scope="{ row }">
              <el-popconfirm title="确认删除此记录？" @confirm="handleDeleteRecord(row)">
                <el-button slot="reference" type="text" size="mini" style="color:#F56C6C">删除</el-button>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <el-pagination
          v-if="total > 0"
          :current-page.sync="pageNum"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          class="record-pagination"
          @size-change="handleSizeChange"
          @current-change="loadRecords"
        />
      </template>
    </div>

    <!-- ========== 新建数据表弹窗 ========== -->
    <el-dialog title="新建数据表" :visible.sync="tableDialogVisible" width="420px" :close-on-click-modal="false">
      <el-form ref="tableForm" :model="tableForm" :rules="tableRules" label-width="80px">
        <el-form-item label="表名称" prop="name">
          <el-input v-model="tableForm.name" placeholder="如：小红书笔记" maxlength="50" />
        </el-form-item>
        <el-form-item label="表标识" prop="tableKey">
          <el-input v-model="tableForm.tableKey" placeholder="英文标识，如 xhs_notes" maxlength="30" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="tableDialogVisible = false">取消</el-button>
        <el-button type="primary" size="small" :loading="tableSubmitLoading" @click="submitTable">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getOverview, listField, listRecord, addTable, updateTable, deleteTable, deleteRecord, batchDeleteRecord, clearRecords, countRecord, exportRecord } from '@/api/bitable'

export default {
  name: 'BitableDetail',
  data() {
    return {
      skeletonLoading: false,
      appToken: '',
      appInfo: {},
      tables: [],
      recordCounts: {},
      activeTableId: '',
      currentTable: null,
      // 字段
      fields: [],
      // 记录
      records: [],
      total: 0,
      pageNum: 1,
      pageSize: 20,
      // 筛选条件
      searchKeyword: '',
      dateRange: null,
      filterField: '',
      filterValue: '',
      // 多条件筛选
      filterRows: [],
      // 列显隐
      columnVisible: {},  // 改成初始全选
      // 用于 detect router reuse
      _lastAppToken: '',
      // 排序
      sortField: '',
      sortOrder: '',
      // 批量选择
      selectedRows: [],
      // 最后刷新时间
      lastRefreshTime: '',
      // 新建数据表
      tableDialogVisible: false,
      tableSubmitLoading: false,
      tableForm: { name: '', tableKey: '' },
      tableRules: {
        name: [{ required: true, message: '请输入表名称', trigger: 'blur' }],
        tableKey: [{ required: true, message: '请输入表标识', trigger: 'blur' }]
      }
    }
  },
  computed: {
    // 只显示用户勾选的字段（未设置的默认显示）
    displayFields() {
      return this.fields.filter(f => this.columnVisible[f.fieldId] !== false)
    },
    visibleFieldCount() {
      return this.fields.filter(f => this.columnVisible[f.fieldId] !== false).length
    },
    // 激活的筛选条件数量
    activeFilterCount() {
      return this.filterRows.filter(r => r.field && r.operator).length
    }
  },
    watch: {
    '$route.query.appToken': {
      handler(newToken, oldToken) {
        // appToken 变化时重新加载（处理从列表页点击另一个应用的情况）
        if (newToken && newToken !== oldToken && newToken !== this._lastAppToken) {
          this.appToken = newToken
          this._lastAppToken = newToken
          // 重置所有状态
          this.appInfo = {}
          this.tables = []
          this.recordCounts = {}
          this.activeTableId = ''
          this.currentTable = null
          this.fields = []
          this.records = []
          this.total = 0
          this.pageNum = 1
          this.searchKeyword = ''
          this.filterRows = []
          this.selectedRows = []
          this.columnVisible = {}
          this.sortField = ''
          this.sortOrder = ''
          this.loadOverview()
        }
      },
      immediate: false
    },
    '$route.query.tableId': {
      handler() {
        if (this.appToken && this.activeTableId) {
          const saved = localStorage.getItem('bitable_column_' + this.appToken + '_' + this.activeTableId)
          if (saved) {
            this.columnVisible = JSON.parse(saved)
          } else {
            this.columnVisible = {}
          }
        }
      }
    },
    // 监听列显隐状态变化，实时保存到 localStorage
    columnVisible: {
      handler(newVal) {
        if (this.appToken && this.activeTableId && Object.keys(newVal).length > 0) {
          localStorage.setItem(
            'bitable_column_' + this.appToken + '_' + this.activeTableId,
            JSON.stringify(newVal)
          )
        }
      },
      deep: true
    }
  },
  created() {
    this.appToken = this.$route.query.appToken
    if (!this.appToken) {
      this.$message.error('缺少appToken参数')
      this.$router.push('/bitable')
      return
    }
    this._lastAppToken = this.appToken
    this.loadOverview()
  },
  beforeRouteUpdate(to, from, next) {
    // 路由参数变化时（切换应用），重新初始化
    const newToken = to.query.appToken
    if (newToken && newToken !== this._lastAppToken) {
      this.appToken = newToken
      this._lastAppToken = newToken
      // 重置所有状态
      this.appInfo = {}
      this.tables = []
      this.recordCounts = {}
      this.activeTableId = ''
      this.currentTable = null
      this.fields = []
      this.records = []
      this.total = 0
      this.pageNum = 1
      this.searchKeyword = ''
      this.filterRows = []
      this.selectedRows = []
      this.columnVisible = {}
      this.sortField = ''
      this.sortOrder = ''
      this.loadOverview()
    }
    next()
  },
  methods: {

    // 获取字段类型
    getFieldType(fieldId) {
      const field = this.fields.find(f => f.fieldId === fieldId)
      return field ? field.fieldType : null
    },
    loadOverview() {
      this.skeletonLoading = true
      getOverview(this.appToken).then(res => {
        // 应用不存在或已删除，跳转回列表
        if (!res.data || !res.data.app || !res.data.app.id) {
          this.$message.error('应用不存在或已删除')
          this.$router.push('/bitable')
          return
        }
        this.appInfo = res.data.app || {}
        this.tables = res.data.tables || []
        this.recordCounts = res.data.recordCounts || {}
        // 默认选中第一个表
        if (this.tables.length > 0) {
          this.activeTableId = this.tables[0].tableId
          this.currentTable = this.tables[0]
          this.loadFieldsAndRecords()
        }
      }).finally(() => { this.skeletonLoading = false })
    },
    onTableChange(tab) {
      this.currentTable = this.tables.find(t => t.tableId === tab.name)
      this.pageNum = 1
      this.clearFiltersOnTableChange()
      this.loadFieldsAndRecords()
    },
    loadFieldsAndRecords() {
      this.loadFields()
      this.loadRecords()
    },
    loadFields() {
      listField(this.appToken, this.activeTableId).then(res => {
        this.fields = res.data || []
        // 读取 localStorage 中已保存的状态
        const storageKey = 'bitable_column_' + this.appToken + '_' + this.activeTableId
        const saved = localStorage.getItem(storageKey)
        let vis = saved ? JSON.parse(saved) : {}
        // 只对新字段设置默认值 true，保留用户已设置的选择（包括 false）
        this.fields.forEach(f => {
          if (vis[f.fieldId] === undefined) {
            vis[f.fieldId] = true
          }
        })
        // 保存合并后的状态
        localStorage.setItem(storageKey, JSON.stringify(vis))
        this.columnVisible = vis
      })
    },
    loadRecords() {
      const params = {
        pageNum: this.pageNum,
        pageSize: this.pageSize,
        keyword: this.searchKeyword || undefined,
        startDate: this.dateRange ? this.dateRange[0] : undefined,
        endDate: this.dateRange ? this.dateRange[1] : undefined,
        filterField: this.filterField || undefined,
        filterValue: this.filterValue || undefined,
        sortField: this.sortField || undefined,
        sortOrder: this.sortOrder || undefined,
        filters: this.buildFiltersParam()
      }
      listRecord(this.appToken, this.activeTableId, params).then(res => {
        console.log('[loadRecords] res:', res)
        console.log('[loadRecords] rows:', res.rows, 'total:', res.total)
        this.records = res.rows || []
        this.total = res.total || 0
        console.log('[loadRecords] this.records set to', this.records.length, 'items')
        // 更新刷新时间
        const now = new Date()
        this.lastRefreshTime = `${now.getHours().toString().padStart(2,'0')}:${now.getMinutes().toString().padStart(2,'0')}:${now.getSeconds().toString().padStart(2,'0')}`
        this.$forceUpdate()
      })
      // 更新记录数
      countRecord(this.appToken, this.activeTableId).then(res => {
        this.$set(this.recordCounts, this.activeTableId, res.data || 0)
      })
    },
    handleSearch() {
      this.pageNum = 1
      this.loadRecords()
    },
    // 判断字段是否为日期类型（type=5 或 __createdTime__）
    isDateField(fieldId) {
      // 特殊字段：上报时间
      if (fieldId === '__createdTime__') return true
      const field = this.fields.find(f => f.fieldId === fieldId)
      return field && field.type === 5
    },
    // 多条件筛选
    addFilterRow() {
      this.filterRows.push({ field: '', operator: 'equals', value: '', dateValue: '' })
    },
    removeFilterRow(index) {
      this.filterRows.splice(index, 1)
    },
    buildFiltersParam() {
      const valid = this.filterRows.filter(r => r.field && r.operator)
      if (valid.length === 0) return undefined
      const result = valid.map(r => {
        const obj = { field: r.field, operator: r.operator }
        // 调试日志
        console.log('filterRow:', r.field, 'isDateField:', this.isDateField(r.field), 'dateValue:', r.dateValue, 'value:', r.value)
        // 日期字段用dateValue，普通字段用value
        const val = this.isDateField(r.field) ? r.dateValue : r.value
        if (val) obj.value = val
        // 传递字段类型，后端据此决定比较方式
        if (this.isDateField(r.field)) obj.fieldType = '5'
        console.log('buildFiltersParam obj:', JSON.stringify(obj))
        return obj
      })
      return JSON.stringify(result)
    },
    applyFilters() {
      this.pageNum = 1
      this.loadRecords()
    },
    resetFilters() {
      this.searchKeyword = ''
      this.dateRange = null
      this.filterField = ''
      this.filterValue = ''
      this.filterRows = []
      this.handleSearch()
    },
    // 切换数据表时清空筛选
    clearFiltersOnTableChange() {
      this.filterRows = []
      this.searchKeyword = ''
      this.dateRange = null
      this.filterField = ''
      this.filterValue = ''
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.pageNum = 1
      this.loadRecords()
    },
    getFieldValue(row, fieldId) {
      try {
        const fields = typeof row.fieldsJson === 'string' ? JSON.parse(row.fieldsJson) : row.fieldsJson
        // 支持点号路径取值："user.uid" → fields.user.uid
        let val = fields
        for (const key of fieldId.split('.')) {
          if (val == null || typeof val !== 'object') { val = undefined; break }
          val = val[key]
        }
        if (val === null || val === undefined) return '-'
        if (typeof val === 'object') return JSON.stringify(val)
        return String(val)
      } catch (e) {
        return '-'
      }
    },
    // 格式化上报时间（兼容时间戳、Date对象和MySQL datetime字符串）
    formatCreatedTime(val) {
      return this.formatDateField(val)
    },
    // 格式化日期字段（通用方法）
    formatDateField(val) {
      if (!val) return '-'
      let date
      // 如果是时间戳数字（毫秒，13位）
      if (typeof val === 'number' || /^[0-9]{13}$/.test(String(val))) {
        const ts = parseInt(val)
        if (ts > 1000000000000) { date = new Date(ts) }
      } else if (typeof val === 'string' && /^[0-9]{4}-[0-9]{2}-[0-9]{2}/.test(val)) {
        // MySQL datetime字符串
        date = new Date(val.replace(' ', 'T'))
      } else if (val instanceof Date) {
        date = val
      }
      if (!date || isNaN(date.getTime())) return String(val)
      // 手动格式化
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      const h = String(date.getHours()).padStart(2, '0')
      const mi = String(date.getMinutes()).padStart(2, '0')
      const s = String(date.getSeconds()).padStart(2, '0')
      return `${y}-${m}-${d} ${h}:${mi}:${s}`
    },
    // 兜底格式化：默认文本分支中，如果值是13位时间戳，自动格式化
    formatTimestampText(val) {
      if (val == null) return ''
      const str = String(val)
      if (/^[0-9]{13}$/.test(str)) {
        const ts = parseInt(str)
        if (ts > 1000000000000) {
          const d = new Date(ts)
          if (!isNaN(d.getTime())) {
            const y = d.getFullYear()
            const m = String(d.getMonth() + 1).padStart(2, '0')
            const day = String(d.getDate()).padStart(2, '0')
            const h = String(d.getHours()).padStart(2, '0')
            const mi = String(d.getMinutes()).padStart(2, '0')
            const s = String(d.getSeconds()).padStart(2, '0')
            return `${y}-${m}-${day} ${h}:${mi}:${s}`
          }
        }
      }
      return str
    },
    colWidth(field) {
      // 根据字段类型推断列宽
      const typeWidths = { 2: 100, 5: 140, 7: 80, 13: 130, 15: 180 }
      return typeWidths[field.type] || 150
    },
    fieldAlignClass(field) {
      // 数字类型右对齐
      return field.type === 2 ? 'col-align-right' : ''
    },
    // 解析选项值（逗号分隔或数组）
    parseOptions(val) {
      if (!val || val === '-') return []
      if (Array.isArray(val)) return val.map(String)
      return String(val).split(',').map(s => s.trim()).filter(Boolean)
    },
    // 选项标签颜色（根据内容hash）
    optionTagType(opt) {
      const types = ['success', 'warning', 'danger', 'info', '']
      let hash = 0
      for (let i = 0; i < opt.length; i++) { hash = opt.charCodeAt(i) + ((hash << 5) - hash) }
      return types[Math.abs(hash) % types.length]
    },
    fieldTagType(type) {
      const map = { 2: 'success', 3: 'warning', 4: 'warning', 5: 'danger', 7: 'info' }
      return map[type] || ''
    },
    goBack() {
      this.$router.push('/bitable/index')
    },
    // 数据表操作
    handleAddTable() {
      this.tableForm = { name: '', tableKey: '' }
      this.tableDialogVisible = true
    },
    submitTable() {
      this.$refs.tableForm.validate(valid => {
        if (!valid) return
        this.tableSubmitLoading = true
        addTable({
          appId: this.appInfo.id,
          appToken: this.appToken,
          tableId: this.tableForm.tableKey,
          name: this.tableForm.name
        }).then(() => {
          this.$message.success('创建成功')
          this.tableDialogVisible = false
          this.loadOverview()
        }).finally(() => { this.tableSubmitLoading = false })
      })
    },
    editTableName() {
      this.$prompt('请输入新名称', '重命名', {
        inputValue: this.currentTable.name,
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(({ value }) => {
        updateTable({ id: this.currentTable.id, name: value }).then(() => {
          this.$message.success('修改成功')
          this.loadOverview()
        })
      }).catch(() => {})
    },
    handleDeleteTable() {
      deleteTable(this.currentTable.id).then(() => {
        this.$message.success('删除成功')
        this.loadOverview()
      })
    },
    handleClearRecords() {
      clearRecords(this.appToken, this.activeTableId).then(() => {
        this.$message.success('已清空')
        this.loadRecords()
      })
    },
    handleDeleteRecord(row) {
      deleteRecord(row.id).then(() => {
        this.$message.success('已删除')
        this.loadRecords()
      })
    },
    onSortChange({ prop, order }) {
      // prop: 字段名, order: 'ascending' | 'descending' | null
      if (!order) {
        this.sortField = ''
        this.sortOrder = ''
      } else {
        this.sortField = prop
        this.sortOrder = order === 'ascending' ? 'asc' : 'desc'
      }
      this.pageNum = 1
      this.loadRecords()
    },
    // 批量选择
    onSelectionChange(rows) {
      this.selectedRows = rows
    },
    // 批量删除
    handleBatchDelete() {
      if (this.selectedRows.length === 0) return
      this.$confirm(`确认删除选中的 ${this.selectedRows.length} 条记录？`, '批量删除', {
        type: 'warning'
      }).then(() => {
        const ids = this.selectedRows.map(r => r.id)
        batchDeleteRecord(ids).then(() => {
          this.$message.success(`已删除 ${ids.length} 条记录`)
          this.selectedRows = []
          this.loadRecords()
        })
      }).catch(() => {})
    },
    // 数据导出（处理 Blob 响应）
    handleExport() {
      if (!this.activeTableId) return
      this.$message.info('正在导出...')
      exportRecord(this.appToken, this.activeTableId).then(res => {
        // res 在 blob 模式下是 Blob 对象
        const blob = res
        const tableName = this.currentTable ? this.currentTable.name : this.activeTableId
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = tableName + '_导出.xlsx'
        link.click()
        URL.revokeObjectURL(link.href)
        this.$message.success('导出成功')
      }).catch(err => {
        this.$message.error('导出失败：' + (err.message || ''))
      })
    }
  }
}
</script>

<style scoped>
.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
  padding: 12px 20px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.header-center {
  text-align: center;
  flex: 1;
}
.app-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
.app-token {
  font-size: 12px;
  color: #909399;
  font-family: monospace;
}
.detail-body {
  min-height: 400px;
}
.tab-badge {
  margin-left: 4px;
}
/* ========== 表格统计条 ========== */
.table-stats {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}
.stats-label {
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 4px;
}
.stats-label i {
  color: #409EFF;
}
.stats-time {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}
.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 12px 0;
  padding: 10px 14px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  position: sticky;
  top: 64px;
  z-index: 9;
}
.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.table-id {
  font-size: 12px;
  color: #909399;
  font-family: monospace;
}
.field-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  padding: 10px 14px;
  background: linear-gradient(135deg, #f5f7fa 0%, #f0f2f5 100%);
  border-radius: 8px;
  margin-bottom: 12px;
  border: 1px solid #ebeef5;
}
.field-label {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
  font-weight: 600;
}
.field-tag {
  font-size: 12px;
  cursor: default;
  border-radius: 4px;
  padding: 2px 8px;
}
.field-key {
  color: #b0b0b0;
  font-size: 11px;
  margin-left: 2px;
}
.record-table {
  margin-top: 8px;
  border-radius: 8px;
  overflow: hidden;
}
/* 斑马纹 */
::v-deep .record-table .el-table__row:nth-child(even) {
  background-color: #fafbfc;
}
/* 悬停高亮 */
::v-deep .record-table .el-table__row:hover > td {
  background-color: #ecf5ff !important;
}
/* 表头美化 */
::v-deep .record-table .el-table__header th {
  background-color: #f5f7fa !important;
  color: #303133;
  font-weight: 600;
  font-size: 13px;
}
/* 选中行 */
::v-deep .record-table .el-table__row.selected-row {
  background-color: #e6f0ff;
}
/* 分页器美化 */
.record-pagination {
  margin-top: 16px;
  padding: 12px 16px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  text-align: right;
}
::v-deep .record-pagination .el-pagination__total {
  color: #909399;
  font-size: 13px;
  line-height: 32px;
}
.filter-rows {
  max-height: 300px;
  overflow-y: auto;
}
.filter-row {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
}
.filter-footer {
  text-align: right;
  margin-top: 12px;
  border-top: 1px solid #ebeef5;
  padding-top: 10px;
}
.filter-badge {
  margin-left: 2px;
}
.column-vis-panel {
  max-height: 320px;
  overflow-y: auto;
}
.column-vis-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  padding-bottom: 6px;
  border-bottom: 1px solid #ebeef5;
}
.column-vis-item {
  display: block;
  margin-left: 0 !important;
  margin-bottom: 4px;
}
.empty-records {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}
.skeleton-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
/* 骨架屏样式 */
.skeleton-tabs {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}
.skeleton-tab {
  width: 80px;
  height: 32px;
  border-radius: 4px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}
.skeleton-tab.active {
  border-bottom: 2px solid #409EFF;
}
.skeleton-toolbar {
  display: flex;
  gap: 12px;
  margin: 12px 0;
  align-items: center;
}
.skeleton-table {
  margin-top: 12px;
}
.skeleton-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}
.skeleton-cell {
  flex: 1;
  height: 28px;
  border-radius: 4px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}
.skeleton-line {
  height: 28px;
  border-radius: 4px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}
.w-16 { width: 64px; }
.w-20 { width: 80px; }
.w-30 { width: 120px; }
.w-40 { width: 160px; }
.w-50 { width: 200px; }
@keyframes skeleton-pulse {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
/* 数字列右对齐 */
::v-deep .col-align-right .cell {
  text-align: right;
}
</style>
