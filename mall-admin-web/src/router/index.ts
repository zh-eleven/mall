import { createRouter, createWebHashHistory } from 'vue-router'
import Layout from '@/views/layout/Layout.vue'
import type { RouteRecordExt } from '@/types/router'

export const constantRouterMap: RouteRecordExt[] = [
  { path: '/404', component: () => import('@/views/normal/404/index.vue'), hidden: true },
  { path: '/login', component: () => import('@/views/normal/login/index.vue'), hidden: true },
  {
    path: '',
    component: Layout,
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'home',
        component: () => import('@/views/integration/HomeView.vue'),
        meta: { title: '首页', icon: 'dashboard' },
      },
    ],
  },
]

export const asyncRouterMap: RouteRecordExt[] = [
  {
    path: '/catalog',
    component: Layout,
    redirect: '/catalog/products',
    name: 'catalog',
    meta: { title: '商品管理', icon: 'product' },
    children: [
      {
        path: 'products',
        name: 'products',
        component: () => import('@/views/integration/ProductView.vue'),
        meta: { title: '商品与 SKU', icon: 'product-list', authority: 'product:read' },
      },
      {
        path: 'brands',
        name: 'brands',
        component: () => import('@/views/integration/BrandView.vue'),
        meta: { title: '品牌管理', icon: 'product-brand', authority: 'brand:read' },
      },
      {
        path: 'categories',
        name: 'categories',
        component: () => import('@/views/integration/CategoryView.vue'),
        meta: { title: '商品分类', icon: 'product-cate', authority: 'category:read' },
      },
      {
        path: 'attributes',
        name: 'attributes',
        component: () => import('@/views/integration/AttributeView.vue'),
        meta: { title: '商品属性', icon: 'product-attr', authority: 'attribute:read' },
      },
    ],
  },
  {
    path: '/trade',
    component: Layout,
    redirect: '/trade/orders',
    name: 'trade',
    meta: { title: '交易管理', icon: 'order' },
    children: [
      {
        path: 'orders',
        name: 'orders',
        component: () => import('@/views/integration/OrderView.vue'),
        meta: { title: '订单管理', icon: 'order', authority: 'order:read' },
      },
      {
        path: 'refunds',
        name: 'refunds',
        component: () => import('@/views/integration/RefundView.vue'),
        meta: { title: '退款管理', icon: 'order-return', authority: 'refund:read' },
      },
    ],
  },
  {
    path: '/member',
    component: Layout,
    redirect: '/member/list',
    name: 'member',
    meta: { title: '会员管理', icon: 'user' },
    children: [
      {
        path: 'list',
        name: 'members',
        component: () => import('@/views/integration/MemberView.vue'),
        meta: { title: '会员列表', icon: 'user', authority: 'member:read' },
      },
    ],
  },
  {
    path: '/access',
    component: Layout,
    redirect: '/access/admins',
    name: 'access',
    meta: { title: '权限管理', icon: 'ums' },
    children: [
      {
        path: 'admins',
        name: 'admins',
        component: () => import('@/views/integration/AdminView.vue'),
        meta: { title: '管理员', icon: 'ums-admin', authority: 'admin:read' },
      },
      {
        path: 'roles',
        name: 'roles',
        component: () => import('@/views/integration/RoleView.vue'),
        meta: { title: '角色', icon: 'ums-role', authority: 'role:read' },
      },
      {
        path: 'resources',
        name: 'resources',
        component: () => import('@/views/integration/ResourceView.vue'),
        meta: { title: '权限资源', icon: 'ums-resource', authority: 'resource:read' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes: constantRouterMap,
})

export default router
