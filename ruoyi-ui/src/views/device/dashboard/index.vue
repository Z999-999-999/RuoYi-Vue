<template>
  <div class="dashboard-wrapper">
    <!-- ========== 页面头部 ========== -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">总览</h1>
        <p class="page-date">{{ currentDate }}</p>
      </div>
      <div class="header-right">
        <button
          v-if="errorCount > 0"
          class="alert-btn"
          @click="$router.push('/device/logs')"
          title="点击查看告警日志"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" x2="12" y1="8" y2="12"></line><line x1="12" x2="12.01" y1="16" y2="16"></line></svg>
          <span>{{ errorCount }} 条告警</span>
          <span class="alert-badge">
            <span class="alert-dot"></span>
            新
          </span>
        </button>
        <button class="refresh-btn" @click="loadAll" :disabled="loading">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"></path><path d="M21 3v5h-5"></path><path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"></path><path d="M8 16H3v5"></path></svg>
          刷新
        </button>
      </div>
    </div>

    <!-- ========== 顶部统计卡片 ========== -->
    <div class="stat-cards">
      <!-- 公司总数 - 差异时红色，一致时绿色 -->
      <div class="stat-card" :class="isBalanced ? 'stat-green' : 'stat-red'" @click="showDiffDialog = true" style="cursor:pointer;">
        <div class="stat-label">公司总数</div>
        <div class="stat-value">{{ dashData.companyTotal || 0 }}</div>
        <div class="stat-sub">
          <span>设定值</span>
          <span class="stat-badge" :class="isBalanced ? 'badge-green' : 'badge-red'">
            {{ isBalanced ? '✓ 一致' : '⚠ 差异' }}
          </span>
        </div>
      </div>

      <!-- 员工持有 -->
      <div class="stat-card stat-white clickable-card" @click="$router.push('/device/settings?tab=employee')">
        <div class="stat-header">
          <span class="stat-label text-gray">员工持有</span>
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-blue-400"><rect width="14" height="20" x="5" y="2" rx="2" ry="2"></rect><path d="M12 18h.01"></path></svg>
        </div>
        <div class="stat-value text-blue">{{ dashData.employeeTotal || 0 }}</div>
        <div class="stat-sub text-gray">{{ (baseData.employees || []).length }} 名员工</div>
      </div>

      <!-- 库房库存 -->
      <div class="stat-card stat-white clickable-card" @click="$router.push('/device/warehouse')">
        <div class="stat-header">
          <span class="stat-label text-gray">库房库存</span>
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-emerald-400"><path d="M18 21V10a1 1 0 0 0-1-1H7a1 1 0 0 0-1 1v11"></path><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V8a2 2 0 0 1 1.132-1.803l7.95-3.974a2 2 0 0 1 1.837 0l7.948 3.974A2 2 0 0 1 22 8z"></path><path d="M6 13h12"></path><path d="M6 17h12"></path></svg>
        </div>
        <div class="stat-value text-emerald">{{ dashData.warehouseTotal || 0 }}</div>
        <div class="stat-sub text-gray">{{ (baseData.warehouseCategories || []).length }} 个分类</div>
      </div>

      <!-- 在外设备 -->
      <div class="stat-card stat-white clickable-card" @click="$router.push('/device/settings?tab=outside')">
        <div class="stat-header">
          <span class="stat-label text-gray">在外设备</span>
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-orange-400"><path d="M16 7h6v6"></path><path d="m22 7-8.5 8.5-5-5L2 17"></path></svg>
        </div>
        <div class="stat-value text-orange">{{ dashData.outsideTotal || 0 }}</div>
        <div class="stat-sub text-gray">维修 / 外借</div>
      </div>
    </div>

    <!-- ========== 库房库存明细 + 月度排行 ========== -->
    <div class="content-grid">
      <!-- 库房库存明细 -->
      <div class="section-card warehouse-section">
        <div class="section-header">
          <div class="header-left">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-slate-400"><path d="M18 21V10a1 1 0 0 0-1-1H7a1 1 0 0 0-1 1v11"></path><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V8a2 2 0 0 1 1.132-1.803l7.95-3.974a2 2 0 0 1 1.837 0l7.948 3.974A2 2 0 0 1 22 8z"></path><path d="M6 13h12"></path><path d="M6 17h12"></path></svg>
            <span class="font-semibold">库房库存明细</span>
          </div>
          <span class="section-badge">合计 {{ dashData.warehouseTotal || 0 }} 台</span>
        </div>
        <div class="warehouse-list">
          <div v-for="stock in (dashData.warehouseStocks || [])" :key="stock.categoryId" class="warehouse-item">
            <div class="color-dot" :style="{ background: stock.categoryColor || '#888' }"></div>
            <span class="warehouse-name">{{ stock.categoryName }}</span>
            <div class="progress-bar">
              <div
                class="progress-fill"
                :style="{ width: dashData.warehouseTotal > 0 ? (stock.quantity / dashData.warehouseTotal * 100) + '%' : '0%', background: stock.categoryColor || '#3b82f6' }"
              ></div>
            </div>
            <span class="warehouse-qty" :style="{ color: stock.categoryColor || '#3b82f6' }">{{ stock.quantity }}</span>
            <span class="warehouse-unit">台</span>
          </div>
        </div>
      </div>

      <!-- 右侧排行 -->
      <div class="rankings-section">
        <!-- 出库排行 -->
        <div class="section-card rank-card">
          <div class="section-header">
            <div class="header-left">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-blue-400"><path d="M10 14.66v1.626a2 2 0 0 1-.976 1.696A5 5 0 0 0 7 21.978"></path><path d="M14 14.66v1.626a2 2 0 0 0 .976 1.696A5 5 0 0 1 17 21.978"></path><path d="M18 9h1.5a1 1 0 0 0 0-5H18"></path><path d="M4 22h16"></path><path d="M6 9a6 6 0 0 0 12 0V3a1 1 0 0 0-1-1H7a1 1 0 0 0-1 1z"></path><path d="M6 9H4.5a1 1 0 0 1 0-5H6"></path></svg>
              <span class="font-semibold">本月出库排行</span>
            </div>
            <div class="rank-filter">
              <select v-model="outRankCategoryId" @change="onOutRankCategoryChange" class="rank-category-select">
                <option :value="null">全部出库</option>
                <option v-for="cat in outCategories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
              </select>
            </div>
          </div>
          <div class="rank-list">
            <div v-for="(item, idx) in outRankList.slice(0, 5)" :key="item.employeeId" class="rank-item">
              <div class="rank-num" :class="getMedalClass(idx)">{{ idx + 1 }}</div>
              <span class="rank-name">{{ item.employeeName }}</span>
              <span class="rank-qty text-blue">{{ item.totalQty }}</span>
              <span class="rank-unit text-gray">台</span>
            </div>
            <div v-if="!outRankList.length" class="empty-tip">暂无数据</div>
          </div>
        </div>

        <!-- 入库排行 -->
        <div class="section-card rank-card">
          <div class="section-header">
            <div class="header-left">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-yellow-400"><path d="M10 14.66v1.626a2 2 0 0 1-.976 1.696A5 5 0 0 0 7 21.978"></path><path d="M14 14.66v1.626a2 2 0 0 0 .976 1.696A5 5 0 0 1 17 21.978"></path><path d="M18 9h1.5a1 1 0 0 0 0-5H18"></path><path d="M4 22h16"></path><path d="M6 9a6 6 0 0 0 12 0V3a1 1 0 0 0-1-1H7a1 1 0 0 0-1 1z"></path><path d="M6 9H4.5a1 1 0 0 1 0-5H6"></path></svg>
              <span class="font-semibold">本月入库排行</span>
            </div>
            <div class="rank-filter">
              <select v-model="inRankCategoryId" @change="onInRankCategoryChange" class="rank-category-select">
                <option :value="null">全部入库</option>
                <option v-for="cat in inCategories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
              </select>
            </div>
          </div>
          <div class="rank-list">
            <div v-for="(item, idx) in inRankList.slice(0, 5)" :key="item.employeeId" class="rank-item">
              <div class="rank-num" :class="getMedalClass(idx)">{{ idx + 1 }}</div>
              <span class="rank-name">{{ item.employeeName }}</span>
              <span class="rank-qty text-red">{{ item.totalQty }}</span>
              <span class="rank-unit text-gray">台</span>
            </div>
            <div v-if="!inRankList.length" class="empty-tip">暂无数据</div>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 员工设备分布 ========== -->
    <div class="section-card employees-section">
      <div class="section-header">
        <div class="header-left">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-slate-400"><rect width="14" height="20" x="5" y="2" rx="2" ry="2"></rect><path d="M12 18h.01"></path></svg>
          <span class="font-semibold">员工设备分布</span>
        </div>
        <span class="text-xs text-slate-400 ml-1">点击员工可查看详情</span>
      </div>

      <!-- 按组展示 -->
      <div v-for="group in groupedEmployees" :key="group.name" class="employee-group">
        <div class="group-header">
          <span class="group-label">{{ group.name }}</span>
          <div class="group-divider"></div>
          <span class="group-badge">{{ group.employees.length }}人 · 共 {{ groupTotal(group) }} 台</span>
        </div>
        <div class="employee-grid">
          <button
            v-for="emp in group.employees"
            :key="emp.id"
            class="emp-card"
            @click="openEmployeeDetail(emp)"
          >
            <!-- 第一行：头像 + 姓名 + 箭头 -->
            <div class="emp-row-1">
              <div class="emp-avatar" :style="{ background: avatarColor(emp.name) }">
                {{ (emp.name || '').slice(0, 1) }}
              </div>
              <div class="emp-name-box">
                <div class="emp-name">{{ emp.name }}</div>
              </div>
              <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="emp-arrow"><path d="m9 18 6-6-6-6"></path></svg>
            </div>
            <!-- 第二行：设备总数 -->
            <div class="emp-row-2">
              <span class="emp-phone-num">{{ emp.phoneTotal || 0 }}</span>
              <span class="emp-phone-label">台</span>
            </div>
            <!-- 第三行：平台标签 -->
            <div class="emp-row-3" v-if="emp._platformTags && emp._platformTags.length">
              <span
                v-for="tag in emp._platformTags.slice(0, 4)"
                :key="tag.code"
                class="emp-tag"
                :style="platformTagStyle(tag)"
              >{{ tag.label }}{{ tag.count }}</span>
              <span v-if="(emp.phoneTotal || 0) > emp._platformTags.reduce((s, t) => s + t.count, 0)" class="emp-tag-more">+{{ (emp.phoneTotal || 0) - emp._platformTags.reduce((s, t) => s + t.count, 0) }}</span>
            </div>
          </button>
        </div>
      </div>
    </div>

    <!-- ========== 最近操作日志 ========== -->
    <div class="section-card logs-section">
      <div class="section-header">
        <div class="header-left">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-slate-400"><path d="M16 17h6v-6"></path><path d="m22 17-8.5-8.5-5 5L2 7"></path></svg>
          <span class="font-semibold">最近操作日志</span>
        </div>
        <button class="view-all-btn" @click="$router.push('/device/logs')">查看完整记录 →</button>
      </div>
      <div class="logs-list">
        <div v-for="(log, idx) in (dashData.recentLogs || []).slice(0, 8)" :key="idx" class="log-item">
          <span class="log-level">INFO</span>
          <span class="log-time">{{ formatDateTime(log.logTime) }}</span>
          <span class="log-module">{{ log.module }}</span>
          <span class="log-message">{{ log.message }}</span>
        </div>
        <div v-if="!(dashData.recentLogs || []).length" class="empty-tip">暂无日志</div>
      </div>
    </div>

    <!-- ========== 数量差异分析弹窗 ========== -->
    <el-dialog title="设备数量差异分析" :visible.sync="showDiffDialog" width="90%" custom-class="diff-dialog-wrapper">
      <div class="diff-dialog">
        <p class="diff-formula">公式：员工持有 + 库房库存 + 在外设备 = 公司总数</p>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="diff-num-card">
              <div>公司设定总数</div>
              <div class="diff-num">{{ dashData.companyTotal || 0 }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="diff-num-card">
              <div>实际计算总数</div>
              <div class="diff-num" :class="isBalanced ? 'text-green' : 'text-red'">{{ calcTotal }}</div>
            </div>
          </el-col>
        </el-row>
        <el-divider />
        <div class="diff-detail">
          <div>员工持有：<b>{{ dashData.employeeTotal }}</b></div>
          <div>库房库存：<b>{{ dashData.warehouseTotal }}</b></div>
          <div>在外设备：<b>{{ dashData.outsideTotal }}</b></div>
          <div>= 合计：<b>{{ calcTotal }}</b></div>
        </div>
        <el-alert v-if="!isBalanced" type="warning" show-icon :closable="false"
          :title="`差异：${calcTotal > (dashData.companyTotal || 0) ? '多' : '少'} ${Math.abs(calcTotal - (dashData.companyTotal || 0))} 台`"
          style="margin-top:12px;"
        />
      </div>
    </el-dialog>

    <!-- ========== 员工详情弹窗 ========== -->
    <el-dialog
      :title="detailEmp ? `${detailEmp.name} - 设备详情` : '员工详情'"
      :visible.sync="showDetailDialog"
      width="90%"
      top="5vh"
      custom-class="emp-detail-dialog"
    >
      <div v-if="detailData" class="emp-detail">
        <!-- 员工基本信息 -->
        <div class="detail-header">
          <div class="detail-avatar" :style="{ background: avatarColor(detailEmp ? detailEmp.name : '') }">
            {{ detailEmp ? (detailEmp.name || '').slice(0,1) : '' }}
          </div>
          <div class="detail-meta">
            <div class="detail-name">{{ detailEmp ? detailEmp.name : '' }}</div>
            <div class="detail-dept">{{ detailEmp ? detailEmp.department : '' }}</div>
          </div>
          <div class="detail-phone-box">
            <div class="detail-phone-num">{{ detailEmp ? detailEmp.phoneTotal : 0 }}</div>
            <div class="detail-phone-label">持有设备数</div>
          </div>
        </div>

        <!-- 汇报记录 -->
        <div class="detail-section">
          <h4 class="detail-section-title"><i class="el-icon-date"></i> 最近汇报记录</h4>
          <el-table :data="detailData.dailyReports || []" size="mini" stripe max-height="200">
            <el-table-column label="日期" width="110">
              <template v-slot="{ row }">{{ formatDate(row.reportDate) }}</template>
            </el-table-column>
            <el-table-column label="手机数" width="80" prop="phoneCount" />
            <el-table-column label="平台统计">
              <template v-slot="{ row }">
                <div v-if="parsePlatformStats(row.platformStats).length" class="platform-tags">
                  <el-tag v-for="s in parsePlatformStats(row.platformStats)" :key="s.platform" size="mini"
                    :style="platformStatStyle(s)" style="margin:1px;"
                  >{{ getPlatformLabel(s.platform) }}: {{ s.active }}用 {{ s.posting }}发 {{ s.disabled }}停</el-tag>
                </div>
                <span v-else style="color:#999;">无</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 进出库记录 -->
        <div class="detail-section">
          <div class="detail-section-header">
            <h4 class="detail-section-title"><i class="el-icon-sort"></i> 进出库记录</h4>
            <span v-if="!inoutLoading && detailData.inoutRecords" class="inout-count">共 {{ detailData.inoutRecords.length }} 条</span>
            <span v-if="inoutLoading" class="inout-count"><i class="el-icon-loading"></i> 加载中</span>
          </div>
          <!-- 筛选栏 -->
          <div class="inout-filter-bar">
            <button
              :class="['filter-btn', inoutFilterMode === 'recent' ? 'active' : '']"
              @click="switchInoutMode('recent')"
            >近30条</button>
            <button
              :class="['filter-btn', inoutFilterMode === 'range' ? 'active' : '']"
              @click="switchInoutMode('range')"
            ><i class="el-icon-date"></i> 日期范围</button>
            <button
              :class="['filter-btn', inoutFilterMode === 'all' ? 'active' : '']"
              @click="switchInoutMode('all')"
            >全部</button>
            <template v-if="inoutFilterMode === 'range'">
              <input type="date" v-model="inoutStartDate" class="filter-date-input" />
              <span class="filter-date-sep">至</span>
              <input type="date" v-model="inoutEndDate" class="filter-date-input" />
              <button
                class="filter-btn filter-apply-btn"
                :disabled="inoutLoading || !inoutStartDate || !inoutEndDate"
                @click="applyInoutRange"
              >查询</button>
            </template>
          </div>
          <el-table :data="detailData.inoutRecords || []" size="mini" stripe max-height="260">
            <el-table-column label="类型" width="70">
              <template v-slot="{ row }">
                <el-tag :type="row.type === 'out' ? 'danger' : 'success'" size="mini">
                  {{ row.type === 'out' ? '出库' : '入库' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="数量" width="60" prop="quantity" />
            <el-table-column label="原因" width="110" prop="inoutCategoryName" show-overflow-tooltip />
            <el-table-column label="分类" width="90" prop="warehouseCategoryName" show-overflow-tooltip />
            <el-table-column label="备注" prop="note" show-overflow-tooltip />
            <el-table-column label="时间" width="140">
              <template v-slot="{ row }">{{ formatDateTime(row.recordTime) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <div v-else style="text-align:center;padding:40px;color:#999;">加载中...</div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getDashboard, getBaseData,
  getMonthlyOutRank, getMonthlyInRank,
  getEmployeeDetail, getEmployeePlatformTags
} from '@/api/device/index'

const AVATAR_COLORS = ['#3b82f6','#10b981','#f59e0b','#ef4444','#8b5cf6','#ec4899','#06b6d4','#f97316']

export default {
  name: 'DeviceDashboard',
  data() {
    return {
      loading: false,
      dashData: {},
      baseData: {},
      errorCount: 0,
      showDiffDialog: false,
      colorMap: null,
      outRankList: [],
      inRankList: [],
      outRankCategoryId: null,
      inRankCategoryId: null,
      showDetailDialog: false,
      detailEmp: null,
      detailData: null,
      inoutFilterMode: 'recent',
      inoutStartDate: '',
      inoutEndDate: '',
      inoutLoading: false
    }
  },
  computed: {
    currentDate() {
      const d = new Date()
      const days = ['日','一','二','三','四','五','六']
      return `${d.getFullYear()}年${d.getMonth()+1}月${d.getDate()}日 星期${days[d.getDay()]}`
    },
    calcTotal() {
      return (this.dashData.employeeTotal || 0) +
             (this.dashData.warehouseTotal || 0) +
             (this.dashData.outsideTotal || 0)
    },
    isBalanced() {
      const total = this.dashData.companyTotal || 0
      return total === 0 || total === this.calcTotal
    },
    groupedEmployees() {
      const employees = this.sortedEmployees
      const groups = {}
      employees.forEach(emp => {
        const groupName = emp.department || '其他'
        if (!groups[groupName]) groups[groupName] = []
        groups[groupName].push(emp)
      })
      return Object.keys(groups).map(name => ({
        name,
        employees: groups[name]
      }))
    },
    sortedEmployees() {
      const employees = this.baseData.employees || []
      const platforms = this.baseData.accountPlatforms || []
      return [...employees].sort((a, b) => (b.phoneTotal || 0) - (a.phoneTotal || 0)).map(emp => {
        const empWithTags = { ...emp, _platformTags: emp._platformTags || [] }
        return empWithTags
      })
    },
    outCategories() {
      const cats = this.baseData.inoutCategories || []
      return cats.filter(c => c.type === 'out' || c.type === 'both')
    },
    inCategories() {
      const cats = this.baseData.inoutCategories || []
      return cats.filter(c => c.type === 'in' || c.type === 'both')
    }
  },
  created() {
    this.loadAll()
    this.timer = setInterval(this.loadAll, 60000)
  },
  beforeDestroy() {
    clearInterval(this.timer)
  },
  methods: {
    async loadAll() {
      this.loading = true
      try {
        const [dash, base, tagsRes] = await Promise.all([
          getDashboard(),
          getBaseData(),
          getEmployeePlatformTags()
        ])
        this.dashData = dash.data || {}
        this.baseData = base.data || {}
        this.errorCount = this.dashData.errorCount || 0

        // 填充员工设备分类标签
        this.enrichEmployeeTags(tagsRes.data || [])

        // 首次加载时设置默认排行分类
        if (this.outRankCategoryId === null) {
          const outCats = (this.baseData.inoutCategories || []).filter(c => c.type === 'out' || c.type === 'both')
          const workCat = outCats.find(c => c.name.includes('作品'))
          if (workCat) this.outRankCategoryId = workCat.id
        }
        if (this.inRankCategoryId === null) {
          const inCats = (this.baseData.inoutCategories || []).filter(c => c.type === 'in' || c.type === 'both')
          const banCat = inCats.find(c => c.name === '永久禁言') || inCats.find(c => c.name.includes('永久禁言'))
          if (banCat) this.inRankCategoryId = banCat.id
        }

        const now = new Date()
        const yearMonth = `${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}`
        const [outRes, inRes] = await Promise.all([
          getMonthlyOutRank(yearMonth, this.outRankCategoryId),
          getMonthlyInRank(yearMonth, this.inRankCategoryId)
        ])
        this.outRankList = outRes.data || []
        this.inRankList = inRes.data || []
      } catch (e) {
        console.error('数据加载失败', e)
        this.$message.error('数据加载失败')
      } finally {
        this.loading = false
      }
    },
    enrichEmployeeTags(tagsData) {
      const employees = this.baseData.employees || []
      // 构建 employeeId -> tags 的映射
      const tagMap = {}
      ;(tagsData || []).forEach(item => {
        tagMap[item.employeeId] = item.tags || []
      })
      employees.forEach(emp => {
        emp._platformTags = tagMap[emp.id] || []
      })
    },
    async openEmployeeDetail(emp) {
      this.detailEmp = emp
      this.showDetailDialog = true
      this.detailData = null
      this.inoutFilterMode = 'recent'
      this.inoutStartDate = ''
      this.inoutEndDate = ''
      this.inoutLoading = false
      try {
        const res = await getEmployeeDetail(emp.id)
        this.detailData = res.data || {}
      } catch (e) {
        this.$message.error('加载员工详情失败')
      }
    },
    async loadRankData() {
      const now = new Date()
      const yearMonth = `${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}`
      const [outRes, inRes] = await Promise.all([
        getMonthlyOutRank(yearMonth, this.outRankCategoryId),
        getMonthlyInRank(yearMonth, this.inRankCategoryId)
      ])
      this.outRankList = outRes.data || []
      this.inRankList = inRes.data || []
    },
    async onOutRankCategoryChange() {
      await this.loadRankData()
    },
    async onInRankCategoryChange() {
      await this.loadRankData()
    },
    async switchInoutMode(mode) {
      this.inoutFilterMode = mode
      if (mode === 'recent') {
        await this.reloadInoutRecords()
      } else if (mode === 'all') {
        await this.reloadInoutRecords({ startDate: '2020-01-01', endDate: '2099-12-31' })
      }
      // range 模式不自动请求，等用户点查询
    },
    async applyInoutRange() {
      if (!this.inoutStartDate || !this.inoutEndDate) return
      await this.reloadInoutRecords({ startDate: this.inoutStartDate, endDate: this.inoutEndDate })
    },
    async reloadInoutRecords(params) {
      if (!this.detailEmp) return
      this.inoutLoading = true
      try {
        if (!params) {
          // recent 模式：不传日期参数，后端默认返回最近30条
          const res = await getEmployeeDetail(this.detailEmp.id)
          if (this.detailData) {
            this.$set(this.detailData, 'inoutRecords', (res.data || {}).inoutRecords || [])
          }
        } else {
          const res = await getEmployeeDetail(this.detailEmp.id, params)
          if (this.detailData) {
            this.$set(this.detailData, 'inoutRecords', (res.data || {}).inoutRecords || [])
          }
        }
      } catch (e) {
        this.$message.error('加载进出库记录失败')
      } finally {
        this.inoutLoading = false
      }
    },
    getMedalClass(idx) {
      if (idx === 0) return 'medal-gold'
      if (idx === 1) return 'medal-silver'
      if (idx === 2) return 'medal-bronze'
      return 'medal-normal'
    },
    groupTotal(group) {
      return (group.employees || []).reduce((s, e) => s + (e.phoneTotal || 0), 0)
    },
    parsePlatformStats(jsonStr) {
      if (!jsonStr) return []
      try { return JSON.parse(jsonStr) } catch (e) { return [] }
    },
    getPlatformLabel(code) {
      const platforms = this.baseData.accountPlatforms || []
      const p = platforms.find(x => x.code === code)
      return p ? p.label : code
    },
    platformStatStyle(stat) {
      const platforms = this.baseData.accountPlatforms || []
      const p = platforms.find(x => x.code === stat.platform)
      const bg = p ? (p.color || '#94a3b8') : '#94a3b8'
      return { background: bg, color: '#fff', border: 'none' }
    },
    platformTagStyle(tag) {
      const bg = tag.color || '#94a3b8'
      return { background: bg, color: '#fff', border: 'none' }
    },
    avatarColor(name) {
      let h = 0
      for (const c of (name || '')) h = ((h * 31) + c.charCodeAt(0)) & 0xffff
      return AVATAR_COLORS[h % AVATAR_COLORS.length]
    },
    formatDate(val) {
      if (!val) return '-'
      return String(val).substring(0, 10)
    },
    formatDateTime(val) {
      if (!val) return '-'
      return String(val).substring(0, 16)
    }
  }
}
</script>

<style scoped>
.dashboard-wrapper {
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
}
.alert-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 12px;
  border: 1px solid #fca5a5;
  background: #fef2f2;
  color: #dc2626;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.alert-btn:hover {
  background: #fee2e2;
}
.alert-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  font-weight: 600;
  color: #dc2626;
}
.alert-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #dc2626;
  animation: pulse 2s infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
.refresh-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #64748b;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.refresh-btn:hover:not(:disabled) {
  background: #f8fafc;
}
.refresh-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* ========== 统计卡片 ========== */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
.stat-card {
  border-radius: 16px;
  padding: 20px;
  transition: all 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.stat-green {
  background: linear-gradient(135deg, #16a34a, #15803d);
  color: #fff;
}
.stat-red {
  background: linear-gradient(135deg, #dc2626, #b91c1c);
  color: #fff;
}
.stat-white {
  background: #fff;
  border: 1px solid #e2e8f0;
}
.clickable-card {
  cursor: pointer;
}
.clickable-card:hover {
  border-color: #93c5fd;
  box-shadow: 0 4px 12px rgba(59,130,246,0.12);
}
.stat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.stat-label {
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 36px;
  font-weight: 800;
  line-height: 1.1;
  margin: 8px 0;
}
.stat-sub {
  font-size: 12px;
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.stat-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 999px;
}
.badge-green {
  background: rgba(255,255,255,0.2);
  color: #fff;
}
.badge-red {
  background: rgba(255,255,255,0.2);
  color: #fff;
}
.text-gray { color: #94a3b8; }
.text-blue { color: #2563eb; }
.text-emerald { color: #16a34a; }
.text-orange { color: #ea580c; }

/* ========== 内容网格 ========== */
.content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

/* ========== 通用区块卡片 ========== */
.section-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.font-semibold {
  font-weight: 600;
  color: #334155;
}
.section-badge {
  font-size: 12px;
  color: #94a3b8;
  background: #f1f5f9;
  padding: 4px 8px;
  border-radius: 999px;
  font-weight: 500;
}
.text-blue { color: #2563eb; }
.text-red { color: #dc2626; }

/* ========== 库房库存明细 ========== */
.warehouse-list {
  padding: 4px 0;
}
.warehouse-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 20px;
  border-bottom: 1px solid #f8fafc;
}
.warehouse-item:last-child {
  border-bottom: none;
}
.color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}
.warehouse-name {
  font-size: 13px;
  color: #475569;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.progress-bar {
  flex: 1;
  height: 6px;
  background: #f1f5f9;
  border-radius: 999px;
  overflow: hidden;
  display: none; /* 隐藏进度条，直接显示数字 */
}
.progress-fill {
  height: 100%;
  border-radius: 999px;
  transition: width 0.3s;
}
.warehouse-qty {
  font-size: 20px;
  font-weight: 700;
  min-width: 40px;
  text-align: right;
}
.warehouse-unit {
  font-size: 12px;
  color: #94a3b8;
  width: 16px;
}

/* ========== 排行 ========== */
.rankings-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.rank-card {
  flex: 1;
}
.rank-filter {
  display: flex;
  align-items: center;
}
.rank-category-select {
  font-size: 12px;
  padding: 4px 8px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: #fff;
  color: #475569;
  cursor: pointer;
  outline: none;
  max-width: 120px;
}
.rank-category-select:focus {
  border-color: #3b82f6;
}
.rank-list {
  padding: 4px 0;
}
.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 20px;
  border-bottom: 1px solid #f8fafc;
}
.rank-item:last-child {
  border-bottom: none;
}
.rank-num {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: #475569;
  flex-shrink: 0;
}
.medal-gold {
  background: #fde68a;
  color: #92400e;
}
.medal-silver {
  background: #d1d5db;
  color: #374151;
}
.medal-bronze {
  background: #fed7aa;
  color: #92400e;
}
.medal-normal {
  background: #f1f5f9;
  color: #64748b;
}
.rank-name {
  flex: 1;
  font-size: 13px;
  font-weight: 500;
  color: #334155;
}
.rank-qty {
  font-size: 18px;
  font-weight: 700;
  min-width: 40px;
  text-align: right;
}
.rank-unit {
  font-size: 12px;
  width: 16px;
}

/* ========== 员工设备分布 ========== */
.employees-section {
  margin-bottom: 24px;
}
.employee-group {
  margin-bottom: 20px;
}
.group-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding: 0 20px;
}
.group-label {
  font-size: 11px;
  font-weight: 700;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.group-divider {
  flex: 1;
  height: 1px;
  background: #e2e8f0;
}
.group-badge {
  font-size: 11px;
  color: #94a3b8;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 999px;
  font-weight: 500;
}
.employee-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  padding: 0 20px;
}
.emp-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s;
  width: 100%;
}
.emp-card:hover {
  border-color: #93c5fd;
  box-shadow: 0 4px 12px rgba(59,130,246,0.1);
}
/* 第一行：头像 + 姓名 + 箭头 */
.emp-row-1 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.emp-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
  font-size: 12px;
  flex-shrink: 0;
}
.emp-name-box {
  flex: 1;
  min-width: 0;
}
.emp-name {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.emp-arrow {
  color: #cbd5e1;
  flex-shrink: 0;
  transition: color 0.2s;
}
.emp-card:hover .emp-arrow {
  color: #60a5fa;
}
/* 第二行：设备总数 */
.emp-row-2 {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 6px;
}
.emp-phone-num {
  font-size: 24px;
  font-weight: 800;
  color: #1e293b;
  line-height: 1;
}
.emp-phone-label {
  font-size: 12px;
  color: #94a3b8;
}
/* 第三行：平台标签 */
.emp-row-3 {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.emp-tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 999px;
  font-weight: 700;
  color: #fff;
}
.emp-tag-more {
  font-size: 10px;
  color: #94a3b8;
}

/* ========== 最近操作日志 ========== */
.logs-section {
  margin-bottom: 24px;
}
.view-all-btn {
  background: none;
  border: none;
  color: #3b82f6;
  font-size: 13px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}
.view-all-btn:hover {
  background: #eff6ff;
}
.logs-list {
  padding: 4px 0;
}
.log-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 20px;
  border-bottom: 1px solid #f8fafc;
}
.log-item:last-child {
  border-bottom: none;
}
.log-level {
  font-size: 11px;
  font-weight: 700;
  color: #60a5fa;
  width: 32px;
  flex-shrink: 0;
}
.log-time {
  font-size: 12px;
  color: #cbd5e1;
  width: 128px;
  flex-shrink: 0;
}
.log-module {
  font-size: 12px;
  color: #94a3b8;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 999px;
  flex-shrink: 0;
}
.log-message {
  font-size: 13px;
  color: #475569;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.empty-tip {
  text-align: center;
  color: #94a3b8;
  padding: 30px;
  font-size: 13px;
}

/* ========== 员工详情弹窗 ========== */
.detail-header {
  display: flex;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border-radius: 12px;
  margin-bottom: 20px;
  color: #fff;
}
.detail-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 600;
  margin-right: 16px;
  flex-shrink: 0;
  background: rgba(255,255,255,0.25);
}
.detail-meta {
  flex: 1;
}
.detail-name {
  font-size: 20px;
  font-weight: 700;
}
.detail-dept {
  font-size: 13px;
  opacity: 0.85;
  margin-top: 4px;
}
.detail-phone-box {
  text-align: center;
  margin-left: 16px;
}
.detail-phone-num {
  font-size: 36px;
  font-weight: 800;
  line-height: 1;
}
.detail-phone-label {
  font-size: 12px;
  opacity: 0.8;
  margin-top: 4px;
}
.detail-section {
  margin-top: 20px;
}
.detail-section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #e2e8f0;
}
.detail-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 12px;
}
.detail-section-header .detail-section-title {
  margin: 0;
  padding-bottom: 0;
  border-bottom: none;
  white-space: nowrap;
  flex-shrink: 0;
}
.inout-count {
  font-size: 11px;
  color: #94a3b8;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 999px;
  font-weight: 500;
  white-space: nowrap;
}

/* ========== 进出库筛选栏 ========== */
.inout-filter-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  padding: 10px 0;
  border-bottom: 1px solid #f1f5f9;
  margin-bottom: 8px;
}
.filter-btn {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 999px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #64748b;
}
.filter-btn:hover {
  border-color: #3b82f6;
  color: #3b82f6;
}
.filter-btn.active {
  background: #3b82f6;
  color: #fff;
  border-color: #3b82f6;
}
.filter-apply-btn {
  background: #3b82f6;
  color: #fff;
  border-color: #3b82f6;
}
.filter-apply-btn:hover:not(:disabled) {
  background: #2563eb;
  border-color: #2563eb;
}
.filter-apply-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.filter-date-input {
  font-size: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 4px 8px;
  color: #475569;
  background: #fff;
  outline: none;
  cursor: pointer;
  width: 130px;
}
.filter-date-input:focus {
  border-color: #3b82f6;
}
.filter-date-sep {
  font-size: 12px;
  color: #94a3b8;
}
.platform-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
}

/* ========== 差异分析弹窗 ========== */
.diff-formula {
  background: #f8fafc;
  padding: 8px;
  border-radius: 4px;
  font-size: 13px;
  color: #374151;
}
.diff-num-card {
  background: #f8fafc;
  padding: 16px;
  border-radius: 8px;
  text-align: center;
  font-size: 13px;
  color: #64748b;
}
.diff-num {
  font-size: 28px;
  font-weight: 600;
  margin-top: 8px;
  color: #1e293b;
}
.text-green { color: #16a34a; }
.text-red { color: #dc2626; }
.diff-detail {
  font-size: 14px;
  line-height: 2;
  color: #475569;
}

/* ========== 响应式 ========== */
@media (max-width: 1200px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  .content-grid {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 768px) {
  .stat-cards {
    grid-template-columns: 1fr;
  }
  .employee-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  }
  .detail-section-header {
    flex-wrap: wrap;
  }
  .inout-filter-bar {
    padding: 8px 0;
  }
  .filter-date-input {
    width: 110px;
  }
}
</style>

<style>
/* 弹窗全局样式 */
.emp-detail-dialog {
  max-width: 720px;
}
.emp-detail-dialog .el-dialog__body {
  padding: 0 20px 20px;
}
.emp-detail-dialog .el-dialog__header {
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
}
/* 差异分析弹窗 - 桌面端限制最大宽度 */
.diff-dialog-wrapper {
  max-width: 500px;
}
.diff-dialog-wrapper .el-dialog__body {
  padding: 20px;
}
/* 移动端适配 */
@media (max-width: 768px) {
  .diff-dialog-wrapper,
  .emp-detail-dialog {
    width: 95% !important;
    max-width: none !important;
    margin-top: 3vh !important;
  }
  .diff-dialog-wrapper .el-dialog__body,
  .emp-detail-dialog .el-dialog__body {
    padding: 12px !important;
  }
  .diff-dialog .diff-formula {
    font-size: 12px;
    word-break: break-all;
  }
  .diff-dialog .diff-num-card div:last-child {
    font-size: 20px;
  }
  .emp-detail-dialog .el-dialog__header {
    padding: 12px !important;
  }
}
</style>
