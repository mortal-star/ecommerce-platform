import request from '@/utils/request'

export function getProducts(params) {
  return request({ url: '/products', method: 'get', params })
}

export function getProductDetail(id) {
  return request({ url: `/products/${id}`, method: 'get' })
}

export function getProductSpecs(productId) {
  return request({ url: `/product-specs/product/${productId}`, method: 'get' })
}

export function getProductImages(productId) {
  return request({ url: `/product-images/product/${productId}`, method: 'get' })
}
