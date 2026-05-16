<template>
  <div class="app-container">
    <!-- ========== 头部操作栏 ========== -->
    <div class="header-bar">
      <div class="header-left">
        <h2 class="page-title">多维表格</h2>
        <span class="page-desc">管理数据应用，接收社媒助手数据上报</span>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索应用名称"
          size="small"
          prefix-icon="el-icon-search"
          clearable
          style="width: 220px; margin-right: 12px"
        />
        <el-button type="primary" size="small" icon="el-icon-plus" @click="handleAdd">新建应用</el-button>
      </div>
    </div>

    <!-- ========== 统计概览 ========== -->
    <div class="stats-bar">
      <div class="stat-item">
        <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
          <i class="el-icon-s-grid" />
        </div>
        <div class="stat-text">
          <div class="stat-value">{{ filteredApps.length }}</div>
          <div class="stat-label">应用总数</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
          <i class="el-icon-document" />
        </div>
        <div class="stat-text">
          <div class="stat-value">{{ totalTables }}</div>
          <div class="stat-label">数据表总数</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
          <i class="el-icon-coin" />
        </div>
        <div class="stat-text">
          <div class="stat-value">{{ totalRecords }}</div>
          <div class="stat-label">上报记录</div>
        </div>
      </div>
    </div>

    <!-- ========== 应用卡片列表 ========== -->
    <div v-loading="loading" class="app-grid">
      <el-empty v-if="!loading && appList.length === 0" description="暂无应用，点击右上角新建" />

      <div v-for="app in filteredApps" :key="app.id" class="app-card" @click="openApp(app)">
        <div class="card-header">
          <div class="card-icon" :style="{ background: getAppGradient(app.name) }">
            <i :class="getAppIcon(app.name)" />
          </div>
          <div class="card-info">
            <h3 class="card-name">{{ app.name }}</h3>
            <span class="card-token">{{ app.appToken }}</span>
          </div>
          <el-dropdown trigger="click" @click.native.stop>
            <span class="card-more"><i class="el-icon-more" /></span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="handleEdit(app)">编辑</el-dropdown-item>
              <el-dropdown-item @click.native="showApiKey(app)">查看API Key</el-dropdown-item>
              <el-dropdown-item @click.native="resetApiKey(app)">重置API Key</el-dropdown-item>
              <el-dropdown-item @click.native="copyReportUrl(app)">复制上报地址</el-dropdown-item>
              <el-dropdown-item divided @click.native="handleDelete(app)">删除</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
        <div class="card-desc">{{ app.description || '暂无描述' }}</div>
        <div class="card-footer">
          <span class="card-meta"><i class="el-icon-collection" /> {{ app.tableCount || 0 }} 个数据表</span>
          <span class="card-meta"><i class="el-icon-time" /> {{ parseTime(app.createTime, '{y}-{m}-{d}') }}</span>
        </div>
      </div>
    </div>

    <!-- ========== 新建/编辑弹窗 ========== -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="520px" :close-on-click-modal="false">
      <el-form ref="appForm" :model="appForm" :rules="appRules" label-width="90px">
        <el-form-item label="应用名称" prop="name">
          <el-input v-model="appForm.name" placeholder="如：社媒数据" maxlength="50" />
        </el-form-item>
        <el-form-item label="应用描述" prop="description">
          <el-input v-model="appForm.description" type="textarea" :rows="3" placeholder="简要描述此应用的用途" maxlength="200" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" size="small" :loading="submitLoading" @click="submitApp">确定</el-button>
      </div>
    </el-dialog>

    <!-- ========== API Key 展示弹窗 ========== -->
        <!-- ========== API Key 展示弹窗 ========== -->
    <el-dialog title="API Key" :visible.sync="apiKeyVisible" width="600px">
      <div class="apikey-info">
        <p class="apikey-label">应用 Token</p>
        <div class="apikey-row">
          <code>{{ currentApp.appToken }}</code>
          <el-button type="text" size="mini" @click="copyText(currentApp.appToken)">复制</el-button>
        </div>
        <p class="apikey-label">API Key</p>
        <div class="apikey-row">
          <code>{{ currentApp.apiKey }}</code>
          <el-button type="text" size="mini" @click="copyText(currentApp.apiKey)">复制</el-button>
        </div>
        <div style="margin-top:4px">
          <el-button type="warning" size="mini" plain @click="resetApiKey(currentApp)">重置 API Key</el-button>
          <span style="color:#909399;font-size:12px;margin-left:8px">重置后旧 Key 立即失效</span>
        </div>
        <el-divider />
        <p class="apikey-label">完整请求（直接复制）</p>
        <div class="apikey-row block">
          <pre class="curl-block">{{ getCurlCommand() }}</pre>
        </div>
        <p class="apikey-hint">复制后在 Postman/Apifox/curl 等工具中粘贴即可使用。<code>{table_key}</code> 替换为实际数据表 ID。</p>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listApp, addApp, updateApp, deleteApp, resetApiKey } from '@/api/bitable'

