import axios, { type AxiosInstance, type InternalAxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截：自动添加Token（直接读 localStorage，避免循环依赖）
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截：统一处理错误
// ⚠️ 不直接 import router/store，改为运行时动态获取，避免循环依赖
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response
    if (data.code === 200) {
      return data
    }
    if (data.code === 401) {
      ElMessage.error(data.message || '登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      // 运行时动态 import，避免循环依赖
      import('@/router').then(({ default: router }) => {
        router.push('/login')
      })
      return Promise.reject(new Error(data.message))
    }
    if (data.code === 403) {
      ElMessage.error('权限不足')
      return Promise.reject(new Error('权限不足'))
    }
    ElMessage.error(data.message || '操作失败')
    return Promise.reject(new Error(data.message))
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      import('@/router').then(({ default: router }) => {
        router.push('/login')
      })
    }
    ElMessage.error(error.response?.data?.message || '网络请求失败')
    return Promise.reject(error)
  }
)

export default request
