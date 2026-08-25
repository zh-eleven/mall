<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const modules = computed(() => [
  { title: '商品管理', path: '/catalog/products', authority: 'product:read', description: '商品、库存、SKU 与属性值' },
  { title: '订单管理', path: '/trade/orders', authority: 'order:read', description: '订单查询、详情与发货' },
  { title: '退款管理', path: '/trade/refunds', authority: 'refund:read', description: '退款审核与处理' },
  { title: '会员管理', path: '/member/list', authority: 'member:read', description: '会员查询与状态管理' },
  { title: '权限管理', path: '/access/admins', authority: 'admin:read', description: '管理员、角色与资源' },
].filter(item => userStore.hasAuthority(item.authority)))
</script>

<template>
  <div class="integration-page">
    <el-card>
      <h2>欢迎回来，{{ userStore.userInfo.nickname || userStore.userInfo.username }}</h2>
      <p class="muted">当前管理端已按后端权限动态开放功能，共授予 {{ userStore.userInfo.authorities.length }} 项权限。</p>
    </el-card>
    <el-row :gutter="20" class="module-grid">
      <el-col v-for="item in modules" :key="item.path" :xs="24" :sm="12" :lg="8">
        <router-link :to="item.path">
          <el-card shadow="hover" class="module-card">
            <h3>{{ item.title }}</h3>
            <p>{{ item.description }}</p>
          </el-card>
        </router-link>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.integration-page { padding: 24px; }
.muted { color: #909399; }
.module-grid { margin-top: 20px; }
.module-card { margin-bottom: 20px; min-height: 130px; }
a { color: inherit; text-decoration: none; }
</style>
