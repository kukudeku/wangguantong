import request from '../utils/request'

export function getCouponTemplates(params) {
  return request.get('/coupon/template/list', { params })
}

export function saveCouponTemplate(data) {
  return request.post('/coupon/template/save', data)
}

export function updateCouponTemplateStatus(data) {
  return request.put('/coupon/template/status', data)
}

export function getSignInRules() {
  return request.get('/coupon/rule/list')
}

export function saveSignInRule(data) {
  return request.post('/coupon/rule/save', data)
}

export function deleteSignInRule(id) {
  return request.delete(`/coupon/rule/${id}`)
}

export function getSignInStatus(memberId) {
  return request.get(`/coupon/sign-in/status/${memberId}`)
}

export function userSignIn(memberId) {
  return request.post(`/coupon/sign-in/${memberId}`)
}

export function getUserCoupons(memberId, params = {}) {
  return request.get(`/coupon/user/${memberId}`, { params })
}
