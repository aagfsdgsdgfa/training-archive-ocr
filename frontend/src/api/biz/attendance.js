import request from '@/utils/request'

// 查询线上考勤与申诉明细列表
export function listAttendance(query) {
  return request({
    url: '/biz/attendance/list',
    method: 'get',
    params: query
  })
}

// 查询线上考勤与申诉明细详细
export function getAttendance(recordId) {
  return request({
    url: '/biz/attendance/' + recordId,
    method: 'get'
  })
}

// 新增线上考勤与申诉明细
export function addAttendance(data) {
  return request({
    url: '/biz/attendance',
    method: 'post',
    data: data
  })
}

// 修改线上考勤与申诉明细
export function updateAttendance(data) {
  return request({
    url: '/biz/attendance',
    method: 'put',
    data: data
  })
}

// 删除线上考勤与申诉明细
export function delAttendance(recordId) {
  return request({
    url: '/biz/attendance/' + recordId,
    method: 'delete'
  })
}

// 按条件批量删除所有匹配数据
export function deleteAllAttendance(query) {
  return request({
    url: '/biz/attendance/deleteAll',
    method: 'delete',
    params: query
  })
}

// OCR考勤确认归档（将该场次下所有记录正式归档到档案和台账）
export function archiveSession(sessionId) {
  return request({
    url: '/biz/attendance/archiveSession/' + sessionId,
    method: 'post'
  })
}
