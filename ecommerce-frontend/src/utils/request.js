import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { getAuthorizationHeader } from '@/utils/auth'

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

function isAdminRoute() {
  const path = router.currentRoute.value?.path || ''
  return path.startsWith('/admin')
}

function redirectToLogin() {
  const current = router.currentRoute.value
  const target = isAdminRoute() ? 'AdminLogin' : 'Login'
  if (current?.name === target) return
  router.replace({
    name: target,
    query: current?.fullPath && current.fullPath !== '/' ? { redirect: current.fullPath } : undefined
  })
}

async function resetUserStore() {
  try {
    const { useUserStore } = await import('@/stores')
    useUserStore().logout()
  } catch (_) {
    // store not initialized yet; ignore
  }
}

request.interceptors.response.use(
  response => {
    const data = response.data
    if (data && Object.prototype.hasOwnProperty.call(data, 'code')) {
      if (data.code === 200) {
        return data.data
      }
      if (data.code === 401) {
        resetUserStore().finally(redirectToLogin)
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
      resetUserStore().finally(redirectToLogin)
    }
    if (status !== 401) {
      ElMessage.error(message)
    } else {
      ElMessage.warning(message || '登录已过期，请重新登录')
    }
    return Promise.reject(error)
  }
)

export default request
