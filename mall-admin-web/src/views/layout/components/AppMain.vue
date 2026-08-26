<script lang="ts" setup>
// 定义组件名称
defineOptions({
  name: 'AppMain'
})
</script>

<template>
  <section class="app-main">
    <router-view v-slot="{ Component }">
        <component :is="Component" />
    </router-view>
  </section>
</template>
