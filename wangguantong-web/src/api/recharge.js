import request from '../utils/request'

export function addRecharge(data) {
  return request.post('/recharge/add', data)
}

export function createRechargePayment(data) {
  return request.post('/recharge/payment', data)
}

export function getRechargeList() {
  return request.get('/recharge/list')
}
