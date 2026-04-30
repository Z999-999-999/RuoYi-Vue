<template>
  <div class="inout-page">
    <!-- ========== 页面头部 ========== -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">进出库管理</h1>
        <p class="page-date">登记设备出库与入库记录</p>
      </div>
      <div class="header-right">
        <el-button type="primary" size="medium" @click="openDialog('out')" class="header-btn btn-out">
          <i class="el-icon-download"></i> 出库登记
        </el-button>
        <el-button type="success" size="medium" @click="openDialog('in')" class="header-btn btn-in">
          <i class="el-icon-upload2"></i> 入库登记
        </el-button>
      </div>
    </div>

    <!-- ========== 新增进出库对话框 ========== -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="showDialog"
      :close-on-click-modal="false"
      :destroy-on-close="true"
      @close="resetForm"
      class="inout-dialog"
    >
      <el-form :model="form" size="small" label-width="90px" class="dialog-form">
        <!-- 第一步：选择原因 -->
        <div class="step-section">
          <div class="step-title"><span class="step-num">1</span> 选择{{ form.type === 'out' ? '出库' : '入库' }}原因</div>
          <el-form-item label="原因" prop="inoutCategoryId">
            <el-select
              v-model="form.inoutCategoryId"
              filterable
              placeholder="请选择原因"
              class="full-width"
            >
              <el-option
                v-for="c in allCategories"
                :key="c.id"
                :label="c.name"
                :value="c.id"
              />
            </el-select>
          </el-form-item>
        </div>

        <!-- 第二步：填写数量 -->
        <div class="step-section">
          <div class="step-title"><span class="step-num">2</span> 填写数量</div>
          <el-form-item label="数量" prop="quantity">
            <el-input-number
              v-model="form.quantity"
              :min="1"
              :max="form.type === 'out' && form.warehouseCategoryId ? getStockQty(form.warehouseCategoryId) : 999999"
              class="qty-input"
              controls-position="right"
            />
            <span class="qty-unit">台</span>
            <span v-if="form.type === 'out' && form.warehouseCategoryId" class="stock-hint">
              (当前库存: {{ getStockQty(form.warehouseCategoryId) }} 台)
            </span>
          </el-form-item>
        </div>

        <!-- 第三步：选择库房分类 -->
        <div class="step-section">
          <div class="step-title"><span class="step-num">3</span> 选择库房分类</div>
          <el-form-item label="库房分类" prop="warehouseCategoryId">
            <el-select
              v-model="form.warehouseCategoryId"
              filterable
              placeholder="请选择库房分类"
              class="full-width"
            >
              <el-option
                v-for="c in warehouseCategories"
                :key="c.id"
                :label="`${c.name}（库存 ${getStockQty(c.id)} 台）`"
                :value="c.id"
              />
            </el-select>
          </el-form-item>
        </div>

        <!-- 第四步：选择接收方/来源 -->
        <div class="step-section">
          <div class="step-title"><span class="step-num">4</span> {{ form.type === 'out' ? '选择接收方' : '选择来源' }}</div>
          
          <!-- 出库：接收方 -->
          <div v-if="form.type === 'out'">
            <el-radio-group v-model="form.receiverType" size="small" class="radio-group">
              <el-radio-button label="employee">员工</el-radio-button>
              <el-radio-button label="outside">在外设备</el-radio-button>
            </el-radio-group>
            <div style="margin-top: 12px;">
              <el-select
                v-if="form.receiverType === 'employee'"
                v-model="form.receiverEmployeeId"
                filterable
                placeholder="选择员工"
                class="full-width"
              >
                <el-option
                  v-for="e in employees"
                  :key="e.id"
                  :label="`${e.name}（持有 ${e.phoneTotal || 0} 台）`"
                  :value="e.id"
                />
              </el-select>
              <el-input
                v-else
                v-model="form.receiverOutsideNote"
                placeholder="请输入在外原因说明"
                class="full-width"
              />
            </div>
          </div>

          <!-- 入库：来源 -->
          <div v-if="form.type === 'in'">
            <el-radio-group v-model="form.senderType" size="small" class="radio-group">
              <el-radio-button label="employee">员工归还</el-radio-button>
              <el-radio-button label="warehouse">直接入库</el-radio-button>
            </el-radio-group>
            <div v-if="form.senderType === 'employee'" style="margin-top: 12px;">
              <el-select
                v-model="form.senderEmployeeId"
                filterable
                placeholder="选择员工"
                class="full-width"
              >
                <el-option
                  v-for="e in employees"
                  :key="e.id"
                  :label="`${e.name}（持有 ${e.phoneTotal || 0} 台）`"
                  :value="e.id"
                />
              </el-select>
            </div>
          </div>
        </div>

        <!-- 备注 -->
        <div class="step-section">
          <div class="step-title"><span class="step-num">5</span> 备注（可选）</div>
          <el-form-item label="备注">
            <el-input v-model="form.note" placeholder="可选备注" />
          </el-form-item>
        </div>
      </el-form>

      <span slot="footer" class="dialog-footer">
        <el-button @click="showDialog = false">取 消</el-button>
        <el-button
          :type="form.type === 'out' ? 'danger' : 'success'"
          @click="submitRecord"
          :loading="submitting"
        >
          <i class="el-icon-check"></i> 确认{{ form.type === 'out' ? '出库' : '入库' }}
        </el-button>
      </span>
    </el-dialog>

    <!-- ========== 库存不足警告弹窗 ========== -->
    <el-dialog
      title="库存不足，无法出库"
      :visible.sync="showStockShortage"
      :close-on-click-modal="false"
      class="shortage-dialog-wrapper"
      @close="handleCloseShortage"
    >
      <div class="shortage-dialog">
        <div class="dialog-icon dialog-icon-red">
          <i class="el-icon-warning-outline"></i>
        </div>
        <p class="dialog-msg">
          库房分类 <b>{{ shortageInfo.catName }}</b> 当前库存不足，出库数量超过可用库存。
        </p>
        <div class="dialog-stats">
          <div class="dialog-stat-item stat-available">
            <div class="stat-label">当前库存</div>
            <div class="stat-value">{{ shortageInfo.available }}</div>
          </div>
          <div class="dialog-stat-item stat-required">
            <div class="stat-label">本次出库</div>
            <div class="stat-value">{{ shortageInfo.quantity }}</div>
          </div>
        </div>
        <div class="dialog-warn">
          缺少 <b>{{ shortageInfo.quantity - shortageInfo.available }}</b> 台。请先补充库存或减少出库数量。
        </div>
      </div>
      <span slot="footer">
        <el-button @click="showStockShortage = false">知道了，去修改</el-button>
      </span>
    </el-dialog>

    <!-- ========== 筛选栏 ========== -->
    <div class="filter-card">
      <div class="filter-header">
        <i class="el-icon-search"></i>
        <span>筛选条件</span>
      </div>
      <el-row type="flex" align="middle" :gutter="12">
        <el-col :span="6">
          <el-select
            v-model="filter.type"
            placeholder="全部类型"
            clearable
            size="small"
            @change="onFilterTypeChange"
            class="full-width"
          >
            <el-option label="出库" value="out" />
            <el-option label="入库" value="in" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select
            v-model="filter.inoutCategoryId"
            placeholder="全部原因"
            filterable
            clearable
            size="small"
            @change="loadRecords"
            class="full-width"
          >
            <el-option
              v-for="c in filteredCategories"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button size="small" @click="resetFilter" class="full-width">重置</el-button>
        </el-col>
        <el-col :span="8" style="text-align: right;">
          <span class="total-hint">共 {{ total }} 条记录</span>
        </el-col>
      </el-row>
    </div>

    <!-- ========== 记录列表 ========== -->
    <div class="table-card">
      <el-table
        :data="records"
        stripe
        class="record-table"
        empty-text="暂无记录"
      >
        <el-table-column label="类型" width="80" align="center">
          <template v-slot="{ row }">
            <el-tag
              :type="row.type === 'out' ? 'danger' : 'success'"
              size="mini"
              class="type-tag"
            >
              {{ row.type === 'out' ? '出库' : '入库' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="70" align="center" prop="quantity" />
        <el-table-column label="原因" width="120" prop="inoutCategoryName" show-overflow-tooltip />
        <el-table-column label="库房分类" width="100" prop="warehouseCategoryName" show-overflow-tooltip />
        <el-table-column label="流转方向">
          <template v-slot="{ row }">
            <span v-if="row.type === 'out' && row.receiverType === 'employee'">
              <i class="el-icon-right"></i> 员工：{{ row.receiverEmployeeName }}
            </span>
            <span v-else-if="row.type === 'out' && row.receiverType === 'outside'">
              <i class="el-icon-right"></i> 在外：{{ row.receiverOutsideNote }}
            </span>
            <span v-else-if="row.type === 'in' && row.senderType === 'employee'">
              <i class="el-icon-back"></i> 员工：{{ row.senderEmployeeName }}
            </span>
            <span v-else>
              <i class="el-icon-download"></i> 直接入库
            </span>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="note" show-overflow-tooltip />
        <el-table-column label="时间" width="150">
          <template v-slot="{ row }">{{ formatDateTime(row.recordTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right" align="center">
          <template v-slot="{ row }">
            <el-button
              type="text"
              size="mini"
              @click="deleteRecord(row.id)"
              class="delete-btn"
            >删除</el-button>
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
  </div>
</template>

<script>
import { getBaseData, listInoutRecords, addInoutRecord, deleteInoutRecord } from '@/api/device/index'

export default {
  name: 'DeviceInout',
  data() {
    const now = new Date()
    const pad = n => String(n).padStart(2,'0')
    const nowStr = `${now.getFullYear()}-${pad(now.getMonth()+1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:00`
    return {
      employees: [],
      warehouseCategories: [],
      allCategories: [],
      filterCategories: [],
      records: [],
      total: 0,
      page: 1,
      pageSize: 20,
      submitting: false,
      showDialog: false,
      dialogTitle: '',
      showStockShortage: false,
      shortageInfo: { quantity: 0, available: 0, catName: '' },
      form: {
        recordTime: nowStr,
        type: 'out',
        quantity: 1,
        inoutCategoryId: null,
        warehouseCategoryId: null,
        receiverType: 'employee',
        receiverEmployeeId: null,
        receiverOutsideNote: '',
        senderType: 'employee',
        senderEmployeeId: null,
        note: ''
      },
      filter: {
        type: '',
        inoutCategoryId: null
      }
    }
  },
  computed: {
    filteredCategories() {
      if (!this.filter.type) return this.filterCategories
      return this.filterCategories.filter(c => c.type === this.filter.type)
    }
  },
  watch: {
    showDialog(val) {
      if (val) this.clearDialogMargin()
    },
    showStockShortage(val) {
      if (val) this.clearDialogMargin()
    }
  },
  created() {
    this.injectDialogCSS()
    this.init()
  },
  beforeDestroy() {
    this.removeDialogCSS()
  },
  methods: {
    async init() {
      try {
        const res = await getBaseData()
        this.employees = res.data.employees || []
        this.warehouseCategories = res.data.warehouseCategories || []
        this.filterCategories = res.data.inoutCategories || []
      } catch (e) {
        console.error('加载基础数据失败', e)
        this.$message.error('加载基础数据失败')
        return
      }      
      
      await this.loadRecords()
    },
    openDialog(type) {
      this.resetForm()
      this.form.type = type
      this.dialogTitle = type === 'out' ? '出库登记' : '入库登记'
      // 重置库存不足弹窗状态，防止残留
      this.showStockShortage = false
      this.shortageInfo = { quantity: 0, available: 0, catName: '' }
      // 加载对应类型的原因列表
      this.loadInoutCategories()
      this.showDialog = true
      // 等 DOM 更新后，清除 Element UI 动态写入的 margin-top 内联样式（移动端适配）
      this.$nextTick(() => {
        const dialogs = document.querySelectorAll('.inout-dialog .el-dialog, .shortage-dialog-wrapper .el-dialog')
        dialogs.forEach(d => {
          d.style.marginTop = ''
          d.style.marginBottom = ''
        })
      })
    },
    loadInoutCategories() {
      // 使用已加载的全部分类数据按类型过滤，不再依赖需要权限的 settings API
      this.allCategories = this.filterCategories.filter(c => c.type === this.form.type)
    },
    getStockQty(catId) {
      const cat = this.warehouseCategories.find(c => c.id === catId)
      return cat ? cat.quantity : 0
    },
    async loadRecords() {
      const params = {
        pageNum: this.page,
        pageSize: this.pageSize,
        ...this.filter
      }
      const res = await listInoutRecords(params)
      this.records = res.rows || []
      this.total = res.total || 0
    },
    async submitRecord() {
      if (!this.form.inoutCategoryId) {
        return this.$message.warning('请选择进出库原因')
      }
      if (!this.form.warehouseCategoryId) {
        return this.$message.warning('请选择库房分类')
      }

      // 出库时检查库存
      if (this.form.type === 'out') {
        // 确保库房分类数据已加载
        if (!this.warehouseCategories || this.warehouseCategories.length === 0) {
          try {
            const res = await getBaseData()
            this.warehouseCategories = res.data.warehouseCategories || []
          } catch (e) {
            this.$message.warning('无法验证库存，请刷新页面后重试')
            return
          }
        }
        const catId = Number(this.form.warehouseCategoryId)
        const stock = this.warehouseCategories.find(c => Number(c.id) === catId)
        if (!stock) {
          this.$message.warning("未找到库存信息，将继续提交")
        } else {
          const available = Number(stock.quantity) || 0
          if (this.form.quantity > available) {
            this.shortageInfo = {
              quantity: this.form.quantity,
              available,
              catName: stock.categoryName || stock.name || ""
            }
            this.showStockShortage = true
            return
          }
        }
      }
      
      this.submitting = true
      try {
        await addInoutRecord(this.form)
        this.$message.success('登记成功')
        this.showDialog = false
        await this.loadRecords()
        // 刷新基础数据（库存）
        const res = await getBaseData()
        this.warehouseCategories = res.data.warehouseCategories || []
      } catch(e) {
        this.$message.error(e.message || '登记失败')
      } finally {
        this.submitting = false
      }
    },
    async deleteRecord(id) {
      try {
        await this.$confirm('确定删除该记录？系统将自动回滚库存。', '提示', { type: 'warning' })
        await deleteInoutRecord(id)
        this.$message.success('删除成功，库存已回滚')
        await this.loadRecords()
        // 刷新基础数据（库存）
        const res = await getBaseData()
        this.warehouseCategories = res.data.warehouseCategories || []
      } catch(e) {}
    },
    handleCloseShortage() {
      this.showStockShortage = false
      this.shortageInfo = { quantity: 0, available: 0, catName: '' }
    },
    clearDialogMargin() {
      // 用 JS 直接强制设置移动端弹窗样式，覆盖 Element UI 内联样式
      const applyMobileDialogStyle = () => {
        if (window.innerWidth > 668) return
        document.querySelectorAll('.inout-dialog .el-dialog, .shortage-dialog-wrapper .el-dialog').forEach(d => {
          d.style.width = '100%'
          d.style.maxWidth = '100%'
          d.style.marginTop = '0'
          d.style.marginBottom = '0'
          d.style.borderRadius = '0'
          d.style.minHeight = '100vh'
        })
        // 强制覆盖 label 和 content 的内联宽度
        document.querySelectorAll('.inout-dialog .el-form-item__label').forEach(el => {
          el.style.width = '80px'
          el.style.minWidth = '80px'
          el.style.maxWidth = '80px'
          el.style.textAlign = 'right'
          el.style.paddingRight = '12px'
        })
        document.querySelectorAll('.inout-dialog .el-form-item__content').forEach(el => {
          el.style.marginLeft = '80px'
        })
      }
      // 分多次执行，确保覆盖 Element UI 的动态写入
      setTimeout(applyMobileDialogStyle, 50)
      setTimeout(applyMobileDialogStyle, 200)
      setTimeout(applyMobileDialogStyle, 500)
    },
    injectDialogCSS() {
      // 动态注入全局 CSS（不被 scoped 限制），让弹窗在首次渲染时就有正确样式
      if (document.getElementById('inout-dialog-css')) return
      const css = `
        @media screen and (max-width: 668px) {
          .inout-dialog .el-dialog,
          .shortage-dialog-wrapper .el-dialog {
            width: 100% !important;
            max-width: 100% !important;
            margin: 0 !important;
            border-radius: 0 !important;
            min-height: 100vh !important;
            display: flex !important;
            flex-direction: column !important;
          }
          .inout-dialog .el-dialog[style],
          .shortage-dialog-wrapper .el-dialog[style] {
            margin-top: 0 !important;
            margin-bottom: 0 !important;
            width: 100% !important;
            max-width: 100% !important;
          }
          .inout-dialog .el-dialog .el-dialog__body {
            padding: 16px !important;
            flex: 1 !important;
            overflow-y: auto !important;
          }
          .inout-dialog .el-dialog .el-dialog__header {
            padding: 16px !important;
            flex-shrink: 0 !important;
            position: sticky !important;
            top: 0 !important;
            background: white !important;
            z-index: 10 !important;
          }
          .inout-dialog .el-dialog .el-dialog__footer {
            padding: 12px 16px !important;
            flex-shrink: 0 !important;
            position: sticky !important;
            bottom: 0 !important;
            background: white !important;
            z-index: 10 !important;
            box-shadow: 0 -2px 8px rgba(0,0,0,0.08) !important;
          }
          .inout-dialog .el-dialog .el-form-item__label {
            width: 80px !important;
            min-width: 80px !important;
            max-width: 80px !important;
            text-align: right !important;
            padding-right: 12px !important;
            font-size: 14px !important;
          }
          .inout-dialog .el-dialog .el-form-item__content {
            margin-left: 80px !important;
            font-size: 14px !important;
          }
        }
      `
      const style = document.createElement('style')
      style.id = 'inout-dialog-css'
      style.textContent = css
      document.head.appendChild(style)
    },
    removeDialogCSS() {
      const style = document.getElementById('inout-dialog-css')
      if (style) style.remove()
    },
    resetForm() {
      const now = new Date()
      const pad = n => String(n).padStart(2,'0')
      this.form = {
        recordTime: `${now.getFullYear()}-${pad(now.getMonth()+1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:00`,
        type: this.form.type,
        quantity: 1,
        inoutCategoryId: null,
        warehouseCategoryId: null,
        receiverType: 'employee',
        receiverEmployeeId: null,
        receiverOutsideNote: '',
        senderType: 'employee',
        senderEmployeeId: null,
        note: ''
      }
    },
    onFilterTypeChange() {
      // 类型变化时清空原因筛选（因为可用原因列表已变）
      this.filter.inoutCategoryId = null
      this.loadRecords()
    },
    resetFilter() {
      this.filter = { type: '', inoutCategoryId: null }
      this.page = 1
      this.loadRecords()
    },
    handlePageChange(p) {
      this.page = p
      this.loadRecords()
    },
    formatDateTime(val) {
      if (!val) return '-'
      return String(val).substring(0, 16)
    }
  }
}
</script>

<style scoped>
.inout-page {
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

/* ========== 表单卡片 ========== */
.form-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  padding: 20px;
  margin-bottom: 20px;
}
.form-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e2e8f0;
}
.main-form {
  margin-top: 16px;
}
.full-width {
  width: 100%;
}
.type-group {
  display: flex;
}
.type-out {
  color: #ef4444;
  font-weight: 500;
}
.type-in {
  color: #22c55e;
  font-weight: 500;
}
.qty-input {
  width: 120px;
}
.qty-unit {
  margin-left: 8px;
  font-size: 13px;
  color: #94a3b8;
}
.submit-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 20px;
}

/* ========== 子面板 ========== */
.sub-panel {
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 12px;
}
.panel-out {
  border: 1px solid #fca5a5;
  background: #fef2f2;
}
.panel-in {
  border: 1px solid #86efac;
  background: #f0fdf4;
}
.panel-gray {
  border: 1px solid #e5e7eb;
  background: #f9fafb;
}
.panel-title {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
}
.required {
  color: #ef4444;
}
.radio-group {
  display: flex;
}

/* ========== 库存不足警告弹窗 ========== */
.shortage-dialog {
  text-align: center;
}
.dialog-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  font-size: 24px;
}
.dialog-icon-red {
  background: #fee2e2;
  color: #ef4444;
}
.dialog-msg {
  font-size: 14px;
  color: #475569;
  line-height: 1.6;
  margin-bottom: 16px;
}
.dialog-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 16px;
}
.dialog-stat-item {
  border-radius: 12px;
  padding: 12px;
  text-align: center;
}
.stat-available {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}
.stat-required {
  background: #fef2f2;
  border: 1px solid #fca5a5;
}
.stat-label {
  font-size: 11px;
  color: #94a3b8;
  margin-bottom: 4px;
}
.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: #1e293b;
}
.stat-required .stat-value {
  color: #ef4444;
}
.dialog-warn {
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 12px;
  color: #b45309;
}

