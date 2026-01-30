import request from '@/utils/request'

// 查询心理评估量列表
export function listScale(query) {
  return request({
    url: '/health/scale/list',
    method: 'get',
    params: query
  })
}

// 查询心理评估量详细
export function getScale(id) {
  return request({
    url: '/health/scale/' + id,
    method: 'get'
  })
}

// 新增心理评估量
export function addScale(data) {
  return request({
    url: '/health/scale',
    method: 'post',
    data: data
  })
}

// 修改心理评估量
export function updateScale(data) {
  return request({
    url: '/health/scale',
    method: 'put',
    data: data
  })
}

// 删除心理评估量
export function delScale(id) {
  return request({
    url: '/health/scale/' + id,
    method: 'delete'
  })
}
