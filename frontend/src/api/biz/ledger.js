import request from '@/utils/request'

// 查询培训台账总列表
export function listLedger(query) {
  return request({
    url: '/biz/ledger/list',
    method: 'get',
    params: query
  })
}

// 查询培训台账总详细
export function getLedger(ledgerId) {
  return request({
    url: '/biz/ledger/' + ledgerId,
    method: 'get'
  })
}

// 新增培训台账总
export function addLedger(data) {
  return request({
    url: '/biz/ledger',
    method: 'post',
    data: data
  })
}

// 修改培训台账总
export function updateLedger(data) {
  return request({
    url: '/biz/ledger',
    method: 'put',
    data: data
  })
}

// 按条件批量删除所有匹配数据
export function deleteAllLedger(query) {
  return request({
    url: '/biz/ledger/deleteAll',
    method: 'delete',
    params: query
  })
}

// 删除培训台账总
export function delLedger(ledgerId) {
  return request({
    url: '/biz/ledger/' + ledgerId,
    method: 'delete'
  })
}
