import request from '../utils/request'

/** 查询用户列表，可传姓名、身份证号、手机号等筛选条件。 */
export function getMemberList(params) {
  return request.get('/member/list', { params })
}

/** 后台新增用户。 */
export function addMember(data) {
  return request.post('/member/add', data)
}

/** 后台修改用户资料。 */
export function updateMember(data) {
  return request.put('/member/update', data)
}

/** 删除指定用户。 */
export function deleteMember(id) {
  return request.delete(`/member/delete/${id}`)
}
