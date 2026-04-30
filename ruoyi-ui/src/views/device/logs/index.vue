<template>
  <div class="logs-page">
    <!-- ========== 页面头部 ========== -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">日志记录</h1>
        <p class="page-date">查看系统操作历史与异常提醒</p>
      </div>
      <div class="header-right">
        <el-button size="small" class="refresh-btn" @click="loadLogs" :loading="loading">
          <i class="el-icon-refresh"></i> 刷新
        </el-button>
        <el-button size="small" type="warning" class="clear-btn" @click="clearInfo" :loading="clearing === 'info'">
          <i class="el-icon-delete"></i> 清空INFO日志
        </el-button>
        <el-button size="small" type="danger" class="clear-btn" @click="clearAll" :loading="clearing === 'all'">
          <i class="el-icon-delete"></i> 清空全部
        </el-button>
      </div>
    </div>

    <!-- ========== 统计卡片 ========== -->
    <div class="stats-cards">
      <div class="stat-card stat-all" @click="filterByLevel('')">
        <div class="stat-value">{{ total }}</div>
        <div class="stat-label">全部</div>
      </div>
      <div class="stat-card stat-warn" @click="filterByLevel('warning')">
        <div class="stat-value">{{ warnCount }}</div>
        <div class="stat-label">当页警告</div>
      </div>
      <div class="stat-card stat-err" @click="filterByLevel('error')">
        <div class="stat-value">{{ errCount }}</div>
        <div class="stat-label">当页错误</div>
      </div>
    </div>

    <!-- ========== 筛选栏 ========== -->
    <div class="filter-card">
      <div class="filter-header">
        <i class="el-icon-search"></i>
        <span>筛选条件</span>
      </div>
      <el-row type="flex" align="middle" :gutter="12">
        <el-col :span="4">
          <div class="filter-item">
            <label class="filter-label">级别</label>
            <el-select
              v-model="filter.level"
              @change="handleFilterChange"
              size="small"
              class="filter-select"
            >
              <el-option label="全部" value=""></el-option>
              <el-option label="INFO" value="info"></el-option>
              <el-option label="WARNING" value="warning"></el-option>
              <el-option label="ERROR" value="error"></el-option>
            </el-select>
          </div>
        </el-col>
        <el-col :span="5">
          <div class="filter-item">
            <label class="filter-label">模块</label>
            <el-select
              v-model="filter.module"
              placeholder="全部模块"
              clearable
              @change="handleFilterChange"
              size="small"
              class="filter-select"
            >
              <el-option
                v-for="m in modules"
                :key="m"
                :label="m"
                :value="m"
              />
            </el-select>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="filter-item">
            <label class="filter-label">关键词</label>
            <el-input
              v-model="inputKeyword"
              placeholder="输入关键词搜索"
              size="small"
              clearable
              @keyup.enter.native="handleSearch"
              class="filter-input"
            >
              <el-button slot="append" icon="el-icon-search" @click="handleSearch"></el-button>
            </el-input>
          </div>
        </el-col>
        <el-col :span="3">
          <el-button size="small" @click="resetFilter" class="reset-btn">重置</el-button>
        </el-col>
        <el-col :span="4" style="text-align: right;">
          <span class="total-hint">共 {{ total }} 条</span>
        </el-col>
      </el-row>
    </div>

    <!-- ========== 日志列表 ========== -->
    <div class="table-card">
      <el-table
        :data="logs"
        size="mini"
        stripe
        empty-text="暂无日志"
        class="log-table"
      >
        <el-table-column label="级别" width="80" align="center">
          <template v-slot="{ row }">
            <el-tag
              :type="levelType(row.level)"
              size="mini"
              class="level-tag"
            >
              {{ row.level.toUpperCase() }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="160">
          <template v-slot="{ row }">{{ formatDateTime(row.logTime) }}</template>
        </el-table-column>
        <el-table-column label="模块" width="120">
          <template v-slot="{ row }">
            <el-tag type="info" size="mini" class="module-tag">{{ row.module }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="消息" show-overflow-tooltip>
          <template v-slot="{ row }">
            <span class="log-message">{{ row.message }}</span>
            <el-tag v-if="row.message && row.message.includes('[强制保存]')" type="danger" size="mini" class="force-tag">强制</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="详情" width="70" align="center">
          <template v-slot="{ row }">
            <el-button v-if="row.detail" type="text" size="mini" @click="showDetail(row)" class="detail-btn">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          @current-change="handlePageChange"
          :current-page="page"
          :page-size="pageSize"
          layout="total, prev, pager, next"
          :total="total"
        />
      </div>
    </div>

    <!-- ========== 详情弹窗 ========== -->
    <el-dialog title="日志详情" :visible.sync="detailVisible" width="90%" class="detail-dialog">
      <pre class="detail-content">{{ detailContent }}</pre>
    </el-dialog>
  </div>
</template>

<script>
import { listLogs, getLogModules, clearLogs } from '@/api/device/index'

export default {
  name: 'DeviceLogs',
  data() {
    return {
      logs: [],
      modules: [],
      total: 0,
      page: 1,
      pageSize: 100,
      loading: false,
      clearing: '',
      filter: { level: '', module: '', keyword: '' },
      inputKeyword: '',
      detailVisible: false,
      detailContent: ''
    }
  },
  computed: {
    warnCount() {
      return this.logs.filter(l => l.level === 'warning').length
    },
    errCount() {
      return this.logs.filter(l => l.level === 'error').length
    }
  },
  created() {
    this.init()
  },
  methods: {
    async init() {
      const [logsRes, modsRes] = await Promise.all([this.fetchLogs(), getLogModules()])
      this.logs = logsRes.rows || []
      this.total = logsRes.total || 0
      this.modules = modsRes.data || []
    },
    async fetchLogs() {
      this.loading = true
      try {
        const params = {
          pageNum: this.page,
          pageSize: this.pageSize,
          ...this.filter
        }
        const res = await listLogs(params)
        this.logs = res.rows || []
        this.total = res.total || 0
        // 提取模块列表
        const newModules = this.logs.map(l => l.module).filter(Boolean)
        this.modules = Array.from(new Set([...this.modules, ...newModules]))
        return res
      } finally {
        this.loading = false
      }
    },
    handleFilterChange() {
      this.page = 1
      this.fetchLogs()
    },
    handleSearch() {
      this.filter.keyword = this.inputKeyword
      this.page = 1
      this.fetchLogs()
    },
    resetFilter() {
      this.filter = { level: '', module: '', keyword: '' }
      this.inputKeyword = ''
      this.page = 1
      this.fetchLogs()
    },
    filterByLevel(level) {
      this.filter.level = level
      this.page = 1
      this.fetchLogs()
    },
    async clearInfo() {
      try {
        await this.$confirm('确定清空所有 INFO 级别日志吗？WARNING/ERROR 级别日志将保留。', '提示', { type: 'warning' })
        this.clearing = 'info'
        await clearLogs('info')
        this.$message.success('INFO 日志已清空')
        await this.fetchLogs()
      } catch(e) {}
      finally {
        this.clearing = ''
      }
    },
    async clearAll() {
      try {
        await this.$confirm('⚠️ 确定要清空全部日志吗？\n\n将删除：\n- 全部日志\n\n该操作不可恢复。', '危险操作', {
          type: 'error',
          confirmButtonText: '确认清空'
        })
        this.clearing = 'all'
        await clearLogs('all')
        this.$message.success('全部日志已清空')
        await this.fetchLogs()
      } catch(e) {}
      finally {
        this.clearing = ''
      }
    },
    showDetail(row) {
      this.detailContent = row.detail || ''
      this.detailVisible = true
    },
    levelType(level) {
      return { info: 'info', warning: 'warning', error: 'danger' }[level] || 'info'
    },
    formatDateTime(val) {
      if (!val) return '-'
      return String(val).substring(0, 19)
    },
    handlePageChange(p) {
      this.page = p
      this.fetchLogs()
    }
  }
}
</script>

<style scoped>
.logs-page {
  padding: 24px;
  background: #f8fafc;
  min-height: calc(100vh - 84px);
}

/* ========== 页面头部 ========== */
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}
.header-left {
  flex: 1;
}
.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 4px 0;
}
.page-date {
  font-size: 13px;
  color: #94a3b8;
  margin: 0;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.refresh-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}
