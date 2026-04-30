import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    userId: Number(localStorage.getItem('userId') || 0),
    profile: {
      username: localStorage.getItem('username') || '',
      phone: localStorage.getItem('phone') || '',
      email: localStorage.getItem('email') || '',
      role: 'student'
    }
  }),
  actions: {
    setUser(user) {
      this.userId = user.userId
      this.profile = {
        username: user.username || '',
        phone: user.phone || '',
        email: user.email || '',
        role: 'student'
      }
      localStorage.setItem('userId', String(user.userId || 0))
      localStorage.setItem('username', user.username || '')
      localStorage.setItem('phone', user.phone || '')
      localStorage.setItem('email', user.email || '')
    },
    setToken(token) {
      this.token = token
    },
    logout() {
      this.token = ''
      this.userId = 0
      this.profile = {
        username: '',
        phone: '',
        email: '',
        role: ''
      }
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      localStorage.removeItem('phone')
      localStorage.removeItem('email')
    }
  }
})

