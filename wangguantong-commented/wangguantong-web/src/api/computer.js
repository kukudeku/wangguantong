import request from '../utils/request'

/** 按区域、状态等查询电脑列表。params 会被 Axios 转成网址查询参数。 */
export function getComputerList(params) {
  return request.get('/computer/list', { params })
}

/** 查询可用于上机或预约的空闲电脑。 */
export function getFreeComputers() {
  return request.get('/computer/free')
}

/** 新增电脑机位。 */
export function addComputer(data) {
  return request.post('/computer/add', data)
}

/** 修改电脑资料或状态。 */
export function updateComputer(data) {
  return request.put('/computer/update', data)
}

/** 按编号删除电脑；模板字符串会把 id 拼进 URL。 */
export function deleteComputer(id) {
  return request.delete(`/computer/delete/${id}`)
}
