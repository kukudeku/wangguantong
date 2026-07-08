import request from '../utils/request'

export function getFoodItems(params) {
  return request.get('/food/item/list', { params })
}

export function getAvailableFoodItems() {
  return request.get('/food/item/available')
}

export function addFoodItem(data) {
  return request.post('/food/item/add', data)
}

export function updateFoodItem(data) {
  return request.put('/food/item/update', data)
}

export function deleteFoodItem(id) {
  return request.delete(`/food/item/delete/${id}`)
}

export function addFoodOrder(data) {
  return request.post('/food/order/add', data)
}

export function cancelFoodOrder(id) {
  return request.post(`/food/order/cancel/${id}`)
}

export function getFoodOrders(params) {
  return request.get('/food/order/list', { params })
}
