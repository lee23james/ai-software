import http from './http'

export function fetchJobList(params = {}) {
  return http.get('/job/list', { params })
}

