import request from '../utils/request'

export function getRepairList(params) {
  return request.get('/repair/list', { params })
}

export function reportRepair(data) {
  return request.post('/repair/report', data)
}

export function processRepair(data) {
  return request.put('/repair/process', data)
}