/* ========== 筛选栏 ========== */
.filter-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  padding: 16px 20px;
  margin-bottom: 16px;
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
.total-hint {
  font-size: 13px;
  color: #64748b;
}

/* ========== 记录表格 ========== */
.table-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  padding: 0;
  overflow: hidden;
}
.record-table {
  width: 100%;
}
.type-tag {
  font-weight: 500;
}
.delete-btn {
  color: #ef4444;
}
.delete-btn:hover {
  color: #dc2626;
}
.pagination-wrapper {
  padding: 12px 20px;
  border-top: 1px solid #f1f5f9;
  text-align: right;
}
/* ========== 新布局样式 ========== */
.header-right {
  display: flex;
  gap: 16px;
  flex-shrink: 0;
}
.header-btn {
  font-weight: 600;
  padding: 12px 24px;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.btn-out {
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
  color: #dc2626;
  border: 1px solid #fca5a5;
}
.btn-out:hover {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.15);
  transform: translateY(-1px);
}
.btn-in {
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
  color: #16a34a;
  border: 1px solid #86efac;
}
.btn-in:hover {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  box-shadow: 0 4px 12px rgba(22, 163, 74, 0.15);
  transform: translateY(-1px);
}

/* ========== 对话框美化 ========== */
.el-dialog {
  border-radius: 16px !important;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15) !important;
}
.el-dialog__header {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  padding: 20px 24px !important;
  border-bottom: 1px solid #e2e8f0;
}
.el-dialog__title {
  font-weight: 700;
  font-size: 18px;
  color: #1e293b;
}
.el-dialog__body {
  padding: 24px !important;
}
.el-dialog__footer {
  padding: 16px 24px !important;
  border-top: 1px solid #f1f5f9;
  background: #fafbfc;
}

