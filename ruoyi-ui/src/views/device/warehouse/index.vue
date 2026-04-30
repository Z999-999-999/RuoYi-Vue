<template>
  <div class="warehouse-page">
    <!-- ========== 页面头部 ========== -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">库房库存</h1>
        <p class="page-date">{{ todayStr }} · 录入各分类当前库存数量</p>
      </div>
    </div>

    <!-- ========== 汇总卡片 ========== -->
    <div class="summary-card" :class="{ 'summary-warning': hasDiscrepancy }">
      <div class="summary-label">库房总库存（预览）</div>
      <div class="summary-value">{{ totalQty }} <span class="summary-unit">台</span></div>
      <div v-if="hasDiscrepancy && companyTotal > 0" class="summary-discrepancy">
        <i class="el-icon-warning"></i>
        合计 {{ actualTotal }} 台，设定 {{ companyTotal }} 台，{{ discrepancyDir }} {{ discrepancyDiff }} 台
      </div>
    </div>

    <!-- ========== 分类卡片网格 ========== -->
    <div v-if="stocks.length === 0" class="empty-state">
      <i class="el-icon-box empty-icon"></i>
      <p>请先在"系统设置 → 库房分类"中添加分类</p>
    </div>

    <div v-else class="stock-grid">
      <div
        v-for="stock in stocks"
        :key="stock.categoryId"
        class="stock-card"
        :class="{ 'modified': isModified(stock) }"
      >
        <div class="stock-header">
          <div class="color-dot" :style="{ background: stock.categoryColor || '#888' }"></div>
          <span class="category-name">{{ stock.categoryName }}</span>
          <el-tag v-if="isModified(stock)" size="mini" type="primary" class="modified-tag">已修改</el-tag>
        </div>

        <div class="stock-quantity">
          <label class="input-label">数量</label>
          <el-input-number
            v-model="editMap[stock.categoryId].quantity"
            :min="0"
            class="qty-input"
            controls-position="right"
          />
          <span class="qty-unit">台</span>
          <span v-if="isModified(stock)" class="original-hint">原: {{ stock.quantity }}</span>
        </div>

        <!-- 异常设备子类型 -->
        <div v-if="stock.categoryType === 'abnormal'" class="stock-subtype">
          <label class="input-label">子类型</label>
          <el-select
            v-model="editMap[stock.categoryId].subType"
            placeholder="不指定"
            size="small"
            clearable
            class="subtype-select"
          >
            <el-option label="待实名" value="pending_verify" />
            <el-option label="待解封" value="pending_unban" />
            <el-option label="其他" value="other" />
          </el-select>
        </div>

        <div class="stock-note">
          <label class="input-label">备注</label>
          <el-input
            v-model="editMap[stock.categoryId].note"
            placeholder="可选备注"
            size="small"
          />
        </div>

        <div v-if="!isModified(stock) && stock.updateTime" class="last-update">
          上次更新: {{ formatTime(stock.updateTime) }}
        </div>
      </div>
    </div>

    <!-- ========== 保存按钮 ========== -->
    <div class="save-section">
      <el-button
        type="primary"
        size="medium"
        :loading="saving"
        @click="saveBatch"
        class="save-btn"
      >
        <i class="el-icon-check"></i> 保存库存
      </el-button>
    </div>
  </div>
</template>

<script>
import { listStocks, updateStockBatch, getDashboard } from '@/api/device/index'

