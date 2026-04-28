import http from './http'

export function fetchCityJobCount() {
  return http.get('/analysis/city-job-count')
}

