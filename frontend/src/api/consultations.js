import api from './axiosInstance'

export const getConsultations = (params) =>
  api.get('/consultations', { params }).then((r) => r.data)

export const getConsultation = (id) =>
  api.get(`/consultations/${id}`).then((r) => r.data)
