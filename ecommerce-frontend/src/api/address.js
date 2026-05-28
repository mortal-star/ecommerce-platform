import request from '@/utils/request'

export function listAddresses() {
  return request({ url: '/addresses', method: 'get' })
}

export function getAddress(addressId) {
  return request({ url: `/addresses/${addressId}`, method: 'get' })
}

export function createAddress(data) {
  return request({ url: '/addresses', method: 'post', data })
}

export function updateAddress(addressId, data) {
  return request({ url: `/addresses/${addressId}`, method: 'put', data })
}

export function deleteAddress(addressId) {
  return request({ url: `/addresses/${addressId}`, method: 'delete' })
}

export function setDefaultAddress(addressId) {
  return request({ url: `/addresses/${addressId}/default`, method: 'put' })
}
