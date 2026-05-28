import request from '@/utils/request'

export function createOrder(data) {
  return request({ url: '/orders', method: 'post', data })
}

export function payOrder(orderId, data) {
  return request({ url: `/orders/${orderId}/pay`, method: 'post', data })
}

export function listOrders(params) {
  return request({ url: '/orders', method: 'get', params })
}

export function getOrderDetail(orderId) {
  return request({ url: `/orders/${orderId}`, method: 'get' })
}

export function cancelOrder(orderId) {
  return request({ url: `/orders/${orderId}/cancel`, method: 'patch' })
}

export function confirmReceive(orderId) {
  return request({ url: `/orders/${orderId}/receive`, method: 'patch' })
}

export function applyRefund(orderId) {
  return request({ url: `/orders/${orderId}/refund`, method: 'patch' })
}
