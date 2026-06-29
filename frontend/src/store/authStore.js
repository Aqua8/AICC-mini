// 간단한 localStorage 기반 인증 상태 관리
// React Context 없이 직접 읽기/쓰기 + 이벤트로 동기화

const STORAGE_KEY = 'aicc_auth'

export function getAuth() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function setAuth({ token, userId, email, name, role }) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify({ token, userId, email, name, role }))
  window.dispatchEvent(new Event('auth-change'))
}

export function clearAuth() {
  localStorage.removeItem(STORAGE_KEY)
  window.dispatchEvent(new Event('auth-change'))
}

export function getToken() {
  return getAuth()?.token ?? null
}

export function isAdmin() {
  return getAuth()?.role === 'ADMIN'
}
