<template>
  <div class="stats-page">
    <!-- ========== 页面头部 ========== -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">统计榜单</h1>
        <p class="page-date">按日期区间、原因分类查看进出库榜单</p>
      </div>
      <div v-if="totalRecords > 2000" class="overflow-hint">
        共 {{ totalRecords }} 条，仅显示前 2000 条
      </div>
    </div>

    <!-- ========== 筛选栏 ========== -->
    <div class="filter-card">
      <div class="filter-row">
        <div class="filter-item">
          <label class="filter-label">开始日期</label>
          <el-date-picker
            v-model="startDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="开始日期"
            size="small"
            class="filter-input"
          />
        </div>
        <div class="filter-item">
          <label class="filter-label">结束日期</label>
          <el-date-picker
            v-model="endDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="结束日期"
            size="small"
            class="filter-input"
          />
        </div>
        <div class="filter-item">
          <label class="filter-label">原因筛选</label>
          <el-select
            v-model="filterCategoryId"
            placeholder="请选择"
            size="small"
            class="filter-input"
          >
            <el-option label="全部原因" :value="null" />
            <el-option
              v-for="c in allCategories"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </el-select>
        </div>
        <div class="filter-buttons">
          <el-button size="small" class="quick-btn" @click="setThisMonth">
            <i class="el-icon-date"></i> 本月
          </el-button>
          <el-button size="small" class="quick-btn" @click="setLast7Days">
            <i class="el-icon-time"></i> 近7天
          </el-button>
          <el-button size="small" type="primary" class="stat-btn" @click="loadStats" :loading="loading">
            <i class="el-icon-refresh"></i> 刷新
          </el-button>
        </div>
      </div>

      <!-- 汇总统计 -->
      <div v-if="!loading" class="summary-row">
        <span class="summary-item">
          共 <b>{{ filteredRecords.length }}</b> 条记录
        </span>
        <span class="summary-item text-green">
          入库 <b>{{ totalInQty }}</b> 台
        </span>
        <span class="summary-item text-red">
          出库 <b>{{ totalOutQty }}</b> 台
        </span>
        <span class="summary-item">
          净 <b :class="netQty >= 0 ? 'text-green' : 'text-red'">{{ netQty >= 0 ? '+' : '' }}{{ netQty }}</b> 台
        </span>
      </div>
      <div v-else class="summary-row">
        <span class="loading-text">
          <i class="el-icon-loading"></i> 加载中...
        </span>
      </div>
    </div>

    <!-- ========== 榜单 Tabs ========== -->
    <div class="tabs-wrapper">
      <el-radio-group v-model="activeTab" size="medium" class="tab-group">
        <el-radio-button label="employee">
          <i class="el-icon-trophy"></i> 员工榜单
        </el-radio-button>
        <el-radio-button label="category">
          <i class="el-icon-data-board"></i> 原因榜单
        </el-radio-button>
      </el-radio-group>
    </div>

    <!-- ========== 员工榜单 ========== -->
    <div v-if="activeTab === 'employee'" class="rank-card">
      <div class="rank-header">
        <div class="rank-header-left">
          <i class="el-icon-trophy"></i>
          <span class="font-semibold">员工进出库排行</span>
          <span class="rank-hint">点击展开原因明细</span>
        </div>
        <span class="rank-total">共 {{ employeeRanks.length }} 名员工</span>
      </div>

      <div v-if="loading" class="loading-state">
        <i class="el-icon-loading"></i>
        <span>加载中...</span>
      </div>

      <div v-else-if="employeeRanks.length === 0" class="empty-state">
        <i class="el-icon-data-analysis empty-icon"></i>
        <p>当前时间段暂无数据</p>
      </div>

      <div v-else class="rank-list">
        <div
          v-for="(item, idx) in employeeRanks"
          :key="item.employeeId"
        >
          <!-- 员工主行 - 可点击展开 -->
          <div
            class="rank-item clickable"
            @click="toggleDetail('emp', item.employeeId)"
          >
            <div class="rank-num" :class="getMedalClass(idx)">
              {{ idx + 1 }}
            </div>
            <div class="rank-info">
              <div class="rank-main">
                <span class="rank-name">{{ item.employeeName }}</span>
                <span v-if="item.department" class="rank-dept">{{ item.department }}</span>
              </div>
              <!-- 收起时：原因标签预览 -->
              <div v-if="!isExpanded('emp', item.employeeId) && item.details.length > 0" class="preview-tags">
                <span
                  v-for="d in item.details.slice(0, 4)"
                  :key="d.categoryId"
                  class="preview-tag"
                  :class="getCategoryTypeClass(d.categoryType)"
                >
                  {{ d.categoryName }} ×{{ d.qty }}
                </span>
                <span v-if="item.details.length > 4" class="preview-more">+{{ item.details.length - 4 }}个原因</span>
              </div>
            </div>
            <span class="stat-badge stat-in">入 {{ item.inQty }}</span>
            <span class="stat-badge stat-out">出 {{ item.outQty }}</span>
            <div class="rank-total">
              <span class="total-num">{{ item.totalQty }}</span>
              <span class="total-unit">台</span>
            </div>
            <i :class="isExpanded('emp', item.employeeId) ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" class="expand-icon"></i>
          </div>

          <!-- 展开：原因明细（带进度条） -->
          <div v-if="isExpanded('emp', item.employeeId)" class="detail-section">
            <div class="detail-header">原因明细（按数量排序）</div>
            <div class="detail-rows">
              <div v-for="(d, dIdx) in item.details" :key="d.categoryId" class="detail-row">
                <span class="detail-idx">{{ dIdx + 1 }}</span>
                <span class="detail-type-badge" :class="getCategoryTypeClass(d.categoryType)">
                  {{ getCategoryTypeLabel(d.categoryType) }}
                </span>
                <span class="detail-name">{{ d.categoryName }}</span>
                <span class="detail-count">{{ d.count }} 次</span>
                <div class="detail-progress">
                  <div
                    class="detail-progress-bar"
                    :class="d.categoryType === 'in' ? 'bar-green' : 'bar-red'"
                    :style="{ width: getPercentage(d.qty, item.totalQty) + '%' }"
                  ></div>
                </div>
                <span class="detail-qty">{{ d.qty }} 台</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 原因榜单 ========== -->
    <div v-if="activeTab === 'category'" class="rank-card">
      <div class="rank-header">
        <div class="rank-header-left">
          <i class="el-icon-data-board"></i>
          <span class="font-semibold">原因分类排行</span>
          <span class="rank-hint">点击展开员工明细</span>
        </div>
        <span class="rank-total">共 {{ categoryRanks.length }} 个原因</span>
      </div>

      <div v-if="loading" class="loading-state">
        <i class="el-icon-loading"></i>
        <span>加载中...</span>
      </div>

      <div v-else-if="categoryRanks.length === 0" class="empty-state">
        <i class="el-icon-data-analysis empty-icon"></i>
        <p>当前时间段暂无数据</p>
      </div>

      <div v-else class="rank-list">
        <div
          v-for="(item, idx) in categoryRanks"
          :key="item.categoryId"
        >
          <!-- 原因主行 - 可点击展开 -->
          <div
            class="rank-item clickable"
            @click="toggleDetail('cat', item.categoryId)"
          >
            <div class="rank-num" :class="getMedalClass(idx)">
              {{ idx + 1 }}
            </div>
            <span class="type-badge" :class="getCategoryTypeClass(item.categoryType)">
              {{ getCategoryTypeLabel(item.categoryType) }}
            </span>
            <div class="rank-info">
              <div class="rank-main">
                <span class="rank-name">{{ item.categoryName }}</span>
              </div>
              <!-- 收起时：员工标签预览 -->
              <div v-if="!isExpanded('cat', item.categoryId) && item.details.length > 0" class="preview-tags">
                <span
                  v-for="d in item.details.slice(0, 4)"
                  :key="d.employeeName"
                  class="preview-tag preview-tag-blue"
                >
                  {{ d.employeeName }} ×{{ d.qty }}
                </span>
                <span v-if="item.details.length > 4" class="preview-more">+{{ item.details.length - 4 }}人</span>
              </div>
            </div>
            <span class="detail-count-inline">{{ item.totalCount }} 次</span>
            <div class="rank-total">
              <span class="total-num">{{ item.totalQty }}</span>
              <span class="total-unit">台</span>
            </div>
            <i :class="isExpanded('cat', item.categoryId) ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" class="expand-icon"></i>
          </div>

          <!-- 展开：员工明细（带进度条） -->
          <div v-if="isExpanded('cat', item.categoryId)" class="detail-section">
            <div class="detail-header">涉及员工（按数量排序）</div>
            <div class="detail-rows">
              <div v-for="(d, dIdx) in item.details" :key="d.employeeName" class="detail-row">
                <span class="detail-idx">{{ dIdx + 1 }}</span>
                <span class="detail-name detail-name-bold">{{ d.employeeName }}</span>
                <span v-if="d.department" class="detail-dept">{{ d.department }}</span>
                <span class="detail-count">{{ d.count }} 次</span>
                <div class="detail-progress">
                  <div
                    class="detail-progress-bar"
                    :class="item.categoryType === 'in' ? 'bar-green' : 'bar-blue'"
                    :style="{ width: getPercentage(d.qty, item.totalQty) + '%' }"
                  ></div>
                </div>
                <span class="detail-qty">{{ d.qty }} 台</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { listInoutRecords, getBaseData } from '@/api/device/index'

