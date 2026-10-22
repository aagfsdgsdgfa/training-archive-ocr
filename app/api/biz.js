import request from '@/utils/request'

// 获取台账列表 (用于统计总人次和最新动态)
export function listLedger(query) {
  return request({
    url: '/biz/ledger/list',
    method: 'get',
    params: query
  })
}

// 获取计划列表 (用于预警)
export function listPlan(query) {
  return request({
    url: '/biz/plan/list',
    method: 'get',
    params: query
  })
}

export function listBatch(query) {
  return request({
    url: '/biz/batch/list',
    method: 'get',
    params: query
  })
}

export function listArchive(query) {
  return request({
    url: '/biz/archive/list',
    method: 'get',
    params: query
  })
} 

export function exportArchive(query) {
  return request({
    url: '/biz/archive/exportTemplate"',
    method: 'post',
    data: query
  })
}

// // 获取年度计划列表 (用于表单下拉选择关联)
// export function listPlan(query) {
//   return request({
//     url: '/biz/plan/list',
//     method: 'get',
//     params: query
//   })
// }

// // 提交手机拍摄的 Base64 图片，创建 OCR 批次入库
// export function uploadBatch(data) {
//   return request({
//     url: '/biz/batch/upload',
//     method: 'post',
//     data: data
//   })
// }

// 获取培训台账列表
// export function listLedger(query) {
//   return request({
//     url: '/biz/ledger/list',
//     method: 'get',
//     params: query
//   })
// }

export function getLedger(ledgerId) {
  return request({
    url: '/biz/ledger/' + ledgerId,
    method: 'get'
  })
}

export function getArchive(archiveId) {
  return request({
    url: '/biz/archive/' + archiveId,
    method: 'get'
  })
}

export function listMyArchive(query) {
  return request({
    url: '/biz/archive/myList', 
    method: 'get',
    params: query
  })
}

// export function getArchive(archiveId) {
//   return request({
//     url: '/biz/archive/' + archiveId,
//     method: 'get'
//   })
// }

// 获取培训计划详细信息
export function getPlan(planId) {
  return request({
    url: '/biz/plan/' + planId,
    method: 'get'
  })
}

// 获取批次详细信息
export function getBatch(batchId) {
  return request({
    url: '/biz/batch/' + batchId,
    method: 'get'
  })
}

export function listNotice(query) {
  return request({
    url: '/system/notice/list',
    method: 'get',
    params: query
  })
}

export function getNotice(noticeId) {
  return request({
    url: '/system/notice/' + noticeId,
    method: 'get'
  })
}

export function uploadBatch(data) {
  return request({
    url: '/biz/batch/uploadBatch',
    method: 'post',
    data: data
  })
}


export function listBatchResult(query) {
  return request({ url: '/biz/result/list', method: 'get', params: query })
}

// 修改OCR扫描批次主
export function updateBatch(data) {
  return request({ url: '/biz/batch', method: 'put', data: data })
}

// 确认并生成台账考勤 
export function confirmAndGenerate(batchId, data) {
  return request({ url: '/biz/batch/confirmAndGenerate/' + batchId, method: 'post', data: data })
}

// 获取该批次下的所有原始图片
export function listBatchLog(query) {
  return request({
    url: '/biz/log/list', 
    method: 'get',
    params: query
  })
}