import request from '@/utils/request'

// 查询年度培训计划列表
export function listPlan(query) {
  return request({
    url: '/biz/plan/list',
    method: 'get',
    params: query
  })
}

// 查询年度培训计划详细
export function getPlan(planId) {
  return request({
    url: '/biz/plan/' + planId,
    method: 'get'
  })
}

// 新增年度培训计划
export function addPlan(data) {
  return request({
    url: '/biz/plan',
    method: 'post',
    data: data
  })
}

// 修改年度培训计划
export function updatePlan(data) {
  return request({
    url: '/biz/plan',
    method: 'put',
    data: data
  })
}

// 删除年度培训计划
export function delPlan(planId) {
  return request({
    url: '/biz/plan/' + planId,
    method: 'delete'
  })
}

// 从Excel导入年度培训计划
export function importPlanExcel(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/biz/plan/importExcel',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// Excel 解析预览（不入库，供人工校对）
export function previewExcel(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/biz/plan/previewExcel',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 确认导入（校对后的数据正式入库）
export function confirmExcelImport(plans) {
  return request({
    url: '/biz/plan/confirmExcelImport',
    method: 'post',
    data: plans
  })
}

// 图片 OCR 扫描预览（高拍仪/拍照/本地图片）
// data: { base64Images: string[], scanLevel: 'dept' | 'company' }
export function previewPlanImage(data) {
  return request({
    url: '/biz/plan/previewPlanImage',
    method: 'post',
    data
  })
}

// 导出年度培训计划导入模板（支持指定年份）
export function exportTemplate(year) {
  return request({
    url: '/biz/plan/exportTemplate',
    method: 'get',
    params: { year },
    responseType: 'blob'
  })
}

// 按条件批量删除所有匹配数据
export function deleteAllPlan(query) {
  return request({
    url: '/biz/plan/deleteAll',
    method: 'delete',
    params: query
  })
}

// 导出部门级培训计划（模板格式）
export function exportDeptPlan(year) {
  return request({
    url: '/biz/plan/exportDeptPlan',
    method: 'post',
    params: { year },
    responseType: 'blob'
  })
}

// 导出公司级培训计划（模板格式）
export function exportCompanyPlan(year) {
  return request({
    url: '/biz/plan/exportCompanyPlan',
    method: 'post',
    params: { year },
    responseType: 'blob'
  })
}
