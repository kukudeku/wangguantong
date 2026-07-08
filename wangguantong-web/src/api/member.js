import request from '../utils/request'

export function getMemberList(params) {
  return request.get('/member/list', { params })
}

export function addMember(data) {
  return request.post('/member/add', data)
}

export function updateMember(data) {
  return request.put('/member/update', data)
}

export function deleteMember(id) {
  return request.delete(`/member/delete/${id}`)
}
