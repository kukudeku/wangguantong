import request from '../utils/request'

export function userLogin(data) {
  return request.post('/user/login', data)
}

export function userRegister(data) {
  return request.post('/user/register', data)
}

export function changeUserPassword(data) {
  return request.post('/user/change-password', data)
}

export function getUserBalanceDetail(memberId) {
  return request.get(`/user/balance-detail/${memberId}`)
}

export function getUserOrders(memberId) {
  return request.get(`/user/orders/${memberId}`)
}

export function getUserOnlineRecords(memberId) {
  return request.get(`/user/online-records/${memberId}`)
}
