import request from '@/utils/request'

export function getDashboardStats() {
  return request({ url: '/admin/dashboard/stats', method: 'get' })
}

export function getSalesTrend(params) {
  return request({ url: '/admin/dashboard/sales-trend', method: 'get', params })
}

export function getOrderStatusStats() {
  return request({ url: '/admin/dashboard/order-status', method: 'get' })
}

export function getTopProducts(params) {
  return request({ url: '/admin/dashboard/top-products', method: 'get', params })
}

export function listAdminUsers(params) {
  return request({ url: '/admin/users', method: 'get', params })
}

export function getAdminUserDetail(id) {
  return request({ url: `/admin/users/${id}`, method: 'get' })
}

export function updateAdminUserStatus(id, status) {
  return request({ url: `/admin/users/${id}/status`, method: 'patch', data: { status } })
}

export function deleteAdminUser(id) {
  return request({ url: `/admin/users/${id}`, method: 'delete' })
}
