<template>
  <div class="daily-management-page">
    <el-tabs v-model="activeTab" type="card" class="main-tabs">
      <!-- ===== 每日汇报 Tab ===== -->
      <el-tab-pane label="每日汇报" name="report">
        <div class="page-header">
          <div class="header-left">
            <h2 class="page-title">每日汇报</h2>
            <p class="page-date">{{ reportDate }} · 员工汇报设备使用情况</p>
          </div>
          <div class="header-right">
            <el-date-picker
              v-model="reportDate"
              type="date"
              value-format="yyyy-MM-dd"
              placeholder="选择日期"
              @change="loadReports"
              size="small"
              class="date-picker"
            />
            <el-tag type="info" class="submit-badge">
              {{ submittedCount }}/{{ employees.length }} 已汇报
            </el-tag>
            <el-button size="small" type="warning" @click="openBatchDialog">
              <i class="el-icon-document-copy"></i> 批量粘贴导入
            </el-button>
            <el-button size="small" class="refresh-btn" @click="loadReports" :loading="loading">
              <i class="el-icon-refresh"></i> 刷新
            </el-button>
          </div>
        </div>

        <div v-if="employees.length === 0" class="empty-state">
          <i class="el-icon-user-solid empty-icon"></i>
          <p>请先在"系统设置"中添加员工</p>
        </div>

        <div v-else class="report-list">
          <div
            v-for="emp in sortedEmployees"
            :key="emp.id"
            class="report-card"
            :class="{ 'has-warning': reportMap[emp.id] && reportMap[emp.id].phoneCount !== emp.phoneTotal }"
          >
            <div class="report-card-header" @click="toggleCard(emp.id)">
              <div class="emp-avatar" :style="{ background: avatarColor(emp.name) }">
                {{ (emp.name || '').slice(0, 1) }}
              </div>
              <div class="emp-info">
                <span class="emp-name">{{ emp.name }}</span>
                <span class="emp-dept">{{ emp.department }}</span>
              </div>
              <div class="emp-status-tags">
                <el-tag size="mini" :type="reportMap[emp.id] ? 'success' : 'info'">
                  {{ reportMap[emp.id] ? '已汇报' : '未汇报' }}
                </el-tag>
                <el-tag size="mini" type="warning" v-if="reportMap[emp.id] && reportMap[emp.id].phoneCount !== emp.phoneTotal">
                  数量不符
                </el-tag>
              </div>
              <i :class="expandedCards[emp.id] ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" class="expand-icon"/>
            </div>

            <div v-show="expandedCards[emp.id]" class="report-card-body">
              <el-form :model="formMap[emp.id]" size="small" label-width="80px">
                <el-form-item label="手机数量">
                  <el-input-number
                    v-model="formMap[emp.id].phoneCount"
                    :min="0"
                    :step="1"
                    class="qty-input"
                  />
                  <span v-if="formMap[emp.id].phoneCount !== emp.phoneTotal" class="qty-warn">
                    登记 {{ emp.phoneTotal }} 台
                  </span>
                </el-form-item>

                <!-- 平台账号统计 -->
                <el-form-item label="账号统计" v-if="platforms.length > 0">
                  <el-table :data="formMap[emp.id].platformStats" size="mini" border class="platform-table">
                    <el-table-column label="平台" width="90">
                      <template v-slot="{ row }">
                        <el-tag :style="platformStyle(row.platform)" size="mini">{{ getPlatformLabel(row.platform) }}</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="在用" width="70" align="center">
                      <template v-slot="{ row }">
                        <el-input-number v-model="row.active" :min="0" size="mini" class="small-input" :controls="false"/>
                      </template>
                    </el-table-column>
                    <el-table-column label="发作品" width="70" align="center">
                      <template v-slot="{ row }">
                        <el-input-number v-model="row.posting" :min="0" size="mini" class="small-input" :controls="false"/>
                      </template>
                    </el-table-column>
                    <el-table-column label="不能用" width="70" align="center">
                      <template v-slot="{ row }">
                        <el-input-number v-model="row.disabled" :min="0" size="mini" class="small-input" :controls="false"/>
                      </template>
                    </el-table-column>
                    <el-table-column label="备注">
                      <template v-slot="{ row }">
                        <el-input v-model="row.note" size="mini" placeholder="可选备注"/>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" size="small" @click="saveReport(emp)" :loading="savingMap[emp.id]">
                    <i class="el-icon-check"></i> 保存
                  </el-button>
                  <el-button
                    v-if="reportMap[emp.id]"
                    type="danger"
                    size="small"
                    @click="deleteReport(reportMap[emp.id].id)"
                  >
                    <i class="el-icon-delete"></i> 删除汇报
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- ===== 每日核对 Tab ===== -->
      <el-tab-pane label="每日核对" name="check">
        <div class="page-header">
          <div class="header-left">
            <h2 class="page-title">每日核对</h2>
            <p class="page-date">{{ checkDate }} · 点击员工卡片右上角按钮切换状态</p>
          </div>
          <div class="header-right">
            <el-date-picker
              v-model="checkDate"
              type="date"
              value-format="yyyy-MM-dd"
              placeholder="选择日期"
              @change="loadChecks"
              size="small"
              class="date-picker"
            />
          </div>
        </div>

        <!-- 统计卡片 -->
        <div class="check-stats-cards">
          <div class="stat-mini-card stat-confirmed">
            <i class="el-icon-check"></i>
            <span>已核对 {{ checkStats.confirmed }}</span>
          </div>
          <div class="stat-mini-card stat-pending">
            <i class="el-icon-time"></i>
            <span>待核对 {{ checkStats.pending }}</span>
          </div>
          <div class="stat-mini-card stat-absent">
            <i class="el-icon-close"></i>
            <span>未报到 {{ checkStats.absent }}</span>
          </div>
        </div>

        <!-- 进度条 -->
        <div class="progress-section">
          <div class="progress-header">
            <span>核对进度</span>
            <span class="progress-text">{{ checkStats.confirmed }} / {{ employees.length }} 人 ({{ progressPercent }}%)</span>
          </div>
          <div class="progress-bar">
            <div class="progress-fill-confirmed" :style="{ width: progressPercent + '%' }"></div>
          </div>
        </div>

        <!-- 工具栏 -->
        <div class="toolbar">
          <el-button size="small" type="success" class="toolbar-btn" @click="setAllStatus('confirmed')">
            <i class="el-icon-check"></i> 全部已核对
          </el-button>
          <el-button size="small" type="danger" class="toolbar-btn" @click="setAllStatus('absent')">
            <i class="el-icon-close"></i> 全部未报到
          </el-button>
          <el-button size="small" class="toolbar-btn" @click="setAllStatus('pending')">
            <i class="el-icon-refresh-left"></i> 重置全部
          </el-button>

          <div class="toolbar-divider"></div>

          <div class="filter-group">
            <el-button-group size="small">
              <el-button
                v-for="s in ['all', 'confirmed', 'pending', 'absent']"
                :key="s"
                size="small"
                :type="filterStatus === s ? (s === 'confirmed' ? 'success' : s === 'absent' ? 'danger' : s === 'pending' ? 'info' : 'primary') : 'default'"
                @click="filterStatus = s"
                class="filter-btn"
              >
                {{ s === 'all' ? '全部' : statusLabels[s] }}
              </el-button>
            </el-button-group>
          </div>

          <el-select
            v-model="filterDept"
            placeholder="所有部门"
            size="small"
            clearable
            class="dept-filter"
          >
            <el-option
              v-for="dept in departments"
              :key="dept"
              :label="dept"
              :value="dept"
            />
          </el-select>
        </div>

        <!-- 员工卡片网格 -->
        <div v-if="employees.length === 0" class="empty-state">
          <i class="el-icon-user-solid empty-icon"></i>
          <p>请先在"系统设置"中添加员工</p>
        </div>

        <div v-else class="check-grid">
          <div v-for="group in groupedByDept" :key="group.name" class="dept-group">
            <div class="dept-header">
              <span class="dept-name">{{ group.name }}</span>
              <div class="dept-divider"></div>
              <span class="dept-badge">
                {{ getDeptConfirmed(group) }} / {{ group.employees.length }} 人
              </span>
            </div>

            <div class="check-cards">
              <div
                v-for="emp in group.employees"
                :key="emp.id"
                v-show="shouldShowEmp(emp)"
                class="check-card"
                :class="getCheckClass(emp.id)"
              >
                <div class="check-card-header" :class="getCheckHeaderClass(emp.id)">
                  <div class="check-emp-avatar" :style="{ background: getAvatarBg(emp) }">
                    {{ (emp.name || '').slice(0, 1) }}
                  </div>
                  <div class="check-emp-info">
                    <div class="check-emp-name">{{ emp.name }}</div>
                    <div class="check-emp-dept">{{ emp.department }}</div>
                  </div>
                  <el-button
                    size="mini"
                    :type="getCheckBtnType(emp.id)"
                    @click="cycleCheck(emp)"
                    class="check-status-btn"
                  >
                    {{ checkStatusLabel(emp.id) }}
                  </el-button>
                </div>

                <div class="check-card-body">
                  <div class="phone-info">
                    <i class="el-icon-mobile-phone"></i>
                    <span>手机</span>
                    <span class="phone-qty" :class="{ 'qty-mismatch': getLatestReport(emp.id) && getLatestReport(emp.id).phoneCount !== emp.phoneTotal }">
                      {{ emp.phoneTotal }} 台
                    </span>
                    <span v-if="getLatestReport(emp.id) && getLatestReport(emp.id).phoneCount !== emp.phoneTotal" class="phone-warn">
                      (汇报 {{ getLatestReport(emp.id).phoneCount }} 台)
                    </span>
                  </div>

                  <div v-if="getLatestReport(emp.id)" class="report-preview">
                    <div class="report-date">
                      最近汇报: {{ formatDate(getLatestReport(emp.id).reportDate) }}
                    </div>
                    <div class="platform-preview">
                      <el-tag
                        v-for="ps in parsePlatformStats(getLatestReport(emp.id).platformStats)"
                        :key="ps.platform"
                        size="mini"
                        :style="platformStyle(ps.platform)"
                        class="platform-tag"
                      >
                        {{ getPlatformLabel(ps.platform) }}: {{ ps.active }}用 {{ ps.posting }}发
                      </el-tag>
                    </div>
                  </div>
                  <div v-else class="no-report">
                    暂无汇报数据
                  </div>

                  <div class="note-section">
                    <el-input
                      v-model="checkNoteMap[emp.id]"
                      size="mini"
                      placeholder="添加核对备注..."
                      @blur="saveCheck(emp)"
                      class="note-input"
                    />
                  </div>

                  <div v-if="checkMap[emp.id] && checkMap[emp.id].checkedAt" class="check-time">
                    操作时间: {{ formatDateTime(checkMap[emp.id].checkedAt) }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- ===== 批量粘贴导入弹窗 ===== -->
    <el-dialog title="批量粘贴导入" :visible.sync="showBatchDialog" width="90%" custom-class="batch-import-dialog" :close-on-click-modal="false">
      <!-- 第一步：粘贴 -->
      <div v-if="batchStep === 1">
        <div class="batch-hint">
          <p><b>支持的格式（可包含多人）：</b></p>
          <pre class="batch-format-example">3.28
姓名:桂妍
手机:13
在用抖音:7
不能用抖音：3（禁言30天）
发作品抖音：3
小红书:2
快手：</pre>
          <p style="margin-top:8px;color:#f59e0b;font-size:12px">✨ 解析后先预览确认，手机数不一致时会提示，确认无误再保存</p>
          <p style="color:#64748b;font-size:12px">可以继续增加格式也能自动适配，比如增加"视频号"分类（需先在系统设置添加该平台）</p>
        </div>
        <el-input
          type="textarea"
          v-model="batchRawText"
          :rows="12"
          placeholder="粘贴汇报内容到这里..."
          class="batch-textarea"
        />
        <div class="batch-actions">
          <el-button @click="showBatchDialog = false">取消</el-button>
          <el-button type="primary" @click="parseBatchText" :disabled="!batchRawText.trim()">
            <i class="el-icon-search"></i> 解析预览
          </el-button>
        </div>
      </div>

      <!-- 第二步：预览确认 -->
      <div v-if="batchStep === 2">
        <!-- 摘要提示 -->
        <div class="batch-summary-tags">
          <span class="batch-tag batch-tag-ok"><i class="el-icon-circle-check"></i> 正常 {{ normalItems.length }} 人</span>
          <span v-if="phoneWarningItems.length" class="batch-tag batch-tag-warn"><i class="el-icon-warning"></i> 手机数不一致 {{ phoneWarningItems.length }} 人</span>
          <span v-if="notMatchedItems.length" class="batch-tag batch-tag-err"><i class="el-icon-circle-close"></i> 未匹配 {{ notMatchedItems.length }} 人</span>
        </div>

        <div class="batch-preview-list">
          <!-- 不一致人员（置顶） -->
          <template v-if="phoneWarningItems.length">
            <div class="batch-section-label batch-section-warn">
              <i class="el-icon-warning"></i> 手机数不一致，请先核查进出库记录再决定是否保存
            </div>
            <div v-for="(item, idx) in phoneWarningItems" :key="'w'+idx" class="batch-preview-card batch-warn">
              <div class="batch-preview-title">
                <span>{{ item.date }} · {{ item.name || '未知' }}</span>
                <span class="batch-phone">手机: {{ item.phoneCount }} <template v-if="item.matchedEmployee">（登记{{ item.matchedEmployee.phoneTotal }}台，差{{ Math.abs(item.phoneCount - item.matchedEmployee.phoneTotal) }}台）</template></span>
              </div>
              <div class="batch-preview-stats">
                <span v-for="s in item.platformStats" :key="s.platform" class="batch-stat-item">
                  {{ s.label }}: 在用{{ s.active }} 发{{ s.posting }} 禁{{ s.disabled }}
                </span>
              </div>
              <div class="batch-warnings">
                <div v-for="w in item.warnings" :key="w" class="batch-warn-item">⚠️ {{ w }}</div>
              </div>
              <div class="batch-card-actions">
                <el-button size="mini" type="primary" plain @click="acceptReportValue(item)">以汇报为准</el-button>
                <el-button size="mini" plain @click="acceptRegisteredValue(item)">以登记为准</el-button>
              </div>
            </div>
          </template>

          <!-- 正常人员 -->
          <template v-if="normalItems.length">
            <div v-if="phoneWarningItems.length" class="batch-section-label batch-section-ok">
              <i class="el-icon-circle-check"></i> 数据一致，可直接保存
            </div>
            <div v-for="(item, idx) in normalItems" :key="'n'+idx" class="batch-preview-card">
              <div class="batch-preview-title">
                <span>{{ item.date }} · {{ item.name || '未知' }}</span>
                <span class="batch-phone">手机: {{ item.phoneCount }}</span>
              </div>
              <div class="batch-preview-stats">
                <span v-for="s in item.platformStats" :key="s.platform" class="batch-stat-item">
                  {{ s.label }}: 在用{{ s.active }} 发{{ s.posting }} 禁{{ s.disabled }}
                </span>
              </div>
            </div>
          </template>

          <!-- 未匹配人员 -->
          <template v-if="notMatchedItems.length">
            <div class="batch-section-label batch-section-err">
              <i class="el-icon-circle-close"></i> 以下员工未在系统中找到，无法保存
            </div>
            <div v-for="(item, idx) in notMatchedItems" :key="'e'+idx" class="batch-preview-card batch-err">
              <div class="batch-preview-title">
                <span>{{ item.date }} · {{ item.name || '未知' }}</span>
                <el-tag type="danger" size="mini">未匹配员工</el-tag>
              </div>
              <div class="batch-preview-stats">
                <span v-for="s in item.platformStats" :key="s.platform" class="batch-stat-item">
                  {{ s.label }}: 在用{{ s.active }} 发{{ s.posting }} 禁{{ s.disabled }}
                </span>
              </div>
              <div class="batch-warnings">
                <div v-for="w in item.warnings" :key="w" class="batch-warn-item">⚠️ {{ w }}</div>
              </div>
            </div>
          </template>
        </div>
        <div class="batch-actions">
          <el-button @click="batchStep = 1">返回修改</el-button>
          <div class="batch-save-area">
            <span v-if="phoneWarningItems.length" class="batch-warn-hint">⚠ 含 {{ phoneWarningItems.length }} 个不一致数据，将自动跳过</span>
            <el-button type="primary" @click="submitBatchReports" :loading="batchSaving" :disabled="normalItems.length === 0">
              <i class="el-icon-check"></i> 确认保存 ({{ normalItems.length }}人)
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listEmployee, getBaseData,
  getReportsByDate, submitReport, deleteReport,
  getChecksByDate, updateDailyCheck, updateEmployee
} from '@/api/device/index'

