import http from './http'

export function fetchAdminDashboard() {
  return http.get('/admin/dashboard')
}

export function fetchAdminJobList(params = {}) {
  return http.get('/admin/job-list', { params })
}

export function getAdminJob(id) {
  return http.get(`/admin/job/${id}`)
}

export function addAdminJob(data) {
  return http.post('/admin/job', data)
}

export function updateAdminJob(id, data) {
  return http.put(`/admin/job/${id}`, data)
}

export function deleteAdminJob(id) {
  return http.delete(`/admin/job/${id}`)
}

export function toggleJobStatus(id, status) {
  return http.patch(`/admin/job/${id}/status`, { status })
}

export function importJobs(file) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/admin/job/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function fetchAdminUserList(params = {}) {
  return http.get('/admin/user-list', { params })
}

export function toggleUserStatus(id, status) {
  return http.patch(`/admin/user/${id}/status`, { status })
}

export function deleteAdminUser(id) {
  return http.delete(`/admin/user/${id}`)
}

export function fetchAdminLogList(params = {}) {
  return http.get('/admin/log-list', { params })
}

export function cleanAdminData(type) {
  return http.post('/admin/clean-data', { type })
}
