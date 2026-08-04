import { defineStore } from 'pinia'

/**
 * 用户状态
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    role: localStorage.getItem('role') || '',
    userId: localStorage.getItem('userId') || '',
    username: localStorage.getItem('username') || '',
    realName: localStorage.getItem('realName') || ''
  }),
  actions: {
    setLogin(data) {
      this.token = data.token
      this.role = data.role
      this.userId = String(data.userId)
      this.username = data.username
      this.realName = data.realName
      localStorage.setItem('token', data.token)
      localStorage.setItem('role', data.role)
      localStorage.setItem('userId', this.userId)
      localStorage.setItem('username', data.username)
      localStorage.setItem('realName', data.realName || '')
    },
    logout() {
      this.token = ''
      this.role = ''
      this.userId = ''
      this.username = ''
      this.realName = ''
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      localStorage.removeItem('realName')
    }
  }
})
