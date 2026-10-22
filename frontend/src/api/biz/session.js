import request from '@/utils/request'

// 查询培训执行场次列表
export function listSession(query) {
  return request({
    url: '/biz/session/list',
    method: 'get',
    params: query
  })
}

// 查询培训执行场次详细
export function getSession(sessionId) {
  return request({
    url: '/biz/session/' + sessionId,
    method: 'get'
  })
}

// 新增培训执行场次
export function addSession(data) {
  return request({
    url: '/biz/session',
    method: 'post',
    data: data
  })
}

// 修改培训执行场次
export function updateSession(data) {
  return request({
    url: '/biz/session',
    method: 'put',
    data: data
  })
}

// 删除培训执行场次
export function delSession(sessionId) {
  return request({
    url: '/biz/session/' + sessionId,
    method: 'delete'
  })
}


// 按条件批量删除所有匹配数据
export function deleteAllSession(query) {
  return request({
    url: '/biz/session/deleteAll',
    method: 'delete',
    params: query
  })
}

//  一键归档结算培训场次
export function archiveSession(sessionId) {
  return request({
    url: '/biz/session/archive/' + sessionId,
    method: 'post'
  })
}