const AVATAR_COLORS = ['#3b82f6','#10b981','#f59e0b','#ef4444','#8b5cf6','#ec4899','#06b6d4','#f97316']
const STATUS_LABELS = { pending: '待核对', confirmed: '已核对', absent: '未报到' }
const CHECK_CYCLE = ['pending', 'confirmed', 'absent']

export default {
  name: 'DeviceReport',
  data() {
    const today = new Date()
    const pad = n => String(n).padStart(2, '0')
    const todayStr = `${today.getFullYear()}-${pad(today.getMonth()+1)}-${pad(today.getDate())}`
    return {
      activeTab: 'report',
      reportDate: todayStr,
      checkDate: todayStr,
      employees: [],
      platforms: [],
      reportMap: {},
      checkMap: {},
      checkNoteMap: {},
      formMap: {},
      expandedCards: {},
      savingMap: {},
      loading: false,
      filterStatus: 'all',
      filterDept: '',
      // 批量导入
      showBatchDialog: false,
      batchStep: 1,
      batchRawText: '',
      batchParsed: [],
      batchSaving: false
    }
  },
  computed: {
    // 批量预览：手机数不一致的项（置顶）
    phoneWarningItems() {
      return this.batchParsed.filter(p => p.matchedEmployee && p.phoneCount !== p.matchedEmployee.phoneTotal)
    },
    // 批量预览：正常项（数据一致的）
    normalItems() {
      return this.batchParsed.filter(p => p.matchedEmployee && p.phoneCount === p.matchedEmployee.phoneTotal)
    },
    // 批量预览：未匹配员工
    notMatchedItems() {
      return this.batchParsed.filter(p => !p.matchedEmployee && p.name)
    },
    sortedEmployees() {
      return [...this.employees].sort((a, b) => {
        const ra = this.reportMap[a.id], rb = this.reportMap[b.id]
        const wa = ra && ra.phoneCount !== a.phoneTotal ? -1 : 0
        const wb = rb && rb.phoneCount !== b.phoneTotal ? -1 : 0
        if (wa !== wb) return wa - wb
        if (!ra && rb) return -1
        if (ra && !rb) return 1
        return 0
      })
    },
    submittedCount() {
      return Object.keys(this.reportMap).length
    },
    checkStats() {
      const counts = { confirmed: 0, absent: 0, pending: 0 }
      Object.values(this.checkMap).forEach(c => {
        counts[c.status] = (counts[c.status] || 0) + 1
      })
      counts.pending += this.employees.length - Object.keys(this.checkMap).length
      return counts
    },
    progressPercent() {
      if (this.employees.length === 0) return 0
      return Math.round(this.checkStats.confirmed / this.employees.length * 100)
    },
    departments() {
      return Array.from(new Set(this.employees.map(e => e.department).filter(Boolean)))
    },
    groupedByDept() {
      const groups = {}
      const visible = this.employees.filter(e => {
        if (this.filterStatus !== 'all' && this.getCheckStatus(e.id) !== this.filterStatus) return false
        if (this.filterDept && e.department !== this.filterDept) return false
        return true
      })

      visible.forEach(emp => {
        const dept = emp.department || '其他'
        if (!groups[dept]) groups[dept] = []
        groups[dept].push(emp)
      })

      return Object.keys(groups).map(name => ({
        name,
        employees: groups[name]
      }))
    },
    statusLabels() {
      return STATUS_LABELS
    }
  },
  created() {
    this.init()
  },
  methods: {
    async init() {
      const res = await getBaseData()
      this.employees = res.data.employees || []
      this.platforms = res.data.accountPlatforms || []
      this.initForms()
      await Promise.all([this.loadReports(), this.loadChecks()])
    },
    initForms() {
      this.employees.forEach(emp => {
        this.$set(this.formMap, emp.id, {
          phoneCount: emp.phoneTotal,
          platformStats: this.platforms.map(p => ({
            platform: p.code, active: 0, posting: 0, disabled: 0, note: ''
          }))
        })
      })
    },
    async loadReports() {
      this.loading = true
      try {
        const res = await getReportsByDate(this.reportDate)
        this.reportMap = {}
        ;(res.data || []).forEach(r => {
          this.$set(this.reportMap, r.employeeId, r)
          if (this.formMap[r.employeeId]) {
            this.$set(this.formMap[r.employeeId], 'phoneCount', r.phoneCount)
            try {
              const stats = JSON.parse(r.platformStats || '[]')
              if (stats.length > 0) {
                this.$set(this.formMap[r.employeeId], 'platformStats', this.platforms.map(p => {
                  const s = stats.find(x => x.platform === p.code) || {}
                  return { platform: p.code, active: s.active || 0, posting: s.posting || 0, disabled: s.disabled || 0, note: s.note || '' }
                }))
              }
            } catch(e) {}
          }
        })
      } finally {
        this.loading = false
      }
    },
    async loadChecks() {
      const res = await getChecksByDate(this.checkDate)
      this.checkMap = {}
      this.checkNoteMap = {}
      ;(res.data || []).forEach(c => {
        this.$set(this.checkMap, c.employeeId, c)
        this.$set(this.checkNoteMap, c.employeeId, c.note || '')
      })
    },
    toggleCard(empId) {
      this.$set(this.expandedCards, empId, !this.expandedCards[empId])
    },
    async saveReport(emp) {
      this.$set(this.savingMap, emp.id, true)
      const form = this.formMap[emp.id]
      try {
        await submitReport({
          employeeId: emp.id,
          reportDate: this.reportDate,
          phoneCount: form.phoneCount,
          platformStats: JSON.stringify(form.platformStats)
        })
        // 同步更新员工登记数（汇报数与登记数一致或汇报数有效时）
        if (form.phoneCount > 0 && form.phoneCount !== emp.phoneTotal) {
          try {
            await updateEmployee({ id: emp.id, name: emp.name, phoneTotal: form.phoneCount })
            emp.phoneTotal = form.phoneCount
          } catch(e) {
            // 同步失败不影响汇报保存
            console.warn('同步登记数失败', e)
          }
        }
        this.$message.success('保存成功')
        await this.loadReports()
      } catch(e) {
        this.$message.error('保存失败')
      } finally {
        this.$set(this.savingMap, emp.id, false)
      }
    },
    async deleteReport(id) {
      try {
        await this.$confirm('确定删除该汇报记录？', '提示', { type: 'warning' })
        await deleteReport(id)
        this.$message.success('删除成功')
        await this.loadReports()
      } catch(e) {}
    },
    async cycleCheck(emp) {
      const cur = this.checkMap[emp.id] ? this.checkMap[emp.id].status : 'pending'
      const idx = CHECK_CYCLE.indexOf(cur)
      const next = CHECK_CYCLE[(idx + 1) % CHECK_CYCLE.length]
      await updateDailyCheck({
        employeeId: emp.id,
        checkDate: this.checkDate,
        status: next,
        note: this.checkNoteMap[emp.id] || ''
      })
      await this.loadChecks()
    },
    async setAllStatus(status) {
      try {
        await this.$confirm(`确定将全部员工设置为"${STATUS_LABELS[status]}"？`, '提示', { type: 'warning' })
        // 批量更新
        for (const emp of this.employees) {
          await updateDailyCheck({
            employeeId: emp.id,
            checkDate: this.checkDate,
            status: status,
            note: this.checkNoteMap[emp.id] || ''
          })
        }
        this.$message.success('批量设置成功')
        await this.loadChecks()
      } catch(e) {}
    },
    async saveCheck(emp) {
      const cur = this.checkMap[emp.id]
      if (!cur) return
      await updateDailyCheck({
        employeeId: emp.id,
        checkDate: this.checkDate,
        status: cur.status,
        note: this.checkNoteMap[emp.id] || ''
      })
    },
    getCheckStatus(empId) {
      return this.checkMap[empId] ? this.checkMap[empId].status : 'pending'
    },
    checkStatusLabel(empId) {
      const s = this.getCheckStatus(empId)
      return STATUS_LABELS[s]
    },
    getCheckBtnType(empId) {
      const s = this.getCheckStatus(empId)
      return { pending: '', confirmed: 'success', absent: 'danger' }[s]
    },
    getCheckClass(empId) {
      const s = this.getCheckStatus(empId)
      return { confirmed: 'check-confirmed', absent: 'check-absent', pending: 'check-pending' }[s]
    },
    getCheckHeaderClass(empId) {
      const s = this.getCheckStatus(empId)
      return { confirmed: 'header-confirmed', absent: 'header-absent', pending: 'header-pending' }[s]
    },
    getDeptConfirmed(group) {
      return group.employees.filter(e => this.getCheckStatus(e.id) === 'confirmed').length
    },
    shouldShowEmp(emp) {
      if (this.filterStatus !== 'all' && this.getCheckStatus(emp.id) !== this.filterStatus) return false
      if (this.filterDept && emp.department !== this.filterDept) return false
      return true
    },
    getLatestReport(empId) {
      // 这里应该获取员工最新的汇报记录，暂时返回null
      return null
    },
    avatarColor(name) {
      let h = 0
      for (const c of (name || '')) h = ((h * 31) + c.charCodeAt(0)) & 0xffff
      return AVATAR_COLORS[h % AVATAR_COLORS.length]
    },
    getAvatarBg(emp) {
      const status = this.getCheckStatus(emp.id)
      if (status === 'confirmed') return '#22c55e'
      if (status === 'absent') return '#ef4444'
      return this.avatarColor(emp.name)
    },
    getPlatformLabel(code) {
      const p = this.platforms.find(x => x.code === code)
      return p ? p.label : code
    },
    platformStyle(code) {
      const colorMap = { black:'#1a1a2e', blue:'#3b82f6', red:'#ef4444', orange:'#f97316', slate:'#94a3b8' }
      const p = this.platforms.find(x => x.code === code)
      const bg = p ? (colorMap[p.color] || '#94a3b8') : '#94a3b8'
      return { background: bg, color: '#fff', border: 'none' }
    },
    parsePlatformStats(jsonStr) {
      if (!jsonStr) return []
      try { return JSON.parse(jsonStr) } catch (e) { return [] }
    },
    formatDate(val) {
      if (!val) return '-'
      return String(val).substring(0, 10)
    },
    formatDateTime(val) {
      if (!val) return '-'
      return String(val).substring(0, 16)
    },

    // ===== 批量粘贴导入 =====
    openBatchDialog() {
      this.batchStep = 1
      this.batchRawText = ''
      this.batchParsed = []
      this.showBatchDialog = true
    },
    parseBatchText() {
      const text = this.batchRawText.trim()
      if (!text) return

      const lines = text.split(/\n/)
      const records = []
      let current = null

      // 构建平台关键词映射
      const platformMap = this.buildPlatformKeywordMap()

      for (let line of lines) {
        line = line.trim()
        if (!line) continue

        // 检测日期行（如 3.28 或 4.30）
        const dateMatch = line.match(/^(\d{1,2})[.／/](\d{1,2})$/)
        if (dateMatch) {
          if (current) records.push(current)
          const month = parseInt(dateMatch[1])
          const day = parseInt(dateMatch[2])
          const year = new Date().getFullYear()
          const m = String(month).padStart(2, '0')
          const d = String(day).padStart(2, '0')
          current = {
            date: `${year}-${m}-${d}`,
            name: '',
            phoneCount: 0,
            platformStats: this.platforms.map(p => ({
              platform: p.code, label: p.label, active: 0, posting: 0, disabled: 0, note: ''
            })),
            warnings: [],
            matchedEmployee: null
          }
          continue
        }

        if (!current) {
          // 第一行不是日期，自动创建今天的记录
          current = {
            date: this.reportDate,
            name: '',
            phoneCount: 0,
            platformStats: this.platforms.map(p => ({
              platform: p.code, label: p.label, active: 0, posting: 0, disabled: 0, note: ''
            })),
            warnings: ['首行未识别为日期，使用当前选择日期'],
            matchedEmployee: null
          }
        }

        // 解析键值对
        const kvMatch = line.match(/^(.+?)[：:]\s*(.*)$/)
        if (!kvMatch) continue

        const key = kvMatch[1].trim()
        const rawVal = kvMatch[2].trim()
        // 提取数字（支持"3（禁言30天）"这种格式）
        const numMatch = rawVal.match(/^(\d+)/)
        const numVal = numMatch ? parseInt(numMatch[1]) : 0
        // 提取括号内备注
        const noteMatch = rawVal.match(/[（(](.+?)[）)]/)
        const noteVal = noteMatch ? noteMatch[1] : ''

        if (key.includes('姓名') || key.includes('名字')) {
          current.name = rawVal
        } else if (key.includes('手机')) {
          current.phoneCount = numVal
        } else {
          // 尝试匹配平台
          const matched = this.matchPlatformKey(key, platformMap)
          if (matched) {
            const stat = current.platformStats.find(s => s.platform === matched.platform)
            if (stat) {
              if (matched.field === 'active') stat.active = numVal
              else if (matched.field === 'posting') stat.posting = numVal
              else if (matched.field === 'disabled') {
                stat.disabled = numVal
                if (noteVal) stat.note = noteVal
              }
            }
          } else if (key.includes('推客户')) {
            // 当日推客户，忽略或存为备注
          }
        }
      }

      if (current) records.push(current)

      // 匹配员工 & 校验手机数（只核对手机数与登记数，平台数据以用户汇报为准不核对）
      records.forEach(rec => {
        const emp = this.employees.find(e => e.name === rec.name)
        if (emp) {
          rec.matchedEmployee = emp
          if (rec.phoneCount !== emp.phoneTotal) {
            rec.warnings.push(`手机数不一致：汇报${rec.phoneCount}台，登记${emp.phoneTotal}台`)
          }
        } else if (rec.name) {
          rec.warnings.push(`未找到员工"${rec.name}"，请先在系统设置中添加`)
        }
      })

      // 不一致的置顶排序
      records.sort((a, b) => {
        const wa = a.matchedEmployee && a.phoneCount !== a.matchedEmployee.phoneTotal ? -1 : 0
        const wb = b.matchedEmployee && b.phoneCount !== b.matchedEmployee.phoneTotal ? -1 : 0
        if (wa !== wb) return wa - wb
        const na = !a.matchedEmployee ? -1 : 0
        const nb = !b.matchedEmployee ? -1 : 0
        if (na !== nb) return na - nb
        return 0
      })

      this.batchParsed = records
      this.batchStep = 2
    },

    // 以汇报为准：更新登记数为汇报数，清除不一致警告
    async acceptReportValue(item) {
      if (item.matchedEmployee) {
        try {
          await updateEmployee({ id: item.matchedEmployee.id, name: item.matchedEmployee.name, phoneTotal: item.phoneCount })
          item.matchedEmployee.phoneTotal = item.phoneCount
          item.warnings = item.warnings.filter(w => !w.includes('手机数不一致'))
          this.$set(this, 'batchParsed', [...this.batchParsed])
          this.$message.success(`${item.name}：已按汇报数${item.phoneCount}台更新登记数`)
        } catch(e) {
          this.$message.error(`${item.name}：更新登记数失败`)
        }
      }
    },

    // 以登记为准：更新汇报数为登记数，清除不一致警告
    acceptRegisteredValue(item) {
      if (item.matchedEmployee) {
        item.phoneCount = item.matchedEmployee.phoneTotal
        item.warnings = item.warnings.filter(w => !w.includes('手机数不一致'))
        this.$set(this, 'batchParsed', [...this.batchParsed])
        this.$message.success(`${item.name}：已按登记数${item.phoneCount}台修改汇报数`)
      }
    },

    buildPlatformKeywordMap() {
      // 构建关键词 -> { platform, field } 映射
      const map = []
      this.platforms.forEach(p => {
        const code = p.code
        const label = p.label
        // 平台名本身作为关键词
        const nameKeywords = [label]
        // 加入别名
        try {
          const actAliases = JSON.parse(p.activeAliases || '[]')
          const disAliases = JSON.parse(p.disabledAliases || '[]')
          const postAliases = JSON.parse(p.postingAliases || '[]')

          // 在用别名 -> active
          actAliases.forEach(a => {
            if (a) map.push({ keyword: a, platform: code, field: 'active' })
          })
          // 不能用别名 -> disabled
          disAliases.forEach(a => {
            if (a) map.push({ keyword: a, platform: code, field: 'disabled' })
          })
          // 发作品别名 -> posting
          postAliases.forEach(a => {
            if (a) map.push({ keyword: a, platform: code, field: 'posting' })
          })
        } catch(e) {}

        // 平台名默认映射
        map.push({ keyword: `在用${label}`, platform: code, field: 'active' })
        map.push({ keyword: `${label}`, platform: code, field: 'active' })
        map.push({ keyword: `不能用${label}`, platform: code, field: 'disabled' })
        map.push({ keyword: `发作品${label}`, platform: code, field: 'posting' })
      })
      return map
    },

    matchPlatformKey(key, platformMap) {
      // 精确匹配
      let found = platformMap.find(m => key === m.keyword)
      if (found) return found
      // 包含匹配
      found = platformMap.find(m => key.includes(m.keyword) || m.keyword.includes(key))
      if (found) return found
      return null
    },

    async submitBatchReports() {
      // 保存所有已匹配员工的记录，自动同步phoneTotal为汇报数
      const validRecords = this.batchParsed.filter(p => p.matchedEmployee)
      const notMatched = this.batchParsed.filter(p => !p.matchedEmployee && p.name)

      if (validRecords.length === 0) {
        this.$message.warning('没有可提交的记录')
        return
      }

      this.batchSaving = true
      let successCount = 0
      let failCount = 0

      try {
        for (const rec of validRecords) {
          try {
            await submitReport({
              employeeId: rec.matchedEmployee.id,
              reportDate: rec.date,
              phoneCount: rec.phoneCount,
              platformStats: JSON.stringify(rec.platformStats.map(s => ({
                platform: s.platform, active: s.active, posting: s.posting, disabled: s.disabled, note: s.note
              })))
            })
            // 自动同步phoneTotal为汇报数
            if (rec.phoneCount > 0 && rec.phoneCount !== rec.matchedEmployee.phoneTotal) {
              try {
                await updateEmployee({ id: rec.matchedEmployee.id, name: rec.matchedEmployee.name, phoneTotal: rec.phoneCount })
                rec.matchedEmployee.phoneTotal = rec.phoneCount
              } catch(e) {
                console.warn('同步登记数失败', e)
              }
            }
            successCount++
          } catch(e) {
            failCount++
          }
        }

        let msg = `成功导入 ${successCount} 条记录`
        if (notMatched.length > 0) {
          msg += `，${notMatched.length} 人未匹配无法保存`
        }
        if (failCount > 0) {
          msg += `，${failCount} 条失败`
        }
        this.$message.success(msg)

        await this.loadReports()
        this.showBatchDialog = false
        this.batchStep = 1
        this.batchRawText = ''
        this.batchParsed = []
      } finally {
        this.batchSaving = false
      }
    }
  }
}
</script>

