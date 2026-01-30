import request from '@/utils/request';

/**
 * 查询我的心理档案
 */
export function getMyProfile() {
  return request({
    url: '/health/profile/my',
    method: 'get'
  });
}

/**
 * 检查是否已填写心理档案
 */
export function checkProfile() {
  return request({
    url: '/health/profile/check',
    method: 'get'
  });
}

/**
 * 创建我的心理档案
 */
export function createMyProfile(data) {
  return request({
    url: '/health/profile/my',
    method: 'post',
    data: {
      realName: data.realName,
      gender: data.gender,
      age: data.age,
      occupation: data.occupation,
      emotionalState: data.emotionalState,
      sleepQuality: data.sleepQuality,
      stressLevel: data.stressLevel,
      mainConcernsArray: data.mainConcernsArray,
      otherConcerns: data.otherConcerns,
      expectations: data.expectations,
      therapyExperience: data.therapyExperience,
      additionalNotes: data.additionalNotes,
      userId: data.userId
    }
  });
}

/**
 * 更新我的心理档案
 */
export function updateMyProfile(data) {
  return request({
    url: '/health/profile/my',
    method: 'put',
    data: {
      id: data.id,
      realName: data.realName,
      gender: data.gender,
      age: data.age,
      occupation: data.occupation,
      emotionalState: data.emotionalState,
      sleepQuality: data.sleepQuality,
      stressLevel: data.stressLevel,
      mainConcernsArray: data.mainConcernsArray,
      otherConcerns: data.otherConcerns,
      expectations: data.expectations,
      therapyExperience: data.therapyExperience,
      additionalNotes: data.additionalNotes,
      userId: data.userId
    }
  });
}

/**
 * 咨询师获取可见档案列表
 */
export function getConsultantProfileList(params) {
  return request({
    url: '/health/profile/consultant/list',
    method: 'get',
    params
  });
}

/**
 * 咨询师查看具体档案
 */
export function getConsultantProfileDetail(userId) {
  return request({
    url: `/health/profile/consultant/${userId}`,
    method: 'get'
  });
}

/**
 * 用户设置档案可见性
 */
export function updateProfileVisibility(visible) {
  return request({
    url: '/health/profile/visibility',
    method: 'put',
    data: {
      visible: visible
    }
  });
}

/**
 * 获取档案可见性状态
 */
export function getProfileVisibility() {
  return request({
    url: '/health/profile/visibility',
    method: 'get'
  });
}

/**
 * 咨询师添加备注
 */
export function addProfileNote(data) {
  return request({
    url: '/health/profile/note',
    method: 'post',
    data
  });
}

/**
 * 获取档案备注列表
 */
export function getProfileNotes(userId) {
  return request({
    url: `/health/profile/note/${userId}`,
    method: 'get'
  });
}

/**
 * 调试接口 - 获取权限信息
 */
export function getDebugAuthInfo() {
  return request({
    url: '/debug/auth',
    method: 'get'
  });
}

/**
 * 调试接口 - 测试咨询师权限
 */
export function testConsultantPermission() {
  return request({
    url: '/debug/test/consultant',
    method: 'get'
  });
}