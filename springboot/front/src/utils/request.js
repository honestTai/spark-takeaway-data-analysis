import axios from 'axios'
import { Message } from 'element-ui'
import router from '@/router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API || '/api',
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    NProgress.start()
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    NProgress.done()
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    NProgress.done()
    const res = response.data

    if (res.code !== 200) {
      // 401未授权，跳转到登录页
      if (res.code === 401) {
        Message.error('登录已过期，请重新登录')
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      } else {
        Message.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    } else {
      return res
    }
  },
  error => {
    NProgress.done()
    
    // 处理HTTP错误状态码
    if (error.response) {
      const { status } = error.response
      if (status === 401) {
        Message.error('登录已过期，请重新登录')
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      } else if (status === 403) {
        Message.error('没有权限访问')
      } else if (status === 404) {
        Message.error('请求的资源不存在')
      } else if (status >= 500) {
        Message.error('服务器错误，请稍后重试')
      } else {
        Message.error(error.response.data?.message || error.message || '网络错误')
      }
    } else {
      Message.error(error.message || '网络错误')
    }
    
    return Promise.reject(error)
  }
)

export default service

