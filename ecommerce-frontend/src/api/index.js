import request from '@/utils/request'

export function getBanners(params) {
  return request({
    url: '/banners',
    method: 'get',
    params
  })
}

export function getProducts(params) {
  return request({
    url: '/products',
    method: 'get',
    params
  })
}

export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}
