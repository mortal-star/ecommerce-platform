import { createPinia, defineStore } from 'pinia'
import {
  clearToken,
  getStoredRoles,
  getStoredUserInfo,
  getToken,
  setStoredRoles,
  setStoredUserInfo,
  setToken
} from '@/utils/auth'
import { getProfile } from '@/api/user'

const pinia = createPinia()

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    userInfo: getStoredUserInfo(),
    roles: getStoredRoles(),
    profileLoading: false
  }),
  getters: {
    isLoggedIn: state => Boolean(state.token),
    isAdmin: state =>
      state.roles.includes('ADMIN') ||
      state.roles.includes('ROLE_ADMIN') ||
      state.userInfo?.userType === 1
  },
  actions: {
    setLoginInfo({ token, userInfo, roles = [] }) {
      this.token = token
      this.userInfo = userInfo
      this.roles = roles
      setToken(token)
      setStoredUserInfo(userInfo)
      setStoredRoles(roles)
    },
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      setStoredUserInfo(userInfo)
    },
    async ensureProfile() {
      if (!this.token) return null
      if (this.userInfo && this.roles.length > 0) return this.userInfo
      if (this.profileLoading) return this.userInfo
      this.profileLoading = true
      try {
        const profile = await getProfile()
        if (profile) {
          this.userInfo = profile
          setStoredUserInfo(profile)
          if (Array.isArray(profile.roles) && profile.roles.length > 0) {
            this.roles = profile.roles
            setStoredRoles(profile.roles)
          } else if (profile.userType === 1 && !this.roles.includes('ADMIN')) {
            this.roles = ['ADMIN']
            setStoredRoles(['ADMIN'])
          } else if (profile.userType === 0 && this.roles.length === 0) {
            this.roles = ['USER']
            setStoredRoles(['USER'])
          }
        }
        return this.userInfo
      } catch (_) {
        return null
      } finally {
        this.profileLoading = false
      }
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
