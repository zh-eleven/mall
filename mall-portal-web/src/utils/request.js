import axios from 'axios'
import qs from 'qs'
import { showToast, showDialog } from 'vant'
import store from '@/store'
import { getToken } from '@/utils/auth'
import router from '@/router'

// 创建一个axios实例
const service = axios.create({
  baseURL: import.meta.env.VUE_APP_BASE_API,
  // withCredentials: true,
  timeout: 5000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    if (store.getters.token) {
      config.headers.Authorization = `Bearer ${getToken()}`
    }
    return config
  },
  error => {
    console.log(error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data

    if (res.code === 200) {
      return res
    }

    const message = res.message || '请求失败'

    if (res.code === 40100) {
      showDialog({
        title: '提示',
        message: '登录状态已失效，请重新登录'
      }).then(() => {
        store.dispatch('user/resetToken').then(() => {
          router.push({ name: 'Login' })
        })
      })
    } else {
      showToast({
        type: 'fail',
        message
      })
    }

    return Promise.reject(new Error(message))
  },
  error => {
    const message =
      error.response?.data?.message ||
      error.message ||
      '网络请求失败'

    showToast({
      type: 'fail',
      message
    })

    return Promise.reject(new Error(message))
  }
)

/**
 * 使用 application/x-www-form-urlencoded format
 * @param {*} url
 * @param {*} postData
 * @returns
 */
service.formDataPost = function(url, postData) {
  const options = {
    method: 'POST',
    headers: { 'content-type': 'application/x-www-form-urlencoded' },
    data: qs.stringify(postData),
    url
  }
  return service(options)
}

export default service
