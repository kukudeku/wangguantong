import request from '../utils/request'

/** 使用会员编号和电脑编号开始上机。 */
export function startOnline(data) {
  return request.post('/online/start', data)
}

/** 结束一条进行中的上机记录。 */
export function endOnline(recordId) {
  return request.post(`/online/end/${recordId}`)
}

/** 按条件查询当前或历史上机记录。 */
export function getOnlineList(params) {
  return request.get('/online/list', { params })
}

/** 只获取状态为“进行中”的上机记录。 */
export function getRunningOnlineList() {
  return request.get('/online/running')
}
