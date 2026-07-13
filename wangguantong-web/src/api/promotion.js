import request from '../utils/request'

export function getPromotionOverview(memberId) {
  return request.get(`/promotion/overview/${memberId}`)
}

export function getPromotionAdminOverview() {
  return request.get('/promotion/admin/overview')
}

export function updatePromotionRule(data) {
  return request.put('/promotion/rule', data)
}
