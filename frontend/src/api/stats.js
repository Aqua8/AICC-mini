import api from './axiosInstance'

export const getDailyStats = (from, to) =>
  api.get('/stats/daily', { params: { from, to } }).then((r) => r.data)

export const getCategoryStats = () =>
  api.get('/stats/category').then((r) => r.data)

export const getSummaryStats = () =>
  api.get('/stats/summary').then((r) => r.data)
