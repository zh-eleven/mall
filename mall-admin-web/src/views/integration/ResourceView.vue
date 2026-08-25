<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { resourceApi } from '@/apis/backend'
import type { Resource } from '@/types/backend'
import { useUserStore } from '@/stores/user'

const userStore=useUserStore();const canWrite=userStore.hasAuthority('resource:write')
const rows=ref<Resource[]>([]);const dialogVisible=ref(false);const editingId=ref<number>()
const form=reactive({name:'',code:'',urlPattern:'',httpMethod:'GET',description:'',status:1})
async function load(){rows.value=(await resourceApi.list()).data}
function openForm(row?:Resource){editingId.value=row?.id;Object.assign(form,row?{name:row.name,code:row.code,urlPattern:row.urlPattern||'',httpMethod:row.httpMethod||'GET',description:row.description||'',status:row.status}:{name:'',code:'',urlPattern:'',httpMethod:'GET',description:'',status:1});dialogVisible.value=true}
async function save(){if(!form.name.trim()||!form.code.trim())return ElMessage.warning('请填写资源名称和权限编码');if(editingId.value)await resourceApi.update(editingId.value,form);else await resourceApi.create(form);ElMessage.success('保存成功');dialogVisible.value=false;await load()}
async function remove(row:Resource){await ElMessageBox.confirm(`确定删除资源“${row.name}”吗？`,'删除确认',{type:'warning'});await resourceApi.remove(row.id);ElMessage.success('删除成功');await load()}
onMounted(load)
</script>
<template><div class="page"><el-card><div class="toolbar"><el-button v-if="canWrite" type="primary" @click="openForm()">新增资源</el-button></div><el-table :data="rows" border><el-table-column prop="id" label="ID" width="70"/><el-table-column prop="name" label="资源名称"/><el-table-column prop="code" label="权限编码"/><el-table-column prop="urlPattern" label="URL 模式" min-width="210"/><el-table-column prop="httpMethod" label="方法" width="90"/><el-table-column prop="description" label="描述"/><el-table-column label="状态" width="90"><template #default="{row}"><el-tag :type="row.status?'success':'danger'">{{row.status?'启用':'禁用'}}</el-tag></template></el-table-column><el-table-column v-if="canWrite" label="操作" width="140"><template #default="{row}"><el-button link type="primary" @click="openForm(row)">编辑</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template></el-table-column></el-table></el-card>
<el-dialog v-model="dialogVisible" :title="editingId?'编辑资源':'新增资源'" width="620px"><el-form :model="form" label-width="100px"><el-form-item label="资源名称" required><el-input v-model="form.name"/></el-form-item><el-form-item label="权限编码" required><el-input v-model="form.code" placeholder="例如 product:read"/></el-form-item><el-form-item label="URL 模式"><el-input v-model="form.urlPattern" placeholder="/api/admin/products/**"/></el-form-item><el-form-item label="HTTP 方法"><el-select v-model="form.httpMethod"><el-option v-for="method in ['GET','POST','PUT','PATCH','DELETE','ALL']" :key="method" :label="method" :value="method"/></el-select></el-form-item><el-form-item label="描述"><el-input v-model="form.description" type="textarea"/></el-form-item><el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0"/></el-form-item></el-form><template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template></el-dialog></div></template>
<style scoped>.page{padding:20px}.toolbar{margin-bottom:16px}</style>