/* ========== 表单步骤样式 ========== */
.dialog-form {
  padding: 0;
}
.step-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}
.step-section:hover {
  border-color: #cbd5e1;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.step-section:last-child {
  margin-bottom: 0;
}
.step-title {
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.step-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  font-size: 13px;
  font-weight: 700;
  box-shadow: 0 2px 6px rgba(59, 130, 246, 0.3);
}
.stock-hint {
  margin-left: 10px;
  font-size: 12px;
  color: #64748b;
  background: #f1f5f9;
  padding: 4px 10px;
  border-radius: 6px;
}
.radio-group {
  margin-bottom: 4px;
}
.radio-group .el-radio-button__inner {
  border-radius: 8px !important;
  transition: all 0.3s ease;
}

/* ========== 页面头部优化 ========== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
/* ========== 响应式设计 ========== */
/* 桌面端：弹窗固定 650px 居中 */
@media screen and (min-width: 669px) {
  .inout-dialog .el-dialog,
  .shortage-dialog-wrapper .el-dialog {
    width: 650px !important;
  }
}
/* 页面头部：768px 断点 */
@media screen and (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
    padding: 16px;
    gap: 16px;
  }
  .header-left {
    text-align: center;
  }
  .header-right {
    flex-direction: column;
    width: 100%;
    gap: 12px;
  }
  .header-btn {
    width: 100%;
    justify-content: center;
  }
  .step-section {
    padding: 12px;
  }
  .filter-card {
    padding: 12px !important;
  }
  .filter-card .el-col {
    margin-bottom: 8px;
  }
  .table-card {
    overflow-x: auto;
  }
  .record-table {
    min-width: 600px;
  }
}
</style>
