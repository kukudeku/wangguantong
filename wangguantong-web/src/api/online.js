import request from '../utils/request'

export function startOnline(data) {
  return request.post('/online/start', data)
}

export function endOnline(recordId) {
  return request.post(`/online/end/${recordId}`)
}

export function getOnlineList(params) {
  return request.get('/online/list', { params })
}

export function getRunningOnlineList() {
  return request.get('/online/running')
}
