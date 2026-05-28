import request from '@/utils/request'

export function sendEmailCode(data) {
  return request({ url: '/auth/email-code', method: 'post', data })
}

export function register(data) {
  return request({ url: '/auth/register', method: 'post', data })
}

export function login(data) {
  return request({ url: '/auth/login', method: 'post', data })
}

export function adminLogin(data) {
  return request({ url: '/auth/admin/login', method: 'post', data })
}

export function resetPassword(data) {
  return request({ url: '/auth/password/reset', method: 'post', data })
}
