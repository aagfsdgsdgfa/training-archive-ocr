import request from '@/utils/request'

// 查询OCR识别结果暂存明细列表
export function listResult(query) {
  return request({
    url: '/biz/result/list',
    method: 'get',
    params: query
  })
}

// 查询OCR识别结果暂存明细详细
export function getResult(resultId) {
  return request({
    url: '/biz/result/' + resultId,
    method: 'get'
  })
}

// 新增OCR识别结果暂存明细
export function addResult(data) {
  return request({
    url: '/biz/result',
    method: 'post',
    data: data
  })
}

// 修改OCR识别结果暂存明细
export function updateResult(data) {
  return request({
    url: '/biz/result',
    method: 'put',
    data: data
  })
}

// 按条件批量删除所有匹配数据
export function deleteAllResult(query) {
  return request({
    url: '/biz/result/deleteAll',
    method: 'delete',
    params: query
  })
}

// 删除OCR识别结果暂存明细
export function delResult(resultId) {
  return request({
    url: '/biz/result/' + resultId,
    method: 'delete'
  })
}
