<template>
  <div class="settings-page">
    <!-- ========== 页面头部 ========== -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">系统设置</h1>
        <p class="page-date">管理员工、分类、原因等基础数据</p>
      </div>
    </div>

    <!-- ========== 设置项 Tabs ========== -->
    <div class="tabs-wrapper">
      <el-tabs v-model="activeTab" type="card" class="settings-tabs">
        <!-- Tab 1: 员工管理 -->
        <el-tab-pane label="员工管理" name="employee">
          <div class="tab-content">
            <!-- 添加表单 -->
            <div class="form-card">
              <div class="form-header">
                <i class="el-icon-user"></i>
                <span>添加新员工</span>
              </div>
              <el-form :model="empForm" inline size="small" class="add-form">
                <el-form-item label="姓名">
                  <el-input v-model="empForm.name" placeholder="员工姓名" class="form-input" />
                </el-form-item>
                <el-form-item label="部门">
                  <el-input v-model="empForm.department" placeholder="部门（可选）" class="form-input" />
                </el-form-item>
                <el-form-item label="手机数">
                  <el-input-number v-model="empForm.phoneTotal" :min="0" class="qty-input" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="addEmployee" class="add-btn">
                    <i class="el-icon-plus"></i> 添加员工
                  </el-button>
                </el-form-item>
              </el-form>
            </div>

            <!-- 员工列表 -->
            <div class="table-card">
              <el-table :data="employees" size="small" stripe class="data-table">
                <el-table-column prop="name" label="姓名" width="120" />
                <el-table-column prop="department" label="部门" width="150" />
                <el-table-column prop="phoneTotal" label="持有手机数" width="110" align="center" />
                <el-table-column label="操作" width="140" align="center">
                  <template v-slot="{ row }">
                    <el-button type="text" size="mini" @click="startEditEmployee(row)" class="edit-btn">编辑</el-button>
                    <el-button type="text" size="mini" @click="removeEmployee(row.id)" class="delete-btn">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>

          <!-- 编辑弹窗 -->
          <el-dialog title="编辑员工" :visible.sync="editEmpVisible" width="90%" class="edit-dialog">
            <el-form :model="editEmpForm" label-width="80px" size="small">
              <el-form-item label="姓名"><el-input v-model="editEmpForm.name" /></el-form-item>
              <el-form-item label="部门"><el-input v-model="editEmpForm.department" /></el-form-item>
              <el-form-item label="手机数量"><el-input-number v-model="editEmpForm.phoneTotal" :min="0" /></el-form-item>
            </el-form>
            <span slot="footer">
              <el-button @click="editEmpVisible=false">取消</el-button>
              <el-button type="primary" @click="saveEditEmployee">保存</el-button>
            </span>
          </el-dialog>
        </el-tab-pane>

        <!-- Tab 2: 公司总数 -->
        <el-tab-pane label="公司总数" name="companyTotal">
          <div class="tab-content">
            <div class="setting-card">
              <div class="setting-header">
                <i class="el-icon-setting"></i>
                <span>设备总数设置</span>
              </div>
              <div class="setting-body">
                <p class="setting-hint">
                  此值用于与实际计算总数（员工持有 + 库房库存 + 在外设备）对比，不一致时系统将给出警告提示。
                </p>
                <div class="setting-control">
                  <label class="setting-label">公司设备总数</label>
                  <el-input-number v-model="companyTotal" :min="0" :step="1" class="total-input" />
                  <el-button type="primary" @click="saveCompanyTotal" class="save-btn">
                    <i class="el-icon-check"></i> 保存
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- Tab 3: 账号类型 -->
        <el-tab-pane label="账号类型" name="platform">
          <div class="tab-content">
            <div class="action-bar">
              <el-button type="primary" size="small" @click="showPlatformDialog(null)" class="add-btn">
                <i class="el-icon-plus"></i> 添加平台
              </el-button>
            </div>

            <div class="table-card">
              <el-table :data="platforms" size="small" stripe class="data-table">
                <el-table-column prop="code" label="编码" width="100" />
                <el-table-column prop="label" label="名称" width="100" />
                <el-table-column label="颜色" width="120" align="center">
                  <template v-slot="{ row }">
                    <span class="color-block" :style="{ background: row.color || '#94a3b8' }"></span>
                    <span class="color-hex">{{ (row.color || '#94a3b8').toUpperCase() }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="sort" label="排序" width="60" align="center" />
                <el-table-column label="操作" width="130" align="center">
                  <template v-slot="{ row }">
                    <el-button type="text" size="mini" @click="showPlatformDialog(row)" class="edit-btn">编辑</el-button>
                    <el-button type="text" size="mini" @click="removePlatform(row.id)" class="delete-btn">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>

          <!-- 平台编辑弹窗 -->
          <el-dialog :title="platformForm.id ? '编辑平台' : '添加平台'" :visible.sync="platformVisible" width="90%" class="edit-dialog">
            <el-form :model="platformForm" label-width="100px" size="small">
              <el-form-item label="编码"><el-input v-model="platformForm.code" :disabled="!!platformForm.id" /></el-form-item>
              <el-form-item label="名称"><el-input v-model="platformForm.label" /></el-form-item>
              <el-form-item label="颜色">
                <el-color-picker v-model="platformForm.color" :predefine="predefineColors" />
                <span class="color-value">{{ platformForm.color || '#94a3b8' }}</span>
              </el-form-item>
              <el-form-item label="在用关键词"><el-input v-model="platformForm.activeAliases" placeholder="逗号分隔，如：抖音,在用抖音" /></el-form-item>
              <el-form-item label="禁用关键词"><el-input v-model="platformForm.disabledAliases" placeholder="逗号分隔" /></el-form-item>
              <el-form-item label="发作品关键词"><el-input v-model="platformForm.postingAliases" placeholder="逗号分隔" /></el-form-item>
              <el-form-item label="排序"><el-input-number v-model="platformForm.sort" :min="0" /></el-form-item>
            </el-form>
            <span slot="footer">
              <el-button @click="platformVisible=false">取消</el-button>
              <el-button type="primary" @click="savePlatform">保存</el-button>
            </span>
          </el-dialog>
        </el-tab-pane>

        <!-- Tab 4: 库房分类 -->
        <el-tab-pane label="库房分类" name="warehouseCategory">
          <div class="tab-content">
            <div class="action-bar">
              <el-button type="primary" size="small" @click="showWareCatDialog(null)" class="add-btn">
                <i class="el-icon-plus"></i> 添加分类
              </el-button>
            </div>

            <div class="table-card">
              <el-table :data="warehouseCategories" size="small" stripe class="data-table">
                <el-table-column prop="name" label="名称" width="120" />
                <el-table-column prop="type" label="类型" width="120">
                  <template v-slot="{ row }">
                    {{ typeLabel(row.type) }}
                  </template>
                </el-table-column>
                <el-table-column label="颜色" width="120" align="center">
                  <template v-slot="{ row }">
                    <span class="color-block" :style="{ background: row.color || '#3b82f6' }"></span>
                    <span class="color-hex">{{ (row.color || '#3b82f6').toUpperCase() }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120" align="center">
                  <template v-slot="{ row }">
                    <el-button type="text" size="mini" @click="showWareCatDialog(row)" class="edit-btn">编辑</el-button>
                    <el-button type="text" size="mini" @click="removeWareCat(row.id)" class="delete-btn">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>

          <!-- 分类编辑弹窗 -->
          <el-dialog :title="wareCatForm.id ? '编辑分类' : '添加分类'" :visible.sync="wareCatVisible" width="90%" class="edit-dialog">
            <el-form :model="wareCatForm" label-width="80px" size="small">
              <el-form-item label="名称"><el-input v-model="wareCatForm.name" /></el-form-item>
              <el-form-item label="类型">
                <el-select v-model="wareCatForm.type" class="full-width">
                  <el-option v-for="t in categoryTypes" :key="t.value" :label="t.label" :value="t.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="颜色">
                <el-color-picker v-model="wareCatForm.color" :predefine="predefineColors" />
                <span class="color-value">{{ wareCatForm.color || '#3b82f6' }}</span>
              </el-form-item>
            </el-form>
            <span slot="footer">
              <el-button @click="wareCatVisible=false">取消</el-button>
              <el-button type="primary" @click="saveWareCat">保存</el-button>
            </span>
          </el-dialog>
        </el-tab-pane>

        <!-- Tab 5: 进出库原因 -->
        <el-tab-pane label="进出库原因" name="inoutCategory">
          <div class="tab-content">
            <div class="form-card">
              <div class="form-header">
                <i class="el-icon-collection-tag"></i>
                <span>添加进出库原因</span>
              </div>
              <el-form :model="inoutCatForm" inline size="small" class="add-form">
                <el-form-item label="原因名称">
                  <el-input v-model="inoutCatForm.name" placeholder="原因名称" class="form-input" />
                </el-form-item>
                <el-form-item label="类型">
                  <el-select v-model="inoutCatForm.type" class="type-select">
                    <el-option label="出库" value="out" />
                    <el-option label="入库" value="in" />
                    <el-option label="两者" value="both" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="addInoutCat" class="add-btn">
                    <i class="el-icon-plus"></i> 添加
                  </el-button>
                </el-form-item>
              </el-form>
            </div>

            <div class="table-card">
              <el-table :data="inoutCategories" size="small" stripe class="data-table">
                <el-table-column prop="name" label="名称" />
                <el-table-column label="类型" width="80" align="center">
                  <template v-slot="{ row }">
                    <el-tag
                      :type="row.type === 'out' ? 'danger' : row.type === 'in' ? 'success' : ''"
                      size="mini"
                    >
                      {{ {out:'出库', in:'入库', both:'两者'}[row.type] }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="100" align="center">
                  <template v-slot="{ row }">
                    <el-button type="text" size="mini" @click="removeInoutCat(row.id)" class="delete-btn">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>

        <!-- Tab 6: 在外设备 -->
        <el-tab-pane label="在外设备" name="outside">
          <div class="tab-content">
            <div class="table-card">
              <el-table :data="outsideDevices" size="small" stripe class="data-table">
                <el-table-column prop="reason" label="原因" show-overflow-tooltip />
                <el-table-column prop="quantity" label="数量" width="60" align="center" />
                <el-table-column label="出去日期" width="110" align="center">
                  <template v-slot="{ row }">{{ formatDate(row.outDate) }}</template>
                </el-table-column>
                <el-table-column label="预计归还" width="110" align="center">
                  <template v-slot="{ row }">{{ formatDate(row.expectedReturnDate) || '-' }}</template>
                </el-table-column>
                <el-table-column label="实际归还" width="110" align="center">
                  <template v-slot="{ row }">
                    <span v-if="row.actualReturnDate" class="returned">{{ formatDate(row.actualReturnDate) }}</span>
                    <span v-else class="not-returned">未归还</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="100" align="center">
                  <template v-slot="{ row }">
                    <el-button v-if="!row.actualReturnDate" type="text" size="mini" @click="doReturn(row)" class="return-btn">确认归还</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import {
  listEmployee, addEmployee, updateEmployee, deleteEmployee,
  listWarehouseCategories, addWarehouseCategory, updateWarehouseCategory, deleteWarehouseCategory,
  listPlatforms, addPlatform, updatePlatform, deletePlatform,
  listInoutCategories, addInoutCategory, deleteInoutCategory,
  listOutsideDevices, addOutsideDevice, updateOutsideDevice, returnOutsideDevice,
  getConfig, setConfig
} from '@/api/device/index'

const PREDEFINE_COLORS = [
  '#1a1a2e', '#3b82f6', '#06b6d4', '#22c55e',
  '#f97316', '#ec4899', '#a855f7', '#ef4444',
  '#94a3b8', '#facc15', '#6366f1', '#14b8a6',
  '#f59e0b', '#8b5cf6', '#e11d48', '#0891b2'
]
const CATEGORY_TYPES = [
  { value:'work_account', label:'作品号' },
  { value:'intercept_account', label:'截留号' },
  { value:'xiaohongshu', label:'小红书号' },
  { value:'abnormal', label:'异常设备' },
  { value:'pending_online', label:'待上机设备' },
  { value:'maintenance', label:'维修设备' },
  { value:'custom', label:'自定义' }
]

export default {
  name: 'DeviceSettings',
  data() {
    return {
      activeTab: 'employee',
      // 员工
      employees: [],
      empForm: { name:'', department:'', phoneTotal:0 },
      editEmpVisible: false,
      editEmpForm: {},
      // 公司总数
      companyTotal: 0,
      // 平台
      platforms: [],
      platformVisible: false,
      platformForm: { code:'', label:'', color:'#94a3b8', activeAliases:'', disabledAliases:'', postingAliases:'', sort:0 },
      predefineColors: [
        '#3b82f6', '#06b6d4', '#22c55e', '#f97316',
        '#ec4899', '#a855f7', '#ef4444', '#f59e0b',
        '#6366f1', '#14b8a6', '#84cc16', '#8b5cf6',
        '#e11d48', '#0891b2', '#0ea5e9', '#1a1a2e'
      ],
      // 库房分类
      warehouseCategories: [],
      wareCatVisible: false,
      wareCatForm: { name:'', type:'custom', color:'#3b82f6' },
      categoryTypes: CATEGORY_TYPES,
      // 进出库原因
      inoutCategories: [],
      inoutCatForm: { name:'', type:'both' },
      // 在外设备
      outsideDevices: [],
    }
  },
  created() {
    this.init()
    // 支持从URL参数自动切换Tab
    const tab = this.$route.query.tab
    if (tab && ['employee', 'companyTotal', 'platform', 'warehouseCategory', 'inoutCategory', 'outside'].includes(tab)) {
      this.activeTab = tab
    }
  },
  methods: {
    async init() {
      await Promise.all([
        this.loadEmployees(), this.loadPlatforms(), this.loadWareCats(),
        this.loadInoutCats(), this.loadOutsides(), this.loadCompanyTotal()
      ])
    },
    colorHex(c) { return c || '#94a3b8' },
    typeLabel(v) {
      const t = CATEGORY_TYPES.find(t => t.value === v)
      return t ? t.label : v
    },
    formatDate(v) { return v ? String(v).substring(0, 10) : '' },

    // ---- 员工 ----
    async loadEmployees() {
      const res = await listEmployee()
      this.employees = res.data || []
    },
    async addEmployee() {
      if (!this.empForm.name) return this.$message.warning('请填写姓名')
      await addEmployee({ ...this.empForm })
      this.$message.success('添加成功')
      this.empForm = { name:'', department:'', phoneTotal:0 }
      this.loadEmployees()
    },
    startEditEmployee(row) {
      this.editEmpForm = { ...row }
      this.editEmpVisible = true
    },
    async saveEditEmployee() {
      await updateEmployee(this.editEmpForm)
      this.$message.success('修改成功')
      this.editEmpVisible = false
      this.loadEmployees()
    },
    async removeEmployee(id) {
      await this.$confirm('确定删除该员工？此操作不可逆。', '提示', { type: 'warning' })
      await deleteEmployee(id)
      this.$message.success('删除成功')
      this.loadEmployees()
    },

    // ---- 公司总数 ----
    async loadCompanyTotal() {
      try {
        const res = await getConfig('company_phone_total')
        const data = res.data || {}
        // 兼容两种返回格式：{value: "253"} 或 直接字符串
        const val = data.value !== undefined ? data.value : data
        this.companyTotal = (val !== null && val !== undefined && val !== '') ? parseInt(val) : 0
      } catch(e) {
        this.companyTotal = 0
      }
    },
    async saveCompanyTotal() {
      await setConfig('company_phone_total', String(this.companyTotal))
      this.$message.success('保存成功')
      await this.loadCompanyTotal()
    },

    // ---- 平台 ----
    async loadPlatforms() {
      const res = await listPlatforms()
      this.platforms = res.data || []
    },
    showPlatformDialog(row) {
      if (row) {
        this.platformForm = {
          ...row,
          activeAliases: this.jsonToStr(row.activeAliases),
          disabledAliases: this.jsonToStr(row.disabledAliases),
          postingAliases: this.jsonToStr(row.postingAliases)
        }
      } else {
        this.platformForm = { code:'', label:'', color:'#94a3b8', activeAliases:'', disabledAliases:'', postingAliases:'', sort:0 }
      }
      this.platformVisible = true
    },
    async savePlatform() {
      const data = {
        ...this.platformForm,
        activeAliases: this.strToJson(this.platformForm.activeAliases),
        disabledAliases: this.strToJson(this.platformForm.disabledAliases),
        postingAliases: this.strToJson(this.platformForm.postingAliases)
      }
      if (data.id) await updatePlatform(data)
      else await addPlatform(data)
      this.$message.success('保存成功')
      this.platformVisible = false
      this.loadPlatforms()
    },
    async removePlatform(id) {
      await this.$confirm('确定删除该平台？', '提示', { type: 'warning' })
      await deletePlatform(id)
      this.$message.success('删除成功')
      this.loadPlatforms()
    },

    // ---- 库房分类 ----
    async loadWareCats() {
      const res = await listWarehouseCategories()
      this.warehouseCategories = res.data || []
    },
    showWareCatDialog(row) {
      this.wareCatForm = row ? { ...row } : { name:'', type:'custom', color:'#3b82f6' }
      this.wareCatVisible = true
    },
    async saveWareCat() {
      const data = this.wareCatForm
      if (data.id) await updateWarehouseCategory(data)
      else await addWarehouseCategory(data)
      this.$message.success('保存成功')
      this.wareCatVisible = false
      this.loadWareCats()
    },
    async removeWareCat(id) {
      await this.$confirm('确定删除该分类？', '提示', { type: 'warning' })
      await deleteWarehouseCategory(id)
      this.$message.success('删除成功')
      this.loadWareCats()
    },

    // ---- 进出库原因 ----
    async loadInoutCats() {
      const res = await listInoutCategories()
      this.inoutCategories = res.data || []
    },
    async addInoutCat() {
      if (!this.inoutCatForm.name) return this.$message.warning('请填写原因名称')
      await addInoutCategory({ ...this.inoutCatForm })
      this.$message.success('添加成功')
      this.inoutCatForm = { name:'', type:'both' }
      this.loadInoutCats()
    },
    async removeInoutCat(id) {
      await this.$confirm('确定删除？', '提示', { type: 'warning' })
      await deleteInoutCategory(id)
      this.$message.success('删除成功')
      this.loadInoutCats()
    },

    // ---- 在外设备 ----
    async loadOutsides() {
      const res = await listOutsideDevices()
      this.outsideDevices = res.data || []
    },
    async doReturn(row) {
      try {
        await this.$prompt('请输入归还日期', '确认归还', {
          inputValue: new Date().toISOString().substring(0, 10),
          inputPlaceholder: 'yyyy-MM-dd'
        }).then(({ value }) => {
          returnOutsideDevice(row.id, value)
          this.$message.success('已标记归还')
          this.loadOutsides()
        })
      } catch(e) {}
    },

    // ---- 工具 ----
    jsonToStr(v) {
      try { return JSON.parse(v || '[]').join(', ') } catch(e) { return v || '' }
    },
    strToJson(v) {
      return JSON.stringify(v.split(',').map(s => s.trim()).filter(Boolean))
    },
  }
}
</script>

<style scoped>
.settings-page {
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

/* ========== Tabs ========== */
.tabs-wrapper {
  background: transparent;
}
.settings-tabs {
  background: transparent;
}
.settings-tabs >>> .el-tabs__header {
  background: transparent;
  border-bottom: 2px solid #e2e8f0;
}
.settings-tabs >>> .el-tabs__item {
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  border: none;
  background: transparent;
  height: 40px;
  line-height: 40px;
}
.settings-tabs >>> .el-tabs__item.is-active {
  color: #3b82f6;
  background: transparent;
  border-bottom: 2px solid #3b82f6;
}
.settings-tabs >>> .el-tabs__content {
  padding: 20px 0;
}
.tab-content {
  min-height: 400px;
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
.add-form {
  margin-top: 16px;
}
.form-input {
  width: 160px;
}
.qty-input {
  width: 120px;
}
.add-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* ========== 表格卡片 ========== */
.table-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
}
.data-table {
  width: 100%;
}
.edit-btn {
  color: #3b82f6;
  font-size: 12px;
}
.delete-btn {
  color: #ef4444;
  font-size: 12px;
}
.return-btn {
  color: #22c55e;
  font-size: 12px;
}
.returned {
  color: #16a34a;
  font-size: 12px;
}
.not-returned {
  color: #9ca3af;
  font-size: 12px;
}

/* ========== 设置卡片 ========== */
.setting-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  padding: 20px;
  max-width: 600px;
}
.setting-header {
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
.setting-hint {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.6;
  margin-bottom: 20px;
  background: #f8fafc;
  padding: 12px;
  border-radius: 8px;
}
.setting-control {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.setting-label {
  font-size: 13px;
  color: #374151;
  font-weight: 500;
}
.total-input {
  width: 140px;
}
.save-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* ========== 操作栏 ========== */
.action-bar {
  margin-bottom: 16px;
}

/* ========== 颜色选择 ========== */
.color-block {
  display: inline-block;
  width: 24px;
  height: 24px;
  border-radius: 5px;
  border: 1px solid rgba(0,0,0,0.08);
  vertical-align: middle;
  flex-shrink: 0;
}
.color-hex {
  font-size: 11px;
  color: #64748b;
  margin-left: 6px;
  font-family: 'Courier New', monospace;
  font-weight: 600;
  letter-spacing: 0.5px;
}
.color-value {
  margin-left: 12px;
  font-size: 12px;
  color: #64748b;
  font-family: 'Courier New', monospace;
}
.full-width {
  width: 100%;
}
.type-select {
  width: 120px;
}

/* ========== 弹窗 ========== */
.edit-dialog >>> .el-dialog {
  max-width: 550px;
}
.edit-dialog >>> .el-dialog__body {
  padding: 20px;
}
.edit-dialog >>> .el-dialog__header {
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .edit-dialog >>> .el-dialog {
    width: 95% !important;
    max-width: none !important;
    margin-top: 3vh !important;
  }
  .edit-dialog >>> .el-dialog__body {
    padding: 12px !important;
  }
  .edit-dialog >>> .el-dialog__header {
    padding: 12px !important;
  }
}
</style>
