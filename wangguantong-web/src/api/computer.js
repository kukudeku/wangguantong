import request from '../utils/request'

export function getComputerList(params) {
  return request.get('/computer/list', { params })
}

export function getFreeComputers() {
  return request.get('/computer/free')
}

export function addComputer(data) {
  return request.post('/computer/add', data)
}

export function updateComputer(data) {
  return request.put('/computer/update', data)
}

export function deleteComputer(id) {
  return request.delete(`/computer/delete/${id}`)
}
