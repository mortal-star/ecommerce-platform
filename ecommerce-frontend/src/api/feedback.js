import request from '@/utils/request'

export function submitFeedback(data) {
  return request({ url: '/feedback', method: 'post', data })
}
