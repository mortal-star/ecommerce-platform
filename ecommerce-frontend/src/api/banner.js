import request from '@/utils/request'

export function getBanners(params) {
  return request({ url: '/banners', method: 'get', params })
}
