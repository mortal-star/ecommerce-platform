import request from '@/utils/request'

export function listNotices(params) {
  return request({ url: '/notices', method: 'get', params })
}
