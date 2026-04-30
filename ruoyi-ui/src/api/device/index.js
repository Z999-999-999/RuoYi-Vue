import request from '@/utils/request'

// ==================== 员工管理 ====================

export function listEmployee(query) {
  return request({ url: '/device/employee/all', method: 'get', params: query })
}

export function addEmployee(data) {
  return request({ url: '/device/employee', method: 'post', data })
}

export function updateEmployee(data) {
  return request({ url: '/device/employee', method: 'put', data })
}

export function deleteEmployee(id) {
  return request({ url: `/device/employee/${id}`, method: 'delete' })
}

// ==================== 总览仪表盘 ====================

export function getDashboard() {
  return request({ url: '/device/dashboard', method: 'get' })
}

export function getBaseData() {
  return request({ url: '/device/dashboard/base', method: 'get' })
}

export function getMonthlyOutRank(yearMonth, categoryId) {
  const params = { yearMonth }
  if (categoryId) params.categoryId = categoryId
  return request({ url: '/device/dashboard/outRank', method: 'get', params })
}

export function getMonthlyInRank(yearMonth, categoryId) {
  const params = { yearMonth }
  if (categoryId) params.categoryId = categoryId
  return request({ url: '/device/dashboard/inRank', method: 'get', params })
}

export function getEmployeeDetail(id, params) {
  return request({ url: `/device/dashboard/employee/${id}`, method: 'get', params })
}

export function getEmployeePlatformTags() {
  return request({ url: '/device/dashboard/employeePlatformTags', method: 'get' })
}

// ==================== 库房管理 ====================

export function listWarehouseCategories() {
  return request({ url: '/device/warehouse/categories', method: 'get' })
}

export function addWarehouseCategory(data) {
  return request({ url: '/device/warehouse/category', method: 'post', data })
}

export function updateWarehouseCategory(data) {
  return request({ url: '/device/warehouse/category', method: 'put', data })
}

export function deleteWarehouseCategory(id) {
  return request({ url: `/device/warehouse/category/${id}`, method: 'delete' })
}

export function listStocks() {
  return request({ url: '/device/warehouse/stocks', method: 'get' })
}

export function updateStockBatch(items) {
  return request({ url: '/device/warehouse/stock/batch', method: 'post', data: { items } })
}

// ==================== 进出库 ====================

export function listInoutRecords(query) {
  return request({ url: '/device/inout/list', method: 'get', params: query })
}

export function addInoutRecord(data) {
  return request({ url: '/device/inout', method: 'post', data })
}

export function deleteInoutRecord(id) {
  return request({ url: `/device/inout/${id}`, method: 'delete' })
}

export function getEmployeeRecords(employeeId, limit = 20) {
  return request({ url: `/device/inout/employee/${employeeId}`, method: 'get', params: { limit } })
}

// ==================== 每日汇报 ====================

export function getReportsByDate(reportDate) {
  return request({ url: '/device/daily/report', method: 'get', params: { reportDate } })
}

export function submitReport(data) {
  return request({ url: '/device/daily/report', method: 'post', data })
}

export function deleteReport(id) {
  return request({ url: `/device/daily/report/${id}`, method: 'delete' })
}

export function getRecentReports(employeeId, limit = 30) {
  return request({ url: `/device/daily/report/recent/${employeeId}`, method: 'get', params: { limit } })
}

// ==================== 每日核对 ====================

export function getChecksByDate(checkDate) {
  return request({ url: '/device/daily/check', method: 'get', params: { checkDate } })
}

export function updateDailyCheck(data) {
  return request({ url: '/device/daily/check', method: 'post', data })
}

// ==================== 在外设备 ====================

export function listOutsideDevices() {
  return request({ url: '/device/outside/list', method: 'get' })
}

export function addOutsideDevice(data) {
  return request({ url: '/device/outside', method: 'post', data })
}

export function updateOutsideDevice(data) {
  return request({ url: '/device/outside', method: 'put', data })
}

export function returnOutsideDevice(id, actualReturnDate) {
  return request({ url: `/device/outside/return/${id}`, method: 'put', data: { actualReturnDate } })
}

export function deleteOutsideDevice(id) {
  return request({ url: `/device/outside/${id}`, method: 'delete' })
}

// ==================== 设置：账号平台 ====================

export function listPlatforms() {
  return request({ url: '/device/settings/platforms', method: 'get' })
}

export function addPlatform(data) {
  return request({ url: '/device/settings/platform', method: 'post', data })
}

export function updatePlatform(data) {
  return request({ url: '/device/settings/platform', method: 'put', data })
}

export function deletePlatform(id) {
  return request({ url: `/device/settings/platform/${id}`, method: 'delete' })
}

// ==================== 设置：进出库原因 ====================

export function listInoutCategories(type) {
  return request({ url: '/device/settings/inoutCategories', method: 'get', params: { type } })
}

export function addInoutCategory(data) {
  return request({ url: '/device/settings/inoutCategory', method: 'post', data })
}

export function updateInoutCategory(data) {
  return request({ url: '/device/settings/inoutCategory', method: 'put', data })
}

export function deleteInoutCategory(id) {
  return request({ url: `/device/settings/inoutCategory/${id}`, method: 'delete' })
}

// ==================== 设置：系统配置 ====================

export function getConfig(key) {
  return request({ url: '/device/settings/config', method: 'get', params: { key } })
}

export function setConfig(key, value) {
  return request({ url: '/device/settings/config', method: 'post', data: { key, value } })
}

// ==================== 操作日志 ====================

export function listLogs(query) {
  return request({ url: '/device/settings/logs', method: 'get', params: query })
}

export function getLogModules() {
  return request({ url: '/device/settings/logs/modules', method: 'get' })
}

export function getLogErrorCount() {
  return request({ url: '/device/settings/logs/errorCount', method: 'get' })
}

export function clearLogs(scope = 'info') {
  return request({ url: '/device/settings/logs/clear', method: 'delete', params: { scope } })
}
