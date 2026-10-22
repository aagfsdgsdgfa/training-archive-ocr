import request from '@/utils/request'

// 查询OCR防重与拦截记录列表
export function listLog(query) {
  return request({
    url: '/biz/log/list',
    method: 'get',
    params: query
  })
}

// 查询OCR防重与拦截记录详细
export function getLog(logId) {
  return request({
    url: '/biz/log/' + logId,
    method: 'get'
  })
}

// 新增OCR防重与拦截记录
export function addLog(data) {
  return request({
    url: '/biz/log',
    method: 'post',
    data: data
  })
}

// 修改OCR防重与拦截记录
export function updateLog(data) {
  return request({
    url: '/biz/log',
    method: 'put',
    data: data
  })
}

// 按条件批量删除所有匹配数据
export function deleteAllLog(query) {
  return request({
    url: '/biz/log/deleteAll',
    method: 'delete',
    params: query
  })
}

// 删除OCR防重与拦截记录
export function delLog(logId) {
  return request({
    url: '/biz/log/' + logId,
    method: 'delete'
  })
}
