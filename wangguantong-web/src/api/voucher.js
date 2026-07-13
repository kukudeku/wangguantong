import request from '../utils/request'

export function getVoucherList(params) {
  return request.get('/voucher/list', { params })
}

export function addVoucher(data) {
  return request.post('/voucher/add', data)
}

export function redeemVoucher(data) {
  return request.post('/voucher/redeem', data)
}

export function updateVoucherStatus(data) {
  return request.put('/voucher/status', data)
}