export default {
  name: 'BitableAppList',
  data() {
    return {
      loading: false,
      searchKeyword: '',
      sortOrder: 'default', // default | name | time
      appList: [],
      // 弹窗
      dialogVisible: false,
      dialogTitle: '新建应用',
      submitLoading: false,
      appForm: { name: '', description: '' },
      appRules: {
        name: [{ required: true, message: '请输入应用名称', trigger: 'blur' }]
      },
      isEdit: false,
      // API Key
      apiKeyVisible: false,
      currentApp: {}
    }
  },
  created() {
    this.loadApps()
  },
  computed: {
    filteredApps() {
      let list = this.appList
      if (this.searchKeyword) {
        const kw = this.searchKeyword.toLowerCase()
        list = list.filter(a => (a.name || '').toLowerCase().includes(kw) || (a.appToken || '').toLowerCase().includes(kw))
      }
      if (this.sortOrder === 'name') {
        list = [...list].sort((a, b) => (a.name || '').localeCompare(b.name || ''))
      } else if (this.sortOrder === 'time') {
        list = [...list].sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      }
      return list
    },
    totalTables() {
      return this.filteredApps.reduce((sum, app) => sum + (app.tableCount || 0), 0)
    },
    totalRecords() {
      return this.filteredApps.reduce((sum, app) => sum + (app.recordCount || 0), 0)
    }
  },
  methods: {
    loadApps() {
      this.loading = true
      listApp().then(res => {
        this.appList = res.rows || []
      }).finally(() => { this.loading = false })
    },
    // 根据应用名称关键词返回图标
    getAppIcon(name) {
      const n = (name || '').toLowerCase()
      if (/小红|xhs|redbook/i.test(n)) return 'el-icon-goods'
      if (/抖音|douyin|tiktok/i.test(n)) return 'el-icon-video-camera'
      if (/微博|weibo/i.test(n)) return 'el-icon-chat-dot-square'
      if (/微信|wechat/i.test(n)) return 'el-icon-message'
      if (/b站|bili|bilibili/i.test(n)) return 'el-icon-video-play'
      if (/知乎|zhihu/i.test(n)) return 'el-icon-notebook-2'
      if (/github|git/i.test(n)) return 'el-icon-github'
      if (/数据|data|database/i.test(n)) return 'el-icon-coin'
      if (/监控|monitor|monitor/i.test(n)) return 'el-icon-monitor'
      if (/日志|log/i.test(n)) return 'el-icon-document-copy'
      return 'el-icon-s-grid'
    },
    // 根据应用名称返回渐变色
    getAppGradient(name) {
      const gradients = [
        'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
        'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
        'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
        'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
        'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
        'linear-gradient(135deg, #6a11cb 0%, #2575fc 100%)',
        'linear-gradient(135deg, #f7971e 0%, #ffd200 100%)',
        'linear-gradient(135deg, #00c6ff 0%, #0072ff 100%)'
      ]
      const idx = (name || '').split('').reduce((acc, ch) => acc + ch.charCodeAt(0), 0) % gradients.length
      return gradients[idx]
    },
    openApp(app) {
      this.$router.push({ path: '/bitable/detail', query: { appToken: app.appToken } })
    },
    handleAdd() {
      this.isEdit = false
      this.dialogTitle = '新建应用'
      this.appForm = { name: '', description: '' }
      this.dialogVisible = true
    },
    handleEdit(app) {
      this.isEdit = true
      this.dialogTitle = '编辑应用'
      this.appForm = { id: app.id, name: app.name, description: app.description }
      this.dialogVisible = true
    },
    submitApp() {
      this.$refs.appForm.validate(valid => {
        if (!valid) return
        this.submitLoading = true
        const fn = this.isEdit ? updateApp : addApp
        fn(this.appForm).then(() => {
          this.$message.success(this.isEdit ? '修改成功' : '创建成功')
          this.dialogVisible = false
          this.loadApps()
        }).finally(() => { this.submitLoading = false })
      })
    },
    handleDelete(app) {
      this.$confirm(`确认删除应用「${app.name}」？该应用下所有数据表和记录将一并删除！`, '警告', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteApp(app.id).then(() => {
          this.$message.success('删除成功')
          // 如果删除的是当前正在查看的应用，自动返回列表
          if (app.appToken === this.$route.query.appToken) {
            this.$router.push('/bitable')
          }
          this.loadApps()
        })
      }).catch(() => {})
    },
    showApiKey(app) {
      this.currentApp = app
      this.apiKeyVisible = true
    },
    resetApiKey(app) {
      this.$confirm('重置后旧 API Key 将立即失效，确定重置？', '重置 API Key', {
        confirmButtonText: '确定重置',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        resetApiKey(app.id).then(res => {
          this.$message.success('API Key 已重置')
          this.currentApp = res.data || app
          this.apiKeyVisible = true
          this.loadApps()
        }).catch(() => {
          this.$message.error('重置失败')
        })
      }).catch(() => {})
    },
    getCurlCommand() {
      if (!this.currentApp.appToken || !this.currentApp.apiKey) return ''
      const apiUrl = window.location.origin.replace(/:\d+$/, '') + ':8080'
      return `curl -X POST ${apiUrl}/api/bitable/reporting/${this.currentApp.appToken}/{table_key} \\\n  -H "Authorization: Bearer ${this.currentApp.apiKey}" \\\n  -H "Content-Type: application/json" \\\n  -d '{\"meta\":{\"fields\":[{\"key\":\"title\",\"name\":\"标题\",\"ui_type\":\"text\"}]},\"list\":[{\"title\":\"测试数据\"}]}'`
    },
    copyReportUrl(app) {
      const apiUrl = window.location.origin.replace(/:\d+$/, '') + ':8080'
      const url = `${apiUrl}/api/bitable/reporting/${app.appToken}/{table_key}`
      this.copyText(url)
    },
    copyText(text) {
      navigator.clipboard.writeText(text).then(() => {
        this.$message.success('已复制到剪贴板')
      }).catch(() => {
        // fallback
        const ta = document.createElement('textarea')
        ta.value = text
        document.body.appendChild(ta)
        ta.select()
        document.execCommand('copy')
        document.body.removeChild(ta)
        this.$message.success('已复制到剪贴板')
      })
    }
  }
}
</script>

<style scoped>
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}
.page-desc {
  font-size: 13px;
  color: #909399;
}
/* ========== 统计概览条 ========== */
.stats-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 16px 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}
.stat-item + .stat-item {
  padding-left: 24px;
  border-left: 1px solid #ebeef5;
}
.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  flex-shrink: 0;
}
.stat-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}
.stat-label {
  font-size: 12px;
  color: #909399;
}
/* ========== 应用卡片列表 ========== */
.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}
.app-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}
.app-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, rgba(64,158,255,0.3), transparent);
  opacity: 0;
  transition: opacity 0.25s;
}
.app-card:hover {
  border-color: #c0d8fa;
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.18);
  transform: translateY(-2px);
}
.app-card:hover::before {
  opacity: 1;
}
.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.card-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}
.card-info {
  flex: 1;
  min-width: 0;
}
.card-name {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.card-token {
  font-size: 11px;
  color: #909399;
  font-family: monospace;
  letter-spacing: 0.5px;
}
.card-more {
  cursor: pointer;
  padding: 4px 8px;
  color: #909399;
  font-size: 16px;
  border-radius: 4px;
  transition: all 0.2s;
}
.card-more:hover {
  color: #409eff;
  background: #f0f7ff;
}
.card-desc {
  margin: 10px 0;
  font-size: 13px;
  color: #606266;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 38px;
  line-height: 1.6;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  padding-top: 10px;
  border-top: 1px solid #f0f2f5;
}



.card-meta {
  display: flex;
  align-items: center;
  gap: 4px;
}
.apikey-info {
  padding: 8px 0;
}
.apikey-label {
  font-size: 13px;
  color: #606266;
  margin: 12px 0 4px;
  font-weight: 500;
}
.apikey-label:first-child {
  margin-top: 0;
}
.apikey-row {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  padding: 8px 12px;
}
.apikey-row code {
  flex: 1;
  font-size: 13px;
  word-break: break-all;
  color: #303133;
}
.apikey-row pre.curl-block {
  margin: 0;
  font-size: 12px;
  line-height: 1.5;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: 'Courier New', Courier, monospace;
  background: transparent;
  padding: 0;
}
.apikey-hint {
  font-size: 12px;
  color: #909399;
  margin: 6px 0;
  line-height: 1.6;
}
.apikey-hint code {
  font-size: 12px;
  color: #409eff;
  background: #ecf5ff;
  padding: 1px 4px;
  border-radius: 3px;
}
.header-right {
  display: flex;
  align-items: center;
}
</style>
