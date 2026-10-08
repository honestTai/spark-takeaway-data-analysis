import request from '@/utils/request'

// 认证API
export const authApi = {
  // 登录
  login(data) {
    return request({
      url: '/auth/login',
      method: 'post',
      data
    })
  },
  // 刷新Token
  refreshToken() {
    return request({
      url: '/auth/refresh',
      method: 'post'
    })
  },
  // 登出
  logout() {
    return request({
      url: '/auth/logout',
      method: 'post'
    })
  }
}

