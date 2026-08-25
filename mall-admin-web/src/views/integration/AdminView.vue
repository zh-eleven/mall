<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi, roleApi } from '@/apis/backend'
import type { Admin, Role } from '@/types/backend'
import { useUserStore } from '@/stores/user'

const userStore=useUserStore()
const canWrite=userStore.hasAuthority('admin:write');const canReadRoles=userStore.hasAuthority('role:read')
const loading=ref(false);const rows=ref<Admin[]>([])
const dialogVisible=ref(false);const editingId=ref<number>()
const form=reactive({username:'',password:'',nickname:'',email:'',avatar:'',note:'',status:1})
const roleVisible=ref(false);const assigning=ref<Admin>();const allRoles=ref<Role[]>([]);const roleIds=ref<number[]>([])
async function load(){loading.value=true;try{rows.value=(await adminApi.list()).data}finally{loading.value=false}}
function reset(){Object.assign(form,{username:'',password:'',nickname:'',email:'',avatar:'',note:'',status:1})}
function openCreate(){editingId.value=undefined;reset();dialogVisible.value=true}
function openEdit(row:Admin){editingId.value=row.id;reset();Object.assign(form,{nickname:row.nickname||'',email:row.email||'',avatar:row.avatar||'',note:row.note||'',status:row.status,password:''});dialogVisible.value=true}
async function save(){if(!editingId.value&&(!form.username.trim()||form.password.length<6))return ElMessage.warning('请输入用户名和至少6位密码');const data=editingId.value?{nickname:form.nickname,email:form.email,avatar:form.avatar,note:form.note,status:form.status}:{...form};if(editingId.value)await adminApi.update(editingId.value,data);else await adminApi.create(data);ElMessage.success('保存成功');dialogVisible.value=false;await load()}
async function remove(row:Admin){await ElMessageBox.confirm(`确定删除管理员“${row.username}”吗？`,'删除确认',{type:'warning'});await adminApi.remove(row.id);ElMessage.success('删除成功');await load()}
async function openRoles(row:Admin){assigning.value=row;const [all,current]=await Promise.all([roleApi.list(),adminApi.roles(row.id)]);allRoles.value=all.data;roleIds.value=current.data.map(item=>item.id);roleVisible.value=true}
async function saveRoles(){if(!assigning.value)return;await adminApi.setRoles(assigning.value.id,roleIds.value);ElMessage.success('角色分配成功');roleVisible.value=false}
onMounted(load)
</script>
<template><div class="page"><el-card><div class="toolbar"><el-button v-if="canWrite" type="primary" @click="openCreate">新增管理员</el-button></div><el-table v-loading="loading" :data="rows" border><el-table-column prop="id" label="ID" width="70"/><el-table-column prop="username" label="用户名"/><el-table-column prop="nickname" label="昵称"/><el-table-column prop="email" label="邮箱"/><el-table-column label="状态" width="90"><template #default="{row}"><el-tag :type="row.status?'success':'danger'">{{row.status?'启用':'禁用'}}</el-tag></template></el-table-column><el-table-column prop="loginTime" label="最后登录" width="180"/><el-table-column prop="createTime" label="创建时间" width="180"/><el-table-column label="操作" width="210" fixed="right"><template #default="{row}"><el-button v-if="canWrite" link type="primary" @click="openEdit(row)">编辑</el-button><el-button v-if="canWrite&&canReadRoles" link type="warning" @click="openRoles(row)">分配角色</el-button><el-button v-if="canWrite" link type="danger" @click="remove(row)">删除</el-button></template></el-table-column></el-table></el-card>
<el-dialog v-model="dialogVisible" :title="editingId?'编辑管理员':'新增管理员'" width="600px"><el-form :model="form" label-width="90px"><el-form-item v-if="!editingId" label="用户名" required><el-input v-model="form.username"/></el-form-item><el-form-item v-if="!editingId" label="密码" required><el-input v-model="form.password" type="password" show-password/></el-form-item><el-form-item label="昵称"><el-input v-model="form.nickname"/></el-form-item><el-form-item label="邮箱"><el-input v-model="form.email"/></el-form-item><el-form-item label="头像"><el-input v-model="form.avatar"/></el-form-item><el-form-item label="备注"><el-input v-model="form.note" type="textarea"/></el-form-item><el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0"/></el-form-item></el-form><template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template></el-dialog>
<el-dialog v-model="roleVisible" title="分配角色" width="520px"><el-select v-model="roleIds" multiple style="width:100%"><el-option v-for="item in allRoles" :key="item.id" :label="`${item.name}（${item.code}）`" :value="item.id"/></el-select><template #footer><el-button @click="roleVisible=false">取消</el-button><el-button type="primary" @click="saveRoles">保存</el-button></template></el-dialog></div></template>
<style scoped>.page{padding:20px}.toolbar{margin-bottom:16px}</style>
