import api from './axiosInstance'

export const signup = (email, password, name) =>
  api.post('/auth/signup', { email, password, name }).then((r) => r.data)

export const login = (email, password) =>
  api.post('/auth/login', { email, password }).then((r) => r.data)
