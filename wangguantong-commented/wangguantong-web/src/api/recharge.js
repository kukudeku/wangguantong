import request from '../utils/request'

/** 提交用户编号和充值金额。 */
export function addRecharge(data) {
  return request.post('/recharge/add', data)
}

/** 获取全部充值明细，默认由后端按最新记录排序。 */
export function getRechargeList() {
  return request.get('/recharge/list')
}
