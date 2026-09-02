import http from './http'

export const authApi = {
  login: data => http.post('/auth/login', data),
  me: () => http.get('/auth/me')
}

export const equipmentApi = {
  page: params => http.get('/equipment', { params }),
  create: data => http.post('/equipment', data),
  update: (id, data) => http.put(`/equipment/${id}`, data),
  remove: id => http.delete(`/equipment/${id}`),
  categories: () => http.get('/equipment/metadata/categories')
}

export const workOrderApi = {
  page: params => http.get('/work-orders', { params }),
  detail: id => http.get(`/work-orders/${id}`),
  create: data => http.post('/work-orders', data),
  action: (id, action, data = {}) => http.post(`/work-orders/${id}/actions/${action}`, data),
  upload: (id, stage, file) => {
    const form = new FormData(); form.append('file', file)
    return http.post(`/work-orders/${id}/attachments?stage=${stage}`, form)
  }
}

export const dashboardApi = { summary: () => http.get('/dashboard') }
export const userApi = { maintainers: () => http.get('/users/maintainers') }
export const reportApi = { export: () => http.get('/reports/export', { responseType: 'blob' }) }
