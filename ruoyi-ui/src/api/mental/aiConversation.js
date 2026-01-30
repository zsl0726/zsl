// ruoyi-ui/src/api/mental/aiConversation.js
import request from '@/utils/request'

// 用户端接口
export function startNewConversation() {
  return request({
    url: '/mental/conversation/my/start',
    method: 'post'
  })
}

export function sendMessage(data) {
  return request({
    url: '/mental/conversation/my/send',
    method: 'post',
    data: data,
    timeout:60000
  })
}

export function getConversationHistory(sessionId) {
  return request({
    url: `/mental/conversation/my/history/${sessionId}`,
    method: 'get'
  })
}

export function getMySessions() {
  return request({
    url: '/mental/conversation/my/sessions',
    method: 'get'
  })
}

export function deleteMySession(sessionId) {
  return request({
    url: `/mental/conversation/my/session/${sessionId}`,
    method: 'delete'
  })
}

// 管理端接口（代码生成器生成）
export function listAiConversation(query) {
  return request({
    url: '/mental/conversation/list',
    method: 'get',
    params: query
  })
}

export function getAiConversation(id) {
  return request({
    url: `/mental/conversation/${id}`,
    method: 'get'
  })
}

export function addAiConversation(data) {
  return request({
    url: '/mental/conversation',
    method: 'post',
    data: data
  })
}

export function updateAiConversation(data) {
  return request({
    url: '/mental/conversation',
    method: 'put',
    data: data
  })
}

export function delAiConversation(id) {
  return request({
    url: `/mental/conversation/${id}`,
    method: 'delete'
  })
}

export function exportAiConversation(query) {
  return request({
    url: '/mental/conversation/export',
    method: 'post',
    responseType: 'blob',
    params: query
  })
}