<style scoped>
.daily-management-page {
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
  gap: 12px;
  flex-wrap: wrap;
}
.date-picker {
  width: 160px;
}
.submit-badge {
  font-size: 13px;
}
.refresh-btn {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* ========== 空状态 ========== */
.empty-state {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  padding: 48px;
  text-align: center;
  color: #94a3b8;
}
.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.4;
}

/* ========== 汇报卡片 ========== */
.report-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.report-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
  transition: all 0.2s;
}
.report-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}
.report-card.has-warning {
  border-color: #fcd34d;
}
.report-card-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #fafafa;
  cursor: pointer;
  transition: background 0.2s;
}
.report-card-header:hover {
  background: #f3f4f6;
}
.emp-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  margin-right: 12px;
  flex-shrink: 0;
}
.emp-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}
.emp-name {
  font-weight: 500;
  color: #1e293b;
}
.emp-dept {
  font-size: 12px;
  color: #9ca3af;
}
.emp-status-tags {
  display: flex;
  gap: 4px;
  margin-right: 12px;
}
.expand-icon {
  color: #cbd5e1;
  transition: color 0.2s;
}
.report-card:hover .expand-icon {
  color: #60a5fa;
}
.report-card-body {
  padding: 16px;
  border-top: 1px solid #f1f5f9;
}
.qty-input {
  width: 140px;
}
.qty-warn {
  color: #f59e0b;
  margin-left: 8px;
  font-size: 12px;
}
.platform-table {
  margin-top:  8px;
}
.small-input {
  width: 60px;
}

/* ========== 核对统计卡片 ========== */
.check-stats-cards {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
.stat-mini-card {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid;
}
.stat-confirmed {
  background: #f0fdf4;
  border-color: #86efac;
  color: #16a34a;
}
.stat-pending {
  background: #f8fafc;
  border-color: #e2e8f0;
  color: #64748b;
}
.stat-absent {
  background: #fef2f2;
  border-color: #fca5a5;
  color: #dc2626;
}

/* ========== 进度条 ========== */
.progress-section {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  padding: 16px;
  margin-bottom: 20px;
}
.progress-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 12px;
  color: #94a3b8;
}
.progress-text {
  font-weight: 600;
  color: #334155;
}
.progress-bar {
  height: 10px;
  background: #f1f5f9;
  border-radius: 999px;
  overflow: hidden;
}
.progress-fill-confirmed {
  height: 100%;
  background: #22c55e;
  border-radius: 999px;
  transition: width 0.5s;
}

/* ========== 工具栏 ========== */
.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.toolbar-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}
.toolbar-divider {
  width: 1px;
  height: 20px;
  background: #e2e8f0;
  margin: 0 4px;
}
.filter-group {
  display: flex;
  align-items: center;
}
.filter-btn {
  margin: 0;
}
.dept-filter {
  width: 140px;
}

/* ========== 部门分组 ========== */
.dept-group {
  margin-bottom: 24px;
}
.dept-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.dept-name {
  font-size: 11px;
  font-weight: 700;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.dept-divider {
  flex: 1;
  height: 1px;
  background: #e2e8f0;
}
.dept-badge {
  font-size: 11px;
  color: #94a3b8;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 999px;
  font-weight: 500;
}

/* ========== 核对卡片 ========== */
.check-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.check-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 12px;
}
.check-card {
  background: #fff;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  overflow: hidden;
  transition: all 0.2s;
}
.check-card.check-confirmed {
  border-color: #86efac;
  box-shadow: 0 4px 12px rgba(34,197,94,0.1);
}
.check-card.check-absent {
  border-color: #fca5a5;
  box-shadow: 0 4px 12px rgba(239,68,68,0.1);
}
.check-card-header {
  display: flex;
  align-items: center;
  padding: 12px;
  gap: 8px;
  transition: background 0.2s;
}
.check-card-header.header-confirmed {
  background: #f0fdf4;
}
.check-card-header.header-absent {
  background: #fef2f2;
}
.check-card-header.header-pending {
  background: #f8fafc;
}
.check-emp-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
  font-size: 12px;
  flex-shrink: 0;
}
.check-emp-info {
  flex: 1;
  min-width: 0;
}
.check-emp-name {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
}
.check-emp-dept {
  font-size: 11px;
  color: #94a3b8;
}
.check-status-btn {
  flex-shrink: 0;
}

