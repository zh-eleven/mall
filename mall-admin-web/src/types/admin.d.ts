/** 登录请求参数 */
export type LoginParam = {
  /** 用户名 */
  username: string
  /** 密码 */
  password: string
}

/** 登录返回结果 */
export type LoginResult = {
  adminId: number
  username: string
  token: string
}
/** 用户信息结果封装 */
export type UserInfoResult = {
  adminId: number
  username: string
  nickname?: string
  avatar?: string
  authorities: string[]
}

/** 用户信息（store中存储的） */
export type UserInfo = {
  /** 管理员ID */
  adminId?: number
  /** 用户名 */
  username: string
  /** 昵称 */
  nickname?: string
  /** 密码 */
  password: string
  /** 登录token */
  token: string
  /** 头像 */
  avatar: string
  /** 后端授予的权限编码 */
  authorities: string[]
}

/** 管理员信息 */
export type UmsAdmin = {
  /** ID */
  id?: number
  /** 用户名 */
  username: string
  /** 密码 */
  password: string
  /** 头像 */
  icon?: string
  /** 邮箱 */
  email?: string
  /** 昵称 */
  nickName?: string
  /** 备注信息 */
  note?: string
  /** 创建时间 */
  createTime?: string
  /** 最后登录时间 */
  loginTime?: string
  /** 帐号启用状态：0->禁用；1->启用 */
  status: number
}
