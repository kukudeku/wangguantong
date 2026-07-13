import request from '../utils/request'

/** 提交管理员用户名和密码。 */
export function login(data) {
  return request.post('/admin/login', data)
}
