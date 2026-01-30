import request from '@/utils/request'

// ========== 用户接口（普通用户使用） ==========
// 用户获取自己的评估记录列表
export function getMyRecordList(params) {
  return request({
    url: '/health/record/my/list',
    method: 'get',
    params
  })
}

// 用户获取自己的评估记录详情
export function getMyRecordDetail(id) {
  return request({
    url: `/health/record/my/${id}`,
    method: 'get'
  })
}

// 用户提交评估
export function submitAssessment(data) {
  return request({
    url: '/health/record/submit',
    method: 'post',
    data
  })
}

// 用户删除自己的评估记录
export function deleteMyRecord(id) {
  return request({
    url: `/health/record/my/${id}`,
    method: 'delete'
  })
}

// 用户导出单个报告
export function exportReport(id) {
  return request({
    url: `/health/record/export/${id}`,
    method: 'get',
    responseType: 'blob'
  })
}

// 批量导出我的报告
export function exportMyReports(params) {
  return request({
    url: '/health/record/my/export',
    method: 'get',
    params: params,
    responseType: 'blob'
  })
}

// ========== 管理员接口（管理员使用） ==========
// 管理员获取所有记录列表
export function getRecordList(params) {
  return request({
    url: '/health/record/list',
    method: 'get',
    params
  })
}

// 管理员获取记录详情
export function getRecordDetail(id) {
  return request({
    url: `/health/record/${id}`,
    method: 'get'
  })
}

// 管理员新增记录
export function addRecord(data) {
  return request({
    url: '/health/record',
    method: 'post',
    data
  })
}

// 管理员修改记录
export function updateRecord(data) {
  return request({
    url: '/health/record',
    method: 'put',
    data
  })
}

// 管理员删除记录
export function delRecord(id) {
  return request({
    url: `/health/record/${id}`,
    method: 'delete'
  })
}

// ========== 兼容性导出（保持现有代码可用） ==========
// 为报告页面提供兼容性
export const deleteAssessmentRecord = deleteMyRecord
export const listMyRecords = getMyRecordList

// 为记录管理页面提供兼容性
export const listRecord = getRecordList

export const listMyRecord = getMyRecordList  // 添加这一行