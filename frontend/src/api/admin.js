import api from './axiosInstance'

// 카테고리
export const getCategories = () =>
  api.get('/admin/categories').then((r) => r.data)

export const createCategory = (data) =>
  api.post('/admin/categories', data).then((r) => r.data)

export const updateCategory = (id, data) =>
  api.put(`/admin/categories/${id}`, data).then((r) => r.data)

export const deleteCategory = (id) =>
  api.delete(`/admin/categories/${id}`)

// FAQ
export const getFaqs = () =>
  api.get('/admin/faq').then((r) => r.data)

export const createFaq = (data) =>
  api.post('/admin/faq', data).then((r) => r.data)

export const updateFaq = (id, data) =>
  api.put(`/admin/faq/${id}`, data).then((r) => r.data)

export const deleteFaq = (id) =>
  api.delete(`/admin/faq/${id}`)
