<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { attributeApi, categoryApi } from '@/apis/backend'
import type { ProductAttribute, ProductCategory } from '@/types/backend'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const canWrite = userStore.hasAuthority('category:write')
const canAssign = userStore.hasAuthority('attribute:write') && userStore.hasAuthority('attribute:read')
const loading = ref(false)
const rows = ref<ProductCategory[]>([])
const dialogVisible = ref(false)
const editingId = ref<number>()
const form = reactive({ parentId: 0, name: '', productUnit: '', navStatus: 1, showStatus: 1, sort: 0, icon: '', keywords: '', description: '' })
const assignVisible = ref(false)
const assigningCategory = ref<ProductCategory>()
const allAttributes = ref<ProductAttribute[]>([])
const attributeIds = ref<number[]>([])
const rootOptions = computed(() => rows.value.filter(item => item.id !== editingId.value).map(item => ({ label: item.name, value: item.id })))

async function load() {
  loading.value = true
  try { rows.value = (await categoryApi.tree()).data } finally { loading.value = false }
}
function openCreate(parentId = 0) {
  editingId.value = undefined
  Object.assign(form, { parentId, name: '', productUnit: '', navStatus: 1, showStatus: 1, sort: 0, icon: '', keywords: '', description: '' })
  dialogVisible.value = true
}
function openEdit(row: ProductCategory) {
  editingId.value = row.id
  Object.assign(form, {
    parentId: row.parentId, name: row.name, productUnit: row.productUnit || '',
    navStatus: row.navStatus, showStatus: row.showStatus, sort: row.sort,
    icon: row.icon || '', keywords: row.keywords || '', description: row.description || '',
  })
  dialogVisible.value = true
}
async function save() {
  if (!form.name.trim()) return ElMessage.warning('请输入分类名称')
  if (editingId.value) await categoryApi.update(editingId.value, form)
  else await categoryApi.create(form)
  ElMessage.success('保存成功'); dialogVisible.value = false; await load()
}
async function remove(row: ProductCategory) {
  await ElMessageBox.confirm(`确定删除分类“${row.name}”吗？`, '删除确认', { type: 'warning' })
  await categoryApi.remove(row.id); ElMessage.success('删除成功'); await load()
}
async function openAssign(row: ProductCategory) {
  assigningCategory.value = row
  const [all, selected] = await Promise.all([
    attributeApi.page({ pageNum: 1, pageSize: 100 }),
    categoryApi.attributes(row.id),
  ])
  allAttributes.value = all.data.list
  attributeIds.value = selected.data.map(item => item.id)
  assignVisible.value = true
}
async function saveAssign() {
  if (!assigningCategory.value) return
  await categoryApi.setAttributes(assigningCategory.value.id, attributeIds.value)
  ElMessage.success('分类属性设置成功'); assignVisible.value = false
}
onMounted(load)
</script>

<template>
  <div class="page"><el-card>
    <div class="toolbar"><el-button v-if="canWrite" type="primary" @click="openCreate()">新增一级分类</el-button></div>
    <el-table v-loading="loading" :data="rows" row-key="id" border default-expand-all :tree-props="{ children: 'children' }">
      <el-table-column prop="name" label="分类名称" min-width="220" />
      <el-table-column prop="level" label="层级" width="80" />
      <el-table-column prop="productCount" label="商品数" width="90" />
      <el-table-column prop="productUnit" label="单位" width="90" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="显示" width="80"><template #default="{ row }"><el-tag :type="row.showStatus ? 'success' : 'info'">{{ row.showStatus ? '显示' : '隐藏' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="260" fixed="right"><template #default="{ row }">
        <el-button v-if="canWrite && row.level === 0" link type="success" @click="openCreate(row.id)">加子类</el-button>
        <el-button v-if="canWrite" link type="primary" @click="openEdit(row)">编辑</el-button>
        <el-button v-if="canAssign && row.level === 1" link type="warning" @click="openAssign(row)">配置属性</el-button>
        <el-button v-if="canWrite" link type="danger" @click="remove(row)">删除</el-button>
      </template></el-table-column>
    </el-table>
  </el-card>
  <el-dialog v-model="dialogVisible" :title="editingId ? '编辑分类' : '新增分类'" width="620px">
    <el-form :model="form" label-width="100px">
      <el-form-item label="父分类"><el-select v-model="form.parentId"><el-option label="一级分类" :value="0" /><el-option v-for="item in rootOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
      <el-form-item label="分类名称" required><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="商品单位"><el-input v-model="form.productUnit" /></el-form-item>
      <el-form-item label="导航显示"><el-switch v-model="form.navStatus" :active-value="1" :inactive-value="0" /></el-form-item>
      <el-form-item label="列表显示"><el-switch v-model="form.showStatus" :active-value="1" :inactive-value="0" /></el-form-item>
      <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
      <el-form-item label="图标地址"><el-input v-model="form.icon" /></el-form-item>
      <el-form-item label="关键词"><el-input v-model="form.keywords" /></el-form-item>
      <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
    </el-form><template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
  </el-dialog>
  <el-dialog v-model="assignVisible" title="配置分类属性" width="560px">
    <el-select v-model="attributeIds" multiple filterable style="width:100%" placeholder="选择属性">
      <el-option v-for="item in allAttributes" :key="item.id" :label="`${item.name}（${item.type ? '参数' : '规格'}）`" :value="item.id" />
    </el-select>
    <template #footer><el-button @click="assignVisible=false">取消</el-button><el-button type="primary" @click="saveAssign">保存</el-button></template>
  </el-dialog></div>
</template>
<style scoped>.page{padding:20px}.toolbar{margin-bottom:16px}</style>
