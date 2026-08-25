<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { resourceApi, roleApi } from '@/apis/backend'
import type { Resource, Role } from '@/types/backend'
import { useUserStore } from '@/stores/user'

const userStore=useUserStore();const canWrite=userStore.hasAuthority('role:write');const canReadResources=userStore.hasAuthority('resource:read')
const rows=ref<Role[]>([]);const dialogVisible=ref(false);const editingId=ref<number>()
const form=reactive({name:'',code:'',description:'',status:1,sort:0})
const resourceVisible=ref(false);const assigning=ref<Role>();const allResources=ref<Resource[]>([]);const resourceIds=ref<number[]>([])
async function load(){rows.value=(await roleApi.list()).data}
function openForm(row?:Role){editingId.value=row?.id;Object.assign(form,row?{name:row.name,code:row.code,description:row.description||'',status:row.status,sort:row.sort}:{name:'',code:'',description:'',status:1,sort:0});dialogVisible.value=true}
async function save(){if(!form.name.trim()||!form.code.trim())return ElMessage.warning('请填写角色名称和编码');if(editingId.value)await roleApi.update(editingId.value,form);else await roleApi.create(form);ElMessage.success('保存成功');dialogVisible.value=false;await load()}
async function remove(row:Role){await ElMessageBox.confirm(`确定删除角色“${row.name}”吗？`,'删除确认',{type:'warning'});await roleApi.remove(row.id);ElMessage.success('删除成功');await load()}
async function openResources(row:Role){assigning.value=row;const [all,current]=await Promise.all([resourceApi.list(),roleApi.resources(row.id)]);allResources.value=all.data;resourceIds.value=current.data.map(item=>item.id);resourceVisible.value=true}
async function saveResources(){if(!assigning.value)return;await roleApi.setResources(assigning.value.id,resourceIds.value);ElMessage.success('资源分配成功');resourceVisible.value=false}
onMounted(load)
</script>
<template><div class="page"><el-card><div class="toolbar"><el-button v-if="canWrite" type="primary" @click="openForm()">新增角色</el-button></div><el-table :data="rows" border><el-table-column prop="id" label="ID" width="70"/><el-table-column prop="name" label="角色名称"/><el-table-column prop="code" label="角色编码"/><el-table-column prop="description" label="描述"/><el-table-column prop="sort" label="排序" width="80"/><el-table-column label="状态" width="90"><template #default="{row}"><el-tag :type="row.status?'success':'danger'">{{row.status?'启用':'禁用'}}</el-tag></template></el-table-column><el-table-column label="操作" width="210"><template #default="{row}"><el-button v-if="canWrite" link type="primary" @click="openForm(row)">编辑</el-button><el-button v-if="canWrite&&canReadResources" link type="warning" @click="openResources(row)">分配资源</el-button><el-button v-if="canWrite" link type="danger" @click="remove(row)">删除</el-button></template></el-table-column></el-table></el-card>
<el-dialog v-model="dialogVisible" :title="editingId?'编辑角色':'新增角色'" width="560px"><el-form :model="form" label-width="90px"><el-form-item label="名称" required><el-input v-model="form.name"/></el-form-item><el-form-item label="编码" required><el-input v-model="form.code" placeholder="大写字母、数字、下划线"/></el-form-item><el-form-item label="描述"><el-input v-model="form.description" type="textarea"/></el-form-item><el-form-item label="排序"><el-input-number v-model="form.sort" :min="0"/></el-form-item><el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0"/></el-form-item></el-form><template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template></el-dialog>
<el-dialog v-model="resourceVisible" title="分配权限资源" width="720px"><el-checkbox-group v-model="resourceIds"><el-row><el-col v-for="item in allResources" :key="item.id" :span="12"><el-checkbox :value="item.id">{{item.name}}（{{item.code}}）</el-checkbox></el-col></el-row></el-checkbox-group><template #footer><el-button @click="resourceVisible=false">取消</el-button><el-button type="primary" @click="saveResources">保存</el-button></template></el-dialog></div></template>
<style scoped>.page{padding:20px}.toolbar{margin-bottom:16px}</style>
