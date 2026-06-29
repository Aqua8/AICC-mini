import axios from 'axios'
import { getToken, clearAuth } from '../store/authStore'

const instance = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 요청마다 JWT 헤더 자동 삽입
instance.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 401 응답 시 로그아웃 처리
instance.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.status === 401) {
      clearAuth()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export default instance
