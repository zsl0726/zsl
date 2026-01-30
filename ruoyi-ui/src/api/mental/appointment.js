import request from '@/utils/request'

// ====== 用户预约相关API ======

// 创建预约
export function addAppointment(data) {
  return request({
    url: '/health/consultation/appointment',
    method: 'post',
    data: data
  })
}

// 获取我的预约列表
export function getMyAppointments(params) {
  return request({
    url: '/health/consultation/appointment/my',
    method: 'get',
    params: params
  })
}

// 取消预约
export function cancelAppointment(id) {
  return request({
    url: `/health/consultation/appointment/${id}/cancel`,
    method: 'put'
  })
}

// ====== 咨询师相关API ======

// 获取咨询师列表
export function getPsychologists(params) {
  return request({
    url: '/health/consultation/appointment/psychologists',
    method: 'get',
    params: params
  })
}

// 获取咨询师预约列表
export function getPsychologistAppointments(params) {
  return request({
    url: '/health/consultation/appointment/psychologist',
    method: 'get',
    params: params
  })
}

// 获取指定咨询师详情
export function getPsychologistInfo(psychologistId) {
  return request({
    url: `/health/consultation/appointment/psychologist/${psychologistId}`,
    method: 'get'
  })
}

// 获取地点列表
export function getLocations() {
  return Promise.resolve({
    code: 200,
    data: [
      { id: 1, name: '心理咨询室A', address: '主楼301' },
      { id: 2, name: '心理咨询室B', address: '主楼302' },
      { id: 3, name: '心理咨询室C', address: '主楼303' }
    ]
  });
}

// 获取可用时间段
export function getAvailableTimeSlots(psychologistId, date) {
  return request({
    url: '/health/consultation/schedule/available-slots',
    method: 'get',
    params: {
      psychologistId,
      date
    }
  })
}

// 接受预约
export function acceptAppointment(id) {
  return request({
    url: `/health/consultation/appointment/${id}/accept`,
    method: 'put'
  })
}

// 拒绝预约
export function rejectAppointment(id) {
  return request({
    url: `/health/consultation/appointment/${id}/reject`,
    method: 'put'
  })
}

// 完成预约
export function completeAppointment(id, counselingNotes) {
  return request({
    url: `/health/consultation/appointment/${id}/complete`,
    method: 'put',
    params: { counselingNotes }
  })
}

// 更新咨询笔记
export function updateAppointmentNotes(id, counselingNotes) {
  return request({
    url: `/health/consultation/appointment/${id}/notes`,
    method: 'put',
    data: { counselingNotes }
  })
}

// 获取统计信息
export function getAppointmentStatistics() {
  return request({
    url: '/health/consultation/appointment/statistics',
    method: 'get'
  })
}

// 获取今日预约 (旧接口，可能有问题)
export function getTodayAppointments() {
  return request({
    url: '/health/consultation/appointment/today',
    method: 'get'
  })
}

// ============ 新增：咨询师首页专用接口 ============

// 获取咨询师今日预约（新接口，支持日期参数）
export function getTodayAppointmentsForPsychologist(date) {
  return request({
    url: '/health/consultation/appointment/psychologist/today',
    method: 'get',
    params: { date }
  })
}

// 获取咨询师待处理预约
export function getPendingAppointmentsForPsychologist() {
  return request({
    url: '/health/consultation/appointment/psychologist/pending',
    method: 'get'
  })
}

// 获取咨询师首页统计信息
export function getPsychologistDashboard() {
  return request({
    url: '/health/consultation/appointment/psychologist/dashboard',
    method: 'get'
  })
}

// 获取预约数量统计
export function getAppointmentCount() {
  return request({
    url: '/health/consultation/appointment/count',
    method: 'get'
  })
}

// ====== 排班管理API ======

// 获取排班列表
export function getScheduleList(params) {
  return request({
    url: '/health/consultation/schedule/list',
    method: 'get',
    params: params
  })
}

// 获取排班详情
export function getSchedule(id) {
  return request({
    url: `/health/consultation/schedule/${id}`,
    method: 'get'
  })
}

// 获取指定日期的排班
export function getScheduleByDate(date) {
  return request({
    url: '/health/consultation/schedule/by-date',
    method: 'get',
    params: { date }
  })
}

// 获取排班日历数据
export function getScheduleCalendar(month) {
  return request({
    url: '/health/consultation/schedule/calendar',
    method: 'get',
    params: { month }
  })
}

// 新增排班
export function addSchedule(data) {
  return request({
    url: '/health/consultation/schedule',
    method: 'post',
    data: data
  })
}

// 更新排班
export function updateSchedule(data) {
  return request({
    url: '/health/consultation/schedule',
    method: 'put',
    data: data
  })
}

// 保存排班（新增或更新）
export function saveSchedule(data) {
  return request({
    url: '/health/consultation/schedule/save',
    method: 'post',
    data: data
  })
}

// 批量保存排班
export function batchSaveSchedule(data) {
  return request({
    url: '/health/consultation/schedule/batch',
    method: 'post',
    data: data
  })
}

// 删除排班
export function deleteSchedule(id) {
  return request({
    url: `/health/consultation/schedule/${id}`,
    method: 'delete'
  })
}

// 获取时间段选项
export function getTimeSlotOptions() {
  return request({
    url: '/health/consultation/schedule/time-slots/options',
    method: 'get'
  })
}

// ====== 通用管理API ======

// 查询预约列表（管理员）
export function listAppointment(query) {
  return request({
    url: '/health/consultation/appointment/list',
    method: 'get',
    params: query
  })
}

// 查询预约详情
export function getAppointment(id) {
  return request({
    url: `/health/consultation/appointment/${id}`,
    method: 'get'
  })
}

// 删除预约
export function delAppointment(ids) {
  return request({
    url: `/health/consultation/appointment/${ids}`,
    method: 'delete'
  })
}

// ====== 咨询评价相关API ======

// 提交评价
export function submitRating(data) {
  return request({
    url: '/health/consultation/appointment/rating/submit',
    method: 'post',
    data: data
  })
}

// 获取咨询师评分信息
export function getPsychologistRating() {
  return request({
    url: '/health/consultation/appointment/psychologist/rating',
    method: 'get'
  })
}

// 获取特定咨询师评分信息
export function getPsychologistRatingById(psychologistId) {
  return request({
    url: `/health/consultation/appointment/psychologist/rating/${psychologistId}`,
    method: 'get'
  })
}

// 根据预约ID查询评价
export function getRatingByAppointmentId(appointmentId) {
  return request({
    url: `/health/consultation/appointment/rating/appointment/${appointmentId}`,
    method: 'get'
  })
}

// 获取咨询师所有评价
export function getPsychologistRatings(psychologistId) {
  return request({
    url: `/health/consultation/appointment/psychologist/ratings/${psychologistId}`,
    method: 'get'
  })
}