.check-card-body {
  padding: 12px;
  border-top: 1px solid #f1f5f9;
}
.phone-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #64748b;
  margin-bottom: 8px;
}
.phone-qty {
  font-weight: 700;
  color: #1e293b;
}
.phone-qty.qty-mismatch {
  color: #ef4444;
}
.phone-warn {
  font-size: 11px;
  color: #f59e0b;
}
.report-preview {
  margin-bottom: 8px;
}
.report-date {
  font-size: 11px;
  color: #94a3b8;
  margin-bottom: 4px;
}
.platform-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.platform-tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 999px;
  font-weight: 700;
}
.no-report {
  font-size: 11px;
  color: #94a3b8;
  margin-bottom: 8px;
}
.note-section {
  margin-bottom: 8px;
}
.note-input {
  font-size: 12px;
}
.check-time {
  font-size: 11px;
  color: #94a3b8;
  border-top: 1px solid #f1f5f9;
  padding-top: 6px;
}

/* ========== Tabs样式 ========== */
.main-tabs {
  background: transparent;
}
.main-tabs >>> .el-tabs__header {
  background: transparent;
  border-bottom: 2px solid #e2e8f0;
}
.main-tabs >>> .el-tabs__item {
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  border: none;
  background: transparent;
}
.main-tabs >>> .el-tabs__item.is-active {
  color: #3b82f6;
  background: transparent;
  border-bottom: 2px solid #3b82f6;
}
.main-tabs >>> .el-tabs__content {
  padding: 20px 0;
}