export default {
  name: 'DeviceStats',
  data() {
    const now = new Date()
    const y = now.getFullYear()
    const m = String(now.getMonth() + 1).padStart(2, '0')
    const lastDay = new Date(y, now.getMonth() + 1, 0).getDate()
    return {
      activeTab: 'employee',
      startDate: `${y}-${m}-01`,
      endDate: `${y}-${m}-${lastDay}`,
      loading: false,
      allRecords: [],       // 原始记录
      allCategories: [],    // 原因分类（含 type 字段）
      totalRecords: 0,
      filterCategoryId: null,
      expandedKeys: {}      // { 'emp_123': true, 'cat_5': true }
    }
  },
  computed: {
    // 按原因分类过滤后的记录
    filteredRecords() {
      if (!this.filterCategoryId) return this.allRecords
      return this.allRecords.filter(r => r.inoutCategoryId === this.filterCategoryId)
    },
    totalInQty() {
      return this.filteredRecords.filter(r => r.type === 'in').reduce((s, r) => s + r.quantity, 0)
    },
    totalOutQty() {
      return this.filteredRecords.filter(r => r.type === 'out').reduce((s, r) => s + r.quantity, 0)
    },
    netQty() {
      return this.totalInQty - this.totalOutQty
    },
    // 员工榜单
    employeeRanks() {
      const map = {}
      this.filteredRecords.forEach(r => {
        const empId = r.type === 'out' ? r.receiverEmployeeId : r.senderEmployeeId
        const empName = r.type === 'out' ? r.receiverEmployeeName : r.senderEmployeeName
        if (!empId || !empName) return
        if (!map[empId]) {
          map[empId] = {
            employeeId: empId,
            employeeName: empName,
            department: '',
            inQty: 0,
            outQty: 0,
            totalQty: 0,
            detailMap: {}
          }
        }
        const qty = r.quantity
        if (r.type === 'in') map[empId].inQty += qty
        else map[empId].outQty += qty
        map[empId].totalQty += qty

        const catId = r.inoutCategoryId
        const catName = r.inoutCategoryName || '未知'
        const catType = this.getCategoryType(catId)
        const key = catId || catName
        if (!map[empId].detailMap[key]) {
          map[empId].detailMap[key] = { categoryId: catId, categoryName: catName, categoryType: catType, qty: 0, count: 0 }
        }
        map[empId].detailMap[key].qty += qty
        map[empId].detailMap[key].count++
      })

      return Object.values(map)
        .sort((a, b) => b.totalQty - a.totalQty)
        .map(item => ({
          ...item,
          details: Object.values(item.detailMap).sort((a, b) => b.qty - a.qty)
        }))
    },
    // 原因榜单
    categoryRanks() {
      const map = {}
      this.filteredRecords.forEach(r => {
        const catId = r.inoutCategoryId
        const catName = r.inoutCategoryName || '未知'
        if (!catId) return
        if (!map[catId]) {
          map[catId] = {
            categoryId: catId,
            categoryName: catName,
            categoryType: this.getCategoryType(catId),
            totalQty: 0,
            totalCount: 0,
            detailMap: {}
          }
        }
        map[catId].totalQty += r.quantity
        map[catId].totalCount++

        const empName = r.type === 'out' ? r.receiverEmployeeName : r.senderEmployeeName
        if (empName) {
          if (!map[catId].detailMap[empName]) {
            map[catId].detailMap[empName] = { employeeName: empName, department: '', qty: 0, count: 0 }
          }
          map[catId].detailMap[empName].qty += r.quantity
          map[catId].detailMap[empName].count++
        }
      })

      return Object.values(map)
        .sort((a, b) => b.totalQty - a.totalQty)
        .map(item => ({
          ...item,
          details: Object.values(item.detailMap).sort((a, b) => b.qty - a.qty)
        }))
    }
  },
  created() {
    this.loadBaseData()
    this.loadStats()
  },
  methods: {
    async loadBaseData() {
      const res = await getBaseData()
      this.allCategories = res.data.inoutCategories || []
    },
    getCategoryType(catId) {
      if (!catId) return 'both'
      const cat = this.allCategories.find(c => c.id === catId)
      return cat ? (cat.type || 'both') : 'both'
    },
    getCategoryTypeClass(type) {
      if (type === 'in') return 'type-in'
      if (type === 'out') return 'type-out'
      return 'type-both'
    },
    getCategoryTypeLabel(type) {
      if (type === 'in') return '入库'
      if (type === 'out') return '出库'
      return '通用'
    },
    setThisMonth() {
      const now = new Date()
      const y = now.getFullYear()
      const m = String(now.getMonth() + 1).padStart(2, '0')
      const lastDay = new Date(y, now.getMonth() + 1, 0).getDate()
      this.startDate = `${y}-${m}-01`
      this.endDate = `${y}-${m}-${lastDay}`
    },
    setLast7Days() {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 6)
      const fmt = d => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
      this.startDate = fmt(start)
      this.endDate = fmt(end)
    },
    async loadStats() {
      this.loading = true
      try {
        const params = {
          pageNum: 1,
          pageSize: 2000,
          startDate: this.startDate,
          endDate: this.endDate
        }
        if (this.filterCategoryId) {
          params.inoutCategoryId = this.filterCategoryId
        }
        const res = await listInoutRecords(params)
        this.allRecords = res.rows || []
        this.totalRecords = res.total || 0
      } finally {
        this.loading = false
      }
    },
    isExpanded(prefix, id) {
      return !!this.expandedKeys[`${prefix}_${id}`]
    },
    toggleDetail(prefix, id) {
      const key = `${prefix}_${id}`
      this.$set(this.expandedKeys, key, !this.expandedKeys[key])
    },
    getPercentage(value, total) {
      if (!total) return 0
      return Math.round(value / total * 100)
    },
    getMedalClass(idx) {
      if (idx === 0) return 'medal-gold'
      if (idx === 1) return 'medal-silver'
      if (idx === 2) return 'medal-bronze'
      return 'medal-normal'
    }
  }
}
</script>

