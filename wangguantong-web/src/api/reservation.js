import request from '../utils/request'

export function addReservation(data) {
  return request.post('/reservation/add', data)
}

export function cancelReservation(id, memberId) {
  return request.post(`/reservation/cancel/${id}`, null, {
    params: memberId ? { memberId } : {}
  })
}

export function startReservation(id, memberId) {
  return request.post(`/reservation/start/${id}`, null, {
    params: memberId ? { memberId } : {}
  })
}

export function getReservationList(params) {
  return request.get('/reservation/list', { params })
}
