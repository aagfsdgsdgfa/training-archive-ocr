import request from '@/utils/request'

// 查询培训地点与会议室管理列表
export function listLocation(query) {
  return request({
    url: '/biz/location/list',
    method: 'get',
    params: query
  })
}

// 查询培训地点与会议室管理详细
export function getLocation(locationId) {
  return request({
    url: '/biz/location/' + locationId,
    method: 'get'
  })
}

// 新增培训地点与会议室管理
export function addLocation(data) {
  return request({
    url: '/biz/location',
    method: 'post',
    data: data
  })
}

// 修改培训地点与会议室管理
export function updateLocation(data) {
  return request({
    url: '/biz/location',
    method: 'put',
    data: data
  })
}

// 按条件批量删除所有匹配数据
export function deleteAllLocation(query) {
  return request({
    url: '/biz/location/deleteAll',
    method: 'delete',
    params: query
  })
}

// 删除培训地点与会议室管理
export function delLocation(locationId) {
  return request({
    url: '/biz/location/' + locationId,
    method: 'delete'
  })
}
