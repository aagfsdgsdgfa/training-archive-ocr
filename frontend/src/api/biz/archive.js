import request from '@/utils/request'

// 查询个人培训档案明细列表
export function listArchive(query) {
  return request({
    url: '/biz/archive/list',
    method: 'get',
    params: query
  })
}

// 查询个人培训档案明细详细
export function getArchive(archiveId) {
  return request({
    url: '/biz/archive/' + archiveId,
    method: 'get'
  })
}

// 新增个人培训档案明细
export function addArchive(data) {
  return request({
    url: '/biz/archive',
    method: 'post',
    data: data
  })
}

// 修改个人培训档案明细
export function updateArchive(data) {
  return request({
    url: '/biz/archive',
    method: 'put',
    data: data
  })
}

// 按条件批量删除所有匹配数据
export function deleteAllArchive(query) {
  return request({
    url: '/biz/archive/deleteAll',
    method: 'delete',
    params: query
  })
}

// 删除个人培训档案明细
export function delArchive(archiveId) {
  return request({
    url: '/biz/archive/' + archiveId,
    method: 'delete'
  })
}
