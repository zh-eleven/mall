import request from '@/utils/request'

export function getInfo() {
  return request({
    url: '/members/me',
    method: 'get'
  })
}

export function profile(data) {
  const payload = {}

  if (data.nickname !== undefined) payload.nickname = data.nickname
  if (data.email !== undefined) payload.email = data.email
  if (data.avatar !== undefined) payload.avatar = data.avatar
  if (data.gender !== undefined) payload.gender = Number(data.gender)
  if (data.birthday !== undefined) payload.birthday = data.birthday

  if (data.phone !== undefined || data.mobile !== undefined) {
    payload.phone = data.phone ?? data.mobile
  }

  return request({
    url: '/members/me',
    method: 'patch',
    data: payload
  })
}
export function login(data) {
  return request({
    url: '/members/login',
    method: 'post',
    data
  })
}

export function logout() {
  return request({
    url: '/logout',
    method: 'post'
  })
}

export function setRegistry(data) {
  return request({
    url: '/members/register',
    method: 'post',
    data: {
      username: data.username,
      password: data.password,
      phone: data.phone || null,
      email: data.email || null
    }
  })
}

export function updatePassword(data) {
  return request({
    url: '/members/me/password',
    method: 'patch',
    data: {
      oldPassword: data.oldPassword,
      newPassword: data.newPassword ?? data.password,
      confirmPassword: data.confirmPassword
    }
  })
}

export function uploadAvatar(data) {
  return request.formDataPost('/user/uploadAvatar', data)
}

export function getMailCode(data) {
  return request({
    url: '/user/sendEmailCode',
    method: 'post',
    data,
    timeout: 20000
  })
}
