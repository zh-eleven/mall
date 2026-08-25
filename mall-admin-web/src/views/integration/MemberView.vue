<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { memberApi } from '@/apis/backend'
import type { Member } from '@/types/backend'
import { useUserStore } from '@/stores/user'

const userStore=useUserStore();const canWrite=userStore.hasAuthority('member:write')
const loading=ref(false);const rows=ref<Member[]>([]);const total=ref(0)
const query=reactive({username:'',phone:'',status:undefined as number|undefined,pageNum:1,pageSize:10})
const detailVisible=ref(false);const detail=ref<Member>()
async function load(){loading.value=true;try{const res=await memberApi.page(query);rows.value=res.data.list;total.value=res.data.total}finally{loading.value=false}}
async function showDetail(row:Member){detail.value=(await memberApi.detail(row.id)).data;detailVisible.value=true}
async function toggle(row:Member){const next=row.status?0:1;await ElMessageBox.confirm(`确定${next?'启用':'禁用'}会员“${row.username}”吗？`,'状态确认',{type:'warning'});await memberApi.setStatus(row.id,next);ElMessage.success('状态修改成功');await load()}
onMounted(load)
</script>
<template><div class="page"><el-card><div class="toolbar"><el-input v-model="query.username" clearable placeholder="用户名"/><el-input v-model="query.phone" clearable placeholder="手机号"/><el-select v-model="query.status" clearable placeholder="状态"><el-option label="禁用" :value="0"/><el-option label="启用" :value="1"/></el-select><el-button type="primary" @click="query.pageNum=1;load()">查询</el-button></div><el-table v-loading="loading" :data="rows" border><el-table-column prop="id" label="ID" width="80"/><el-table-column prop="username" label="用户名"/><el-table-column prop="nickname" label="昵称"/><el-table-column prop="phone" label="手机号"/><el-table-column prop="email" label="邮箱"/><el-table-column label="性别" width="80"><template #default="{row}">{{['未知','男','女'][row.gender||0]}}</template></el-table-column><el-table-column label="状态" width="90"><template #default="{row}"><el-tag :type="row.status?'success':'danger'">{{row.status?'启用':'禁用'}}</el-tag></template></el-table-column><el-table-column prop="createTime" label="注册时间" width="180"/><el-table-column label="操作" width="150"><template #default="{row}"><el-button link type="primary" @click="showDetail(row)">详情</el-button><el-button v-if="canWrite" link :type="row.status?'danger':'success'" @click="toggle(row)">{{row.status?'禁用':'启用'}}</el-button></template></el-table-column></el-table><el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" layout="total, sizes, prev, pager, next" @change="load"/></el-card><el-dialog v-model="detailVisible" title="会员详情" width="650px"><el-descriptions v-if="detail" :column="2" border><el-descriptions-item label="用户名">{{detail.username}}</el-descriptions-item><el-descriptions-item label="昵称">{{detail.nickname||'-'}}</el-descriptions-item><el-descriptions-item label="手机">{{detail.phone||'-'}}</el-descriptions-item><el-descriptions-item label="邮箱">{{detail.email||'-'}}</el-descriptions-item><el-descriptions-item label="生日">{{detail.birthday||'-'}}</el-descriptions-item><el-descriptions-item label="状态">{{detail.status?'启用':'禁用'}}</el-descriptions-item><el-descriptions-item label="注册时间" :span="2">{{detail.createTime}}</el-descriptions-item></el-descriptions></el-dialog></div></template>
<style scoped>.page{padding:20px}.toolbar{display:flex;gap:10px;margin-bottom:16px}.toolbar .el-input,.toolbar .el-select{width:200px}.el-pagination{margin-top:16px;justify-content:flex-end}</style>
