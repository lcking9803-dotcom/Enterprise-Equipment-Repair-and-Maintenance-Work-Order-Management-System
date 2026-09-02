import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({ baseURL: import.meta.env.VITE_API_BASE || '/api', timeout: 15000 })

http.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  response => response,
  error => {
    const message = error.response?.data?.message || '请求失败，请检查服务状态'
    ElMessage.error(message)
    if (error.response?.status === 401) {
      localStorage.clear()
      if (location.pathname !== '/login') location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default http

