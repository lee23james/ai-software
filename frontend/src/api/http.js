import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 60000
})

http.interceptors.response.use((response) => {
  const payload = response.data
  if (
    payload &&
    typeof payload === 'object' &&
    typeof payload.code === 'number' &&
    payload.code !== 200
  ) {
    const err = new Error(payload.message || '请求失败')
    err.response = { data: { message: payload.message, code: payload.code } }
    return Promise.reject(err)
  }
  return payload
})

export default http

