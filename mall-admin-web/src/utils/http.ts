import { useUserStore } from '@/stores/user'
import type { CommonResult } from '@/types/common'
import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const http = axios.create({
  baseURL: import.meta.env.VITE_BASE_SERVER_URL,
  timeout: 5000,
})

// axios请求拦截器
http.interceptors.request.use(
  config => {
    //从pinia获取token
    const userStore = useUserStore()
    const token = userStore.userInfo.token
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  e => Promise.reject(e),
)

// axios响应拦截器
http.interceptors.response.use(
  response => {
    const res: CommonResult<unknown> = response.data
    if (res.code !== 200) {
      // code为非200是抛错，这里统一处理提示信息
      ElMessage({
        message: res.message,
        type: 'error',
        duration: 3 * 1000,
      })
      return Promise.reject(new Error(res.message))
    } else {
      // 返回响应JSON中的data属性，不包括message和code
      return response.data
    }
  },
  error => {
    const result = error.response?.data as CommonResult<unknown> | undefined
    const message = result?.message || error.message || '请求失败'
    ElMessage({
      message,
      type: 'error',
      duration: 3 * 1000,
    })
    if (result?.code === 40100 || error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.fedLogout()
      if (!location.hash.startsWith('#/login')) {
        location.hash = '#/login'
      }
    }
    return Promise.reject(error)
  },
)

export function request<T>(config: AxiosRequestConfig): Promise<CommonResult<T>> {
  return http.request<CommonResult<T>, CommonResult<T>>(config)
}

export default http
