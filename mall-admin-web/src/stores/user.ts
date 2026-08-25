import { defineStore } from 'pinia'
import { getAdminInfoAPI, adminLoginAPI } from '@/apis/admin'
import { ref } from 'vue'
import type { LoginParam, UserInfo } from '@/types/admin'

export const useUserStore = defineStore(
  'user',
  () => {
    // 用户信息
    const userInfo = ref<UserInfo>({
      username: '',
      password: '',
      avatar: '',
      token: '',
      authorities: [],
    })

    // 用户登录
    const userLogin = async (loginParam: LoginParam) => {
      const res = await adminLoginAPI(loginParam)

      userInfo.value.token = res.data.token
      userInfo.value.username = res.data.username

      await getUserInfo()
    }

    // 获取用户信息
    const getUserInfo = async () => {
      const res = await getAdminInfoAPI()

      userInfo.value.adminId = res.data.adminId
      userInfo.value.username = res.data.username
      userInfo.value.nickname = res.data.nickname
      userInfo.value.avatar = res.data.avatar || ''
      userInfo.value.authorities = res.data.authorities || []
    }

    // 用户登出
    // const userLogout = async () => {
    //   await adminLogoutAPI()
    //   userInfo.value.token = ''
    //   userInfo.value.roles = []
    // }

    // 前端登出
    const fedLogout = () => {
      userInfo.value.token = ''
      userInfo.value.authorities = []
    }

    const userLogout = async () => fedLogout()

    const hasAuthority = (authority: string) =>
      userInfo.value.authorities.includes(authority)

    return {
      userInfo,
      userLogin,
      getUserInfo,
      userLogout,
      fedLogout,
      hasAuthority,
    }
  },
  {
    // 持久化配置
    persist: true,
  },
)
