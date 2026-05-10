import http from './http'

export function createResume(payload) {
  return http.post('/resume', payload)
}

export function uploadResume(formData) {
  return http.post('/resume/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 12000
  })
}

export function triggerResumeMatch(resumeId, topN = 20) {
  return http.post(`/resume/${resumeId}/match`, null, {
    params: { topN },
    timeout: 12000
  })
}

export function fetchResumeMatches(resumeId) {
  return http.get(`/resume/${resumeId}/matches`)
}

export function fetchResumeHistory(userId) {
  return http.get('/resume/history', { params: { userId } })
}

export function fetchResumeHistoryDetail(resumeId) {
  return http.get(`/resume/${resumeId}`)
}

export function generateJobSelectionAdvice(resumeId) {
  return http.post(`/resume/${resumeId}/job-selection-advice`, null, { timeout: 120000 })
}

export function generateInterestResumeAdvice(resumeId) {
  return http.post(`/resume/${resumeId}/interest-resume-advice`, null, { timeout: 120000 })
}
