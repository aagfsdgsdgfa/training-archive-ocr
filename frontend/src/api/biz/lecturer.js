import request from '@/utils/request'

// 查询培训讲师资质库列表
export function listLecturer(query) {
  return request({
    url: '/biz/lecturer/list',
    method: 'get',
    params: query
  })
}

// 查询培训讲师资质库详细
export function getLecturer(lecturerId) {
  return request({
    url: '/biz/lecturer/' + lecturerId,
    method: 'get'
  })
}

// 新增培训讲师资质库
export function addLecturer(data) {
  return request({
    url: '/biz/lecturer',
    method: 'post',
    data: data
  })
}

// 修改培训讲师资质库
export function updateLecturer(data) {
  return request({
    url: '/biz/lecturer',
    method: 'put',
    data: data
  })
}

// 按条件批量删除所有匹配数据
export function deleteAllLecturer(query) {
  return request({
    url: '/biz/lecturer/deleteAll',
    method: 'delete',
    params: query
  })
}

// 删除培训讲师资质库
export function delLecturer(lecturerId) {
  return request({
    url: '/biz/lecturer/' + lecturerId,
    method: 'delete'
  })
}
