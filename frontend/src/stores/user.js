import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    profile: {
      username: 'student',
      role: 'student'
    }
  }),
  actions: {
    setToken(token) {
      this.token = token
    },
    logout() {
      this.token = ''
      this.profile = {
        username: '',
        role: ''
      }
    }
  }
})

