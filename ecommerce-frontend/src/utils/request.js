import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { clearToken, getAuthorizationHeader } from '@/utils/auth'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

request.interceptors.request.use(
  config => {
    const authorization = getAuthorizationHeader()
    if (authorization) {
      config.headers.Authorization = authorization
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const data = response.data
    if (data && Object.prototype.hasOwnProperty.call(data, 'code')) {
      if (data.code === 200) {
        return data.data
      }
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    return data
  },
  error => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络异常'
    if (status === 401) {
      clearToken()
      router.replace({ name: 'Login' })
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
