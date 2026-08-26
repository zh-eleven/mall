import { login as loginApi, getInfo } from '@/api/user'
import { getToken, setToken, removeToken } from '@/utils/auth'

const state = {
  token: getToken(),
  id: '',
  name: '',
  userInfo: {}
}

const mutations = {
  SET_TOKEN: (state, token) => {
    state.token = token
  },
  SET_USER_INFO: (state, info) => {
    state.userInfo = info
    state.id = info.id
    state.name = info.nickname || info.username
  }
}

const actions = {
  async login({ commit }, { username, password }) {
    const { data } = await loginApi({ username, password })

    if (!data?.token) {
      throw new Error('登录响应中缺少 Token')
    }

    commit('SET_TOKEN', data.token)
    setToken(data.token)

    return data
  },

  async logout({ commit }) {
    commit('SET_TOKEN', '')
    commit('SET_USER_INFO', {})
    removeToken()
  },

  async getInfo({ commit }) {
    const { data } = await getInfo()
    if (!data) throw new Error('获取基本信息失败，请重新登录')
    commit('SET_USER_INFO', data)
    return data
  },

  resetToken({ commit }) {
    commit('SET_TOKEN', '')
    removeToken()
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
