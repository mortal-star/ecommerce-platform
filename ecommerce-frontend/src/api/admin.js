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

export function adminListProducts(params) {
  return request({ url: '/products/admin', method: 'get', params })
}

export function adminCreateProduct(data) {
  return request({ url: '/products', method: 'post', data })
}

export function adminUpdateProduct(id, data) {
  return request({ url: `/products/${id}`, method: 'put', data })
}

export function adminDeleteProduct(id) {
  return request({ url: `/products/${id}`, method: 'delete' })
}

export function adminUpdateProductStatus(id, status) {
  return request({ url: `/products/${id}/status`, method: 'patch', params: { status } })
}

export function adminListCategories(params) {
  return request({ url: '/categories', method: 'get', params })
}

export function adminCreateCategory(data) {
  return request({ url: '/categories', method: 'post', data })
}

export function adminUpdateCategory(id, data) {
  return request({ url: `/categories/${id}`, method: 'put', data })
}

export function adminDeleteCategory(id) {
  return request({ url: `/categories/${id}`, method: 'delete' })
}

export function adminUpdateCategorySort(sortList) {
  return request({ url: '/categories/sort', method: 'put', data: sortList })
}

export function adminListReviews(params) {
  return request({ url: '/product-reviews/admin', method: 'get', params })
}

export function adminDeleteReview(reviewId) {
  return request({ url: `/product-reviews/${reviewId}`, method: 'delete' })
}

export function adminListOrders(params) {
  return request({ url: '/orders/admin', method: 'get', params })
}

export function adminGetOrderDetail(orderId) {
  return request({ url: `/orders/${orderId}`, method: 'get' })
}

export function adminShipOrder(orderId, data) {
  return request({ url: `/orders/admin/${orderId}/ship`, method: 'patch', data })
}

export function adminCancelOrder(orderId) {
  return request({ url: `/orders/admin/${orderId}/cancel`, method: 'patch' })
}

export function adminProcessRefund(orderId, data) {
  return request({ url: `/orders/admin/${orderId}/refund`, method: 'patch', data })
}

export function adminExportProductsUrl() {
  return '/api/admin/excel/products/export'
}

export function adminExportOrdersUrl(status) {
  return status != null ? `/api/admin/excel/orders/export?status=${status}` : '/api/admin/excel/orders/export'
}

export function adminImportProducts(formData) {
  return request({
    url: '/admin/excel/products/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
