import api from './axiosInstance'

export const getConsultations = (params) =>
  api.get('/consultations', { params }).then((r) => r.data)

export const getConsultation = (id) =>
  api.get(`/consultations/${id}`).then((r) => r.data)

export const updateConsultationStatus = (id, status) =>
  api.patch(`/consultations/${id}/status`, { status }).then((r) => r.data)

export const updateConsultationCategory = (id, categoryId) =>
  api.patch(`/consultations/${id}/category`, { categoryId }).then((r) => r.data)
