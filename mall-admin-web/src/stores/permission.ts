import { defineStore } from 'pinia'
import { shallowRef } from 'vue'
import { asyncRouterMap, constantRouterMap } from '@/router/index'
import type { RouteRecordExt } from '@/types/router'

function hasPermission(authorities: string[], route: RouteRecordExt) {
  const required = route.meta?.authority
  if (!required) return true
  const requiredList = Array.isArray(required) ? required : [required]
  return requiredList.some(authority => authorities.includes(authority))
}

// 对菜单进行排序
function sortRouters(accessedRouters: RouteRecordExt[]) {
  accessedRouters.forEach(router => {
    if (router.children && router.children.length > 0) {
      router.children.sort((a, b) => compare(a, b))
    }
  })
  accessedRouters.sort((a, b) => compare(a, b))
}

// 降序比较函数
function compare(a: RouteRecordExt, b: RouteRecordExt) {
  if (a.sort && b.sort) {
    return b.sort - a.sort
  } else {
    return 0
  }
}

export const usePermissionStore = defineStore('permission', () => {
  // 所有路由，静态路由+动态路由
  const routers = shallowRef(constantRouterMap)
  // 有权限访问的动态路由
  const addRouters = shallowRef<RouteRecordExt[]>([])
  // 生成可访问的路由表
  const generateRoutes = (authorities: string[]) => {
    const accessedRouters: RouteRecordExt[] = asyncRouterMap
      .map(route => {
        const children = (route.children as RouteRecordExt[] | undefined)
          ?.filter(child => hasPermission(authorities, child))
        return { ...route, children } as RouteRecordExt
      })
      .filter(route =>
        hasPermission(authorities, route) &&
        (!route.children || route.children.length > 0),
      )
    //对菜单进行排序
    sortRouters(accessedRouters)
    addRouters.value = accessedRouters
    routers.value = constantRouterMap.concat(accessedRouters)
  }

  const resetRoutes = () => {
    addRouters.value = []
    routers.value = constantRouterMap
  }

  return {
    routers,
    addRouters,
    generateRoutes,
    resetRoutes,
  }
})

// 默认导出保持兼容性
export default usePermissionStore
