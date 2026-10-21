import api from './axiosInstance'

// 로그인한 누구나 읽을 수 있는 카테고리 목록 (관리는 api/admin.js, ADMIN 전용)
export const getCategories = () =>
  api.get('/categories').then((r) => r.data)
