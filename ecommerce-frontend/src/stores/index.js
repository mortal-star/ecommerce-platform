import { createPinia, defineStore } from 'pinia'
import { clearToken, getToken, setToken } from '@/utils/auth'

const pinia = createPinia()

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    userInfo: null,
    roles: []
  }),
  getters: {
    isLoggedIn: state => Boolean(state.token),
    isAdmin: state => state.roles.includes('ADMIN') || state.roles.includes('ROLE_ADMIN')
  },
  actions: {
    setLoginInfo({ token, userInfo, roles = [] }) {
      this.token = token
      this.userInfo = userInfo
      this.roles = roles
      setToken(token)
    },
    setUserInfo(userInfo) {
      this.userInfo = userInfo
    },
    logout() {
      this.token = ''
      this.userInfo = null
      this.roles = []
      clearToken()
    }
  }
})

export default pinia
