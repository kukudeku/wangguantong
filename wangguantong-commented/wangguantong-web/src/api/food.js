import request from '../utils/request'

/** 后台按名称、分类、状态查询全部商品。 */
export function getFoodItems(params) {
  return request.get('/food/item/list', { params })
}

/** 用户点餐或后台下单时，只获取“上架”商品。 */
export function getAvailableFoodItems() {
  return request.get('/food/item/available')
}

/** 新增点餐商品。 */
export function addFoodItem(data) {
  return request.post('/food/item/add', data)
}

/** 修改商品资料、分类、价格或上下架状态。 */
export function updateFoodItem(data) {
  return request.put('/food/item/update', data)
}

/** 删除指定商品。 */
export function deleteFoodItem(id) {
  return request.delete(`/food/item/delete/${id}`)
}

/** 创建点餐订单；会员订单会由后端扣除余额。 */
export function addFoodOrder(data) {
  return request.post('/food/order/add', data)
}

/** 取消订单；满足条件的会员订单由后端退款。 */
export function cancelFoodOrder(id) {
  return request.post(`/food/order/cancel/${id}`)
}

/** 后台确认商品已经交付，将订单标记为已完成。 */
export function completeFoodOrder(id) {
  return request.post(`/food/order/complete/${id}`)
}

/** 查询点餐订单，可按顾客姓名和订单状态筛选。 */
export function getFoodOrders(params) {
  return request.get('/food/order/list', { params })
}
