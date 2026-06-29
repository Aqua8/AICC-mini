import api from './axiosInstance'

export const createRoom = () =>
  api.post('/chat/rooms').then((r) => r.data)

export const getRoom = (roomId) =>
  api.get(`/chat/rooms/${roomId}`).then((r) => r.data)

export const endRoom = (roomId) =>
  api.post(`/chat/rooms/${roomId}/end`).then((r) => r.data)
