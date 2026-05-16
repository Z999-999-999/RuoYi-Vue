import request from '@/utils/request'

// ==================== 应用管理 ====================

export function listApp(query) {
  return request({ url: '/bitable/app/list', method: 'get', params: query })
}

export function getApp(id) {
  return request({ url: `/bitable/app/${id}`, method: 'get' })
}

export function addApp(data) {
  return request({ url: '/bitable/app', method: 'post', data })
}

export function updateApp(data) {
  return request({ url: '/bitable/app', method: 'put', data })
}

export function deleteApp(id) {
  return request({ url: `/bitable/app/${id}`, method: 'delete' })
}

export function resetApiKey(id) {
  return request({ url: `/bitable/app/resetApiKey/${id}`, method: 'post' })
}

// ==================== 数据表管理 ====================

export function listTable(appToken) {
  return request({ url: `/bitable/table/list/${appToken}`, method: 'get' })
}

export function addTable(data) {
  return request({ url: '/bitable/table', method: 'post', data })
}

export function updateTable(data) {
  return request({ url: '/bitable/table', method: 'put', data })
}

export function deleteTable(id) {
  return request({ url: `/bitable/table/${id}`, method: 'delete' })
}

// ==================== 字段管理 ====================

export function listField(appToken, tableId) {
  return request({ url: `/bitable/field/list/${appToken}/${tableId}`, method: 'get' })
}

export function updateField(data) {
  return request({ url: '/bitable/field', method: 'put', data })
}

export function deleteField(id) {
  return request({ url: `/bitable/field/${id}`, method: 'delete' })
}

// ==================== 记录管理 ====================

export function listRecord(appToken, tableId, query) {
  return request({ url: `/bitable/record/list/${appToken}/${tableId}`, method: 'get', params: query })
}

export function countRecord(appToken, tableId) {
  return request({ url: `/bitable/record/count/${appToken}/${tableId}`, method: 'get' })
}

export function deleteRecord(id) {
  return request({ url: `/bitable/record/${id}`, method: 'delete' })
}

export function batchDeleteRecord(ids) {
  return request({ url: '/bitable/record/batch', method: 'delete', data: ids })
}

export function clearRecords(appToken, tableId) {
  return request({ url: `/bitable/record/clear/${appToken}/${tableId}`, method: 'delete' })
}

export function exportRecord(appToken, tableId) {
  return request({ url: `/bitable/record/export/${appToken}/${tableId}`, method: 'get', responseType: 'blob' })
}

// ==================== 概览 ====================

export function getOverview(appToken) {
  return request({ url: `/bitable/overview/${appToken}`, method: 'get' })
}
