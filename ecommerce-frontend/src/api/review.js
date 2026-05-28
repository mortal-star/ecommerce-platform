import request from '@/utils/request'

export function listProductReviews(productId, params) {
  return request({ url: `/product-reviews/product/${productId}`, method: 'get', params })
}

export function submitProductReview(data) {
  return request({ url: '/product-reviews', method: 'post', data })
}
