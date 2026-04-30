import http from './http'

export function saveInterestJobs(payload) {
  return http.post('/user/interest-jobs', payload)
}

export function fetchInterestJobs(userId) {
  return http.get('/user/interest-jobs', { params: { userId } })
}
