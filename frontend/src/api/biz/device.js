import request from '@/utils/request'

// 查询硬件设备与高拍仪管理列表
export function listDevice(query) {
  return request({
    url: '/biz/device/list',
    method: 'get',
    params: query
  })
}

// 查询硬件设备与高拍仪管理详细
export function getDevice(deviceId) {
  return request({
    url: '/biz/device/' + deviceId,
    method: 'get'
  })
}

// 新增硬件设备与高拍仪管理
export function addDevice(data) {
  return request({
    url: '/biz/device',
    method: 'post',
    data: data
  })
}

// 修改硬件设备与高拍仪管理
export function updateDevice(data) {
  return request({
    url: '/biz/device',
    method: 'put',
    data: data
  })
}

// 按条件批量删除所有匹配数据
export function deleteAllDevice(query) {
  return request({
    url: '/biz/device/deleteAll',
    method: 'delete',
    params: query
  })
}

// 删除硬件设备与高拍仪管理
export function delDevice(deviceId) {
  return request({
    url: '/biz/device/' + deviceId,
    method: 'delete'
  })
}
