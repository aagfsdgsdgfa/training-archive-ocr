import request from '@/utils/request'

// 查询标准课程资源库列表
export function listCourse(query) {
  return request({
    url: '/biz/course/list',
    method: 'get',
    params: query
  })
}

// 查询标准课程资源库详细
export function getCourse(courseId) {
  return request({
    url: '/biz/course/' + courseId,
    method: 'get'
  })
}

// 新增标准课程资源库
export function addCourse(data) {
  return request({
    url: '/biz/course',
    method: 'post',
    data: data
  })
}

// 修改标准课程资源库
export function updateCourse(data) {
  return request({
    url: '/biz/course',
    method: 'put',
    data: data
  })
}

// 按条件批量删除所有匹配数据
export function deleteAllCourse(query) {
  return request({
    url: '/biz/course/deleteAll',
    method: 'delete',
    params: query
  })
}

// 删除标准课程资源库
export function delCourse(courseId) {
  return request({
    url: '/biz/course/' + courseId,
    method: 'delete'
  })
}