export default {
  name: 'DeviceWarehouse',
  data() {
    const today = new Date()
    const pad = n => String(n).padStart(2, '0')
    const todayStr = `${today.getFullYear()}-${pad(today.getMonth()+1)}-${pad(today.getDate())}`
    return {
      stocks: [],
      editMap: {},
      saving: false,
      colorMap: null,
      todayStr,
      companyTotal: 0,
      employeeTotal: 0,
      outsideTotal: 0
    }
  },
  computed: {
    totalQty() {
      return Object.values(this.editMap).reduce((s, v) => s + (parseInt(v.quantity) || 0), 0)
    },
    actualTotal() {
      return this.totalQty + this.employeeTotal + this.outsideTotal
    },
    hasDiscrepancy() {
      return this.companyTotal > 0 && this.actualTotal !== this.companyTotal
    },
    discrepancyDiff() {
      return Math.abs(this.actualTotal - this.companyTotal)
    },
    discrepancyDir() {
      return this.actualTotal > this.companyTotal ? '多' : '少'
    }
  },
  created() {
    this.loadStocks()
    this.loadDashTotals()
  },
  methods: {
    async loadStocks() {
      const res = await listStocks()
      this.stocks = res.data || []
      this.editMap = {}
      this.stocks.forEach(s => {
        this.$set(this.editMap, s.categoryId, {
          quantity: s.quantity,
          subType: s.subType || '',
          note: s.note || ''
        })
      })
    },
    isModified(stock) {
      const e = this.editMap[stock.categoryId]
      if (!e) return false
      return parseInt(e.quantity) !== stock.quantity || e.note !== (stock.note || '')
    },
    async saveBatch() {
      const newTotal = this.totalQty + this.employeeTotal + this.outsideTotal
      if (this.companyTotal > 0 && newTotal !== this.companyTotal) {
        const diff = Math.abs(newTotal - this.companyTotal)
        const direction = newTotal > this.companyTotal ? '多' : '少'
        try {
          await this.$confirm(
            `保存后设备总数：${newTotal}\n` +
            `设定总数：${this.companyTotal}\n` +
            `${direction} ${diff} 台，是否强制保存？`,
            '数量差异警示',
            { type: 'warning', confirmButtonText: '强制保存', cancelButtonText: '取消' }
          )
        } catch(e) {
          return // 用户取消
        }
      }
      this.saving = true
      try {
        const items = this.stocks.map(s => ({
          categoryId: s.categoryId,
          quantity: parseInt(this.editMap[s.categoryId].quantity) || 0,
          subType: this.editMap[s.categoryId].subType,
          note: this.editMap[s.categoryId].note
        }))
        await updateStockBatch(items)
        this.$message.success('库存保存成功')
        await this.loadStocks()
        await this.loadDashTotals()
      } catch(e) {
        this.$message.error('保存失败：' + (e.message || ''))
      } finally {
        this.saving = false
      }
    },
    formatTime(val) {
      if (!val) return '-'
      return val.substring(0, 16)
    },
    async loadDashTotals() {
      try {
        const res = await getDashboard()
        const d = res.data || {}
        this.companyTotal = d.companyTotal || 0
        this.employeeTotal = d.employeeTotal || 0
        this.outsideTotal = d.outsideTotal || 0
      } catch(e) {
        console.warn('获取总览数据失败', e)
      }
    }
  }
}
</script>

<style scoped>
.warehouse-page {
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

/* ========== 汇总卡片 ========== */
.summary-card {
  background: #fff;
  border-radius: 16px;
  border: 2px solid #e2e8f0;
  padding: 20px;
  margin-bottom: 24px;
}
.summary-card.summary-warning {
  border-color: #fca5a5;
  background: #fef2f2;
}
.summary-label {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 8px;
}
.summary-value {
  font-size: 36px;
  font-weight: 800;
  color: #1e293b;
  line-height: 1.1;
}
.summary-unit {
  font-size: 16px;
  font-weight: 400;
  color: #94a3b8;
}
.summary-discrepancy {
  margin-top: 8px;
  font-size: 13px;
  color: #dc2626;
  font-weight: 500;
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
  display: block;
}

/* ========== 库存卡片网格 ========== */
.stock-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}
.stock-card {
  background: #fff;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  padding: 16px;
  transition: all 0.2s;
}
.stock-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}
.stock-card.modified {
  border-color: #3b82f6;
  box-shadow: 0 4px 12px rgba(59,130,246,0.1);
}

.stock-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}
.category-name {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  flex: 1;
}
.modified-tag {
  flex-shrink: 0;
}

.stock-quantity {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.input-label {
  font-size: 12px;
  color: #94a3b8;
  width: 42px;
  flex-shrink: 0;
}
.qty-input {
  width: 120px;
}
.qty-unit {
  font-size: 13px;
  color: #94a3b8;
}
.original-hint {
  font-size: 11px;
  color: #94a3b8;
}

.stock-subtype {
  margin-bottom: 12px;
}
.subtype-select {
  width: 100%;
}

.stock-note {
  margin-bottom: 8px;
}

.last-update {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f1f5f9;
}

/* ========== 保存按钮区域 ========== */
.save-section {
  display: flex;
  justify-content: flex-end;
}
.save-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 24px;
  font-size: 14px;
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .stock-grid {
    grid-template-columns: 1fr;
  }
}
</style>