/* ========== 批量粘贴导入 ========== */
.batch-import-dialog {
  max-width: 700px;
}
.batch-hint {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
  font-size: 13px;
  color: #475569;
}
.batch-hint p {
  margin: 4px 0;
}
.batch-format-example {
  background: #1e293b;
  color: #a5f3fc;
  padding: 10px 14px;
  border-radius: 6px;
  font-size: 12px;
  line-height: 1.6;
  margin: 8px 0;
  overflow-x: auto;
}
.batch-textarea textarea {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
}
.batch-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
.batch-preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
  color: #1e293b;
}
.batch-preview-list {
  max-height: 400px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.batch-preview-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px 16px;
}
.batch-preview-card.batch-warn {
  border-color: #fb923c;
  background: #fff7ed;
}
.batch-preview-card.batch-err {
  border-color: #fca5a5;
  background: #fef2f2;
}
.batch-preview-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  margin-bottom: 6px;
  flex-wrap: wrap;
}
.batch-phone {
  font-weight: 400;
  color: #64748b;
  font-size: 12px;
}
.batch-preview-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  font-size: 12px;
  color: #475569;
}
.batch-stat-item {
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 4px;
}
.batch-warnings {
  margin-top: 6px;
}
.batch-warn-item {
  font-size: 12px;
  color: #d97706;
}
.batch-card-actions {
  margin-top: 8px;
  display: flex;
  gap: 6px;
}
/* 摘要标签 */
.batch-summary-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}
.batch-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 6px;
  font-weight: 500;
}
.batch-tag-ok {
  background: #ecfdf5;
  border: 1px solid #a7f3d0;
  color: #059669;
}
.batch-tag-warn {
  background: #fff7ed;
  border: 1px solid #fed7aa;
  color: #ea580c;
  font-weight: 700;
}
.batch-tag-err {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
}
/* 分区标签 */
.batch-section-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
  padding: 6px 0 2px;
}
.batch-section-warn {
  color: #ea580c;
}
.batch-section-ok {
  color: #059669;
}
.batch-section-err {
  color: #dc2626;
}
/* 保存区域 */
.batch-save-area {
  display: flex;
  align-items: center;
  gap: 10px;
}
.batch-warn-hint {
  font-size: 12px;
  color: #ea580c;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  padding: 4px 10px;
  border-radius: 6px;
}

@media (max-width: 768px) {
  .batch-import-dialog {
    max-width: none !important;
  }
  .batch-preview-stats {
    font-size: 11px;
  }
}
</style>
