import request from '../utils/request'

export function addReservation(data) {
  return request.post('/reservation/add', data)
}

export function cancelReservation(id) {
  return request.post(`/reservation/cancel/${id}`)
}

export function startReservation(id) {
  return request.post(`/reservation/start/${id}`)
}

export function getReservationList(params) {
  return request.get('/reservation/list', { params })
}
