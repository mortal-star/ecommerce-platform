import request from '@/utils/request'

export function listFavorites(params) {
  return request({ url: '/favorites', method: 'get', params })
}

export function addFavorite(productId) {
  return request({ url: `/favorites/${productId}`, method: 'post' })
}

export function cancelFavorite(productId) {
  return request({ url: `/favorites/${productId}`, method: 'delete' })
}
