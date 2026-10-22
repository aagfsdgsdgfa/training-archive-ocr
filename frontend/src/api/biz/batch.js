import request from '@/utils/request'

// // 查询OCR扫描批次主列表
// export function listBatch(query) {
//   return request({
//     url: '/biz/batch/list',
//     method: 'get',
//     params: query
//   })
// }

// 查询OCR扫描批次主详细
export function getBatch(batchId) {
  return request({
    url: '/biz/batch/' + batchId,
    method: 'get'
  })
}

// 新增OCR扫描批次主
export function addBatch(data) {
  return request({
    url: '/biz/batch',
    method: 'post',
    data: data
  })
}

// 修改OCR扫描批次主
export function updateBatch(data) {
  return request({
    url: '/biz/batch',
    method: 'put',
    data: data
  })
}

// 删除OCR扫描批次主
// export function delBatch(batchId) {
//   return request({
//     url: '/biz/batch/' + batchId,
//     method: 'delete'
//   })
// }


// 1. 查询批次列表
export function listBatch(query) {
  return request({
    url: '/biz/batch/list',
    method: 'get',
    params: query
  })
}

// 2. 批量上传扫表图片 (包含 planId 和 base64Images 数组)
export function uploadBatch(data) {
  return request({
    url: '/biz/batch/uploadBatch',
    method: 'post',
    data: data
  })
}

// 3. 确认并生成台账考勤
export function confirmAndGenerate(batchId, data) {
  return request({
    url: '/biz/batch/confirmAndGenerate/' + batchId,
    method: 'post',
    data: data
  })
}

// 按条件批量删除所有匹配数据
export function deleteAllBatch(query) {
  return request({
    url: '/biz/batch/deleteAll',
    method: 'delete',
    params: query
  })
}

// 4. 删除批次
export function delBatch(batchId) {
  return request({
    url: '/biz/batch/' + batchId,
    method: 'delete'
  })
}

// 5. 获取该批次下的所有原始图片 (log)
export function listBatchLog(query) {
  return request({
    url: '/biz/log/list', 
    method: 'get',
    params: query
  })
}

// 6. 获取该批次下的所有解析结果 (result)
export function listBatchResult(query) {
  return request({
    url: '/biz/result/list',
    method: 'get',
    params: query
  })
}

// 7. 基于源文件重新 OCR 提取元数据（用于历史批次补提应到/实到/培训日期）
export function reextractBatch(batchId) {
  return request({
    url: '/biz/batch/reextract/' + batchId,
    method: 'post'
  })
}