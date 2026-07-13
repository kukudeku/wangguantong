import request from '../utils/request'

/** 创建电脑预约。 */
export function addReservation(data) {
  return request.post('/reservation/add', data)
}

/** 取消指定预约并释放机位。 */
export function cancelReservation(id) {
  return request.post(`/reservation/cancel/${id}`)
}

/** 从有效预约直接办理上机。 */
export function startReservation(id) {
  return request.post(`/reservation/start/${id}`)
}

/** 查询预约记录，可按会员、电脑和状态筛选。 */
export function getReservationList(params) {
  return request.get('/reservation/list', { params })
}