<style scoped>
.stats-page {
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
.overflow-hint {
  font-size: 12px;
  color: #d97706;
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 8px;
  padding: 6px 12px;
  white-space: nowrap;
}

/* ========== 筛选栏 ========== */
.filter-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  padding: 20px;
  margin-bottom: 20px;
}
.filter-row {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 16px;
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
.filter-input {
  width: 160px;
}
.filter-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
  padding-bottom: 1px;
}
.quick-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}
.stat-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}
.summary-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
  font-size: 13px;
}
.summary-item {
  color: #64748b;
}
.summary-item b {
  font-weight: 600;
}
.text-green { color: #16a34a; }
.text-red { color: #dc2626; }
.loading-text {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #94a3b8;
}

/* ========== Tabs ========== */
.tabs-wrapper {
  margin-bottom: 20px;
}
.tab-group {
  display: flex;
  gap: 8px;
}

/* ========== 榜单卡片 ========== */
.rank-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
}
.rank-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
}
.rank-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #334155;
}
.rank-hint {
  font-size: 11px;
  color: #94a3b8;
  font-weight: 400;
}
.rank-total {
  font-size: 12px;
  color: #94a3b8;
  background: #f1f5f9;
  padding: 4px 8px;
  border-radius: 999px;
  font-weight: 500;
}

/* ========== 排行列表 ========== */
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 48px;
  color: #94a3b8;
}
.empty-state {
  text-align: center;
  color: #94a3b8;
  padding: 48px;
}
.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.4;
  display: block;
}
.rank-list {
  padding: 4px 0;
}
.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  border-bottom: 1px solid #f8fafc;
  position: relative;
}
.rank-item.clickable {
  cursor: pointer;
  transition: background 0.15s;
}
.rank-item.clickable:hover {
  background: #f8fafc;
}
.rank-num {
  width: 28px;
  height: 28px;
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
.rank-info {
  flex: 1;
  min-width: 0;
}
.rank-main {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}
.rank-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}
.rank-dept {
  font-size: 11px;
  color: #94a3b8;
  background: #f1f5f9;
  padding: 1px 6px;
  border-radius: 4px;
}

/* ========== 类型标签 ========== */
.type-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
  flex-shrink: 0;
}
.type-in {
  background: #dcfce7;
  color: #15803d;
}
.type-out {
  background: #fee2e2;
  color: #b91c1c;
}
.type-both {
  background: #f1f5f9;
  color: #475569;
}

/* ========== 统计徽标 ========== */
.stat-badge {
  font-size: 12px;
  font-weight: 500;
  flex-shrink: 0;
}
.stat-badge.stat-in { color: #16a34a; }
.stat-badge.stat-out { color: #dc2626; }
.detail-count-inline {
  font-size: 12px;
  color: #94a3b8;
  flex-shrink: 0;
}

.rank-total {
  text-align: right;
  flex-shrink: 0;
}
.total-num {
  font-size: 20px;
  font-weight: 800;
  color: #1e293b;
  line-height: 1;
}
.total-unit {
  font-size: 12px;
  color: #94a3b8;
}
.expand-icon {
  color: #94a3b8;
  font-size: 12px;
  flex-shrink: 0;
}

/* ========== 收起时预览标签 ========== */
.preview-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 4px;
}
.preview-tag {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 600;
}
.preview-tag.type-in {
  background: #dcfce7;
  color: #15803d;
}
.preview-tag.type-out {
  background: #fee2e2;
  color: #b91c1c;
}
.preview-tag.type-both {
  background: #f1f5f9;
  color: #475569;
}
.preview-tag-blue {
  background: #dbeafe;
  color: #1d4ed8;
}
.preview-more {
  font-size: 11px;
  color: #94a3b8;
}

/* ========== 详情区域（展开） ========== */
.detail-section {
  width: 100%;
  background: #f8fafc;
  border-top: 1px solid #f1f5f9;
  padding: 12px 20px 12px 60px;
}
.detail-header {
  font-size: 11px;
  color: #94a3b8;
  margin-bottom: 10px;
  font-weight: 500;
}
.detail-rows {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.detail-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.detail-idx {
  font-size: 12px;
  color: #94a3b8;
  width: 16px;
  text-align: right;
  flex-shrink: 0;
}
.detail-type-badge {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 600;
  flex-shrink: 0;
}
.detail-name {
  flex: 1;
  font-size: 13px;
  color: #475569;
  min-width: 0;
}
.detail-name-bold {
  font-weight: 600;
  color: #334155;
}
.detail-dept {
  font-size: 11px;
  color: #94a3b8;
  background: #e2e8f0;
  padding: 1px 6px;
  border-radius: 4px;
  flex-shrink: 0;
}
.detail-count {
  font-size: 12px;
  color: #94a3b8;
  flex-shrink: 0;
}
.detail-progress {
  width: 96px;
  height: 6px;
  background: #e2e8f0;
  border-radius: 3px;
  overflow: hidden;
  flex-shrink: 0;
}
.detail-progress-bar {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s ease;
}
.bar-green {
  background: #4ade80;
}
.bar-red {
  background: #f87171;
}
.bar-blue {
  background: #60a5fa;
}
.detail-qty {
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
  width: 40px;
  text-align: right;
  flex-shrink: 0;
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }
  .filter-item {
    width: 100%;
  }
  .filter-input {
    width: 100%;
  }
  .filter-buttons {
    margin-left: 0;
    justify-content: flex-start;
  }
  .detail-progress {
    width: 60px;
  }
}
</style>