.clear-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* ========== 统计卡片 ========== */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
.stat-card {
  border-radius: 16px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.stat-all {
  background: #fff;
  border-color: #e2e8f0;
}
.stat-warn {
  background: #fffbeb;
  border-color: #fde68a;
}
.stat-err {
  background: #fef2f2;
  border-color: #fecaca;
}
.stat-value {
  font-size: 28px;
  font-weight: 800;
  line-height: 1.1;
  margin-bottom: 4px;
}
.stat-all .stat-value { color: #334155; }
.stat-warn .stat-value { color: #d97706; }
.stat-err .stat-value { color: #dc2626; }
.stat-label {
  font-size: 12px;
  font-weight: 500;
}
.stat-all .stat-label { color: #94a3b8; }
.stat-warn .stat-label { color: #b45309; }
.stat-err .stat-label { color: #dc2626; }

/* ========== 筛选栏 ========== */
.filter-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  padding: 20px;
  margin-bottom: 20px;
}
.filter-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
  margin-bottom: 12px;
}
.filter-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.filter-label {
  font-size: 11px;
  color: #94a3b8;
  font-weight: 500;
}
.filter-select {
  width: 100%;
}
.filter-input {
  width: 100%;
}
.reset-btn {
  margin-top: 20px;
}
.total-hint {
  font-size: 13px;
  color: #64748b;
}

/* ========== 日志表格 ========== */
.table-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
}
.log-table {
  width: 100%;
}
.level-tag {
  font-weight: 600;
  min-width: 56px;
  text-align: center;
}
.module-tag {
  font-size: 10px;
}
.log-message {
  font-size: 13px;
  color: #475569;
}
.force-tag {
  margin-left: 4px;
  font-size: 10px;
}
.detail-btn {
  font-size: 11px;
}
.pagination-wrapper {
  padding: 12px 20px;
  border-top: 1px solid #f1f5f9;
  text-align: right;
}

/* ========== 详情弹窗 ========== */
.detail-dialog >>> .el-dialog {
  max-width: 600px;
}
.detail-dialog >>> .el-dialog__body {
  padding: 20px;
}
.detail-content {
  white-space: pre-wrap;
  font-size: 12px;
  max-height: 400px;
  overflow: auto;
  background: #f8fafc;
  padding: 12px;
  border-radius: 8px;
  margin: 0;
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
  .detail-dialog >>> .el-dialog {
    width: 95% !important;
    max-width: none !important;
    margin-top: 3vh !important;
  }
  .detail-dialog >>> .el-dialog__body {
    padding: 12px !important;
  }
}
</style>
