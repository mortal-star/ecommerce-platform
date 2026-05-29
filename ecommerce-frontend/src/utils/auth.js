const TOKEN_KEY = 'ECOMMERCE_TOKEN'
const USER_INFO_KEY = 'ECOMMERCE_USER_INFO'
const ROLES_KEY = 'ECOMMERCE_ROLES'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_INFO_KEY)
  localStorage.removeItem(ROLES_KEY)
}

export function getAuthorizationHeader() {
  const token = getToken()
  return token ? `Bearer ${token}` : ''
}

export function getStoredUserInfo() {
  try {
    const raw = localStorage.getItem(USER_INFO_KEY)
    return raw ? JSON.parse(raw) : null
  } catch (_) {
    return null
  }
}

export function setStoredUserInfo(userInfo) {
  if (userInfo) {
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
  } else {
    localStorage.removeItem(USER_INFO_KEY)
  }
}

export function getStoredRoles() {
  try {
    const raw = localStorage.getItem(ROLES_KEY)
    return raw ? JSON.parse(raw) : []
  } catch (_) {
    return []
  }
}

export function setStoredRoles(roles) {
  if (Array.isArray(roles) && roles.length > 0) {
    localStorage.setItem(ROLES_KEY, JSON.stringify(roles))
  } else {
    localStorage.removeItem(ROLES_KEY)
  }
}
