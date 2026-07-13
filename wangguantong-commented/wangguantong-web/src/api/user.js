import request from '../utils/request'

/** 用户使用身份证号和密码登录。 */
export function userLogin(data) {
  return request.post('/user/login', data)
}

/** 用户端注册新账号。 */
export function userRegister(data) {
  return request.post('/user/register', data)
}

/** 登录用户修改自己的密码。 */
export function changeUserPassword(data) {
  return request.post('/user/change-password', data)
}

/** 查询用户的充值记录和余额消费明细。 */
export function getUserBalanceDetail(memberId) {
  return request.get(`/user/balance-detail/${memberId}`)
}

/** 查询用户自己的点餐订单。 */
export function getUserOrders(memberId) {
  return request.get(`/user/orders/${memberId}`)
}

/** 查询用户自己的当前及历史上机记录。 */
export function getUserOnlineRecords(memberId) {
  return request.get(`/user/online-records/${memberId}`)
}
