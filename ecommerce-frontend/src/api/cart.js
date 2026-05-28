import request from '@/utils/request'

export function listCart() {
  return request({ url: '/cart', method: 'get' })
}

export function addCart(data) {
  return request({ url: '/cart', method: 'post', data })
}

export function updateCartQuantity(cartId, data) {
  return request({ url: `/cart/${cartId}/quantity`, method: 'put', data })
}

export function deleteCart(cartId) {
  return request({ url: `/cart/${cartId}`, method: 'delete' })
}

export function updateCartSelected(data) {
  return request({ url: '/cart/selected', method: 'patch', data })
}
