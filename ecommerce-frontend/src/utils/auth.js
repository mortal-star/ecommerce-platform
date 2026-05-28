const TOKEN_KEY = 'ECOMMERCE_TOKEN'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getAuthorizationHeader() {
  const token = getToken()
  return token ? `Bearer ${token}` : ''
}
