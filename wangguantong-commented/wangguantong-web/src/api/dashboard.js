import request from '../utils/request'

/** 获取后台首页所需的会员数、电脑数、今日充值等汇总数据。 */
export function getStatistics() {
  return request.get('/dashboard/statistics')
}
