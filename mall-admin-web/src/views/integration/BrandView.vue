<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { brandApi } from '@/apis/backend'
import type { Brand } from '@/types/backend'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const canWrite = userStore.hasAuthority('brand:write')
const loading = ref(false)
const rows = ref<Brand[]>([])
const total = ref(0)
const query = reactive({ keyword: '', pageNum: 1, pageSize: 10 })
const dialogVisible = ref(false)
const editingId = ref<number>()
const form = reactive({
  name: '', firstLetter: '', sort: 0, factoryStatus: 1, showStatus: 1,
  logo: '', bigPic: '', brandStory: '',
})

async function load() {
  loading.value = true
  try {
    const res = await brandApi.page(query)
    rows.value = res.data.list
    total.value = res.data.total
  } finally { loading.value = false }
}

function openCreate() {
  editingId.value = undefined
  Object.assign(form, { name: '', firstLetter: '', sort: 0, factoryStatus: 1, showStatus: 1, logo: '', bigPic: '', brandStory: '' })
  dialogVisible.value = true
}

function openEdit(row: Brand) {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name, firstLetter: row.firstLetter || '', sort: row.sort,
    factoryStatus: row.factoryStatus, showStatus: row.showStatus,
    logo: row.logo || '', bigPic: row.bigPic || '', brandStory: row.brandStory || '',
  })
  dialogVisible.value = true
}

async function save() {
  if (!form.name.trim()) return ElMessage.warning('请输入品牌名称')
  const data = { ...form, firstLetter: form.firstLetter || undefined }
  if (editingId.value) await brandApi.update(editingId.value, data)
  else await brandApi.create(data)
  ElMessage.success(editingId.value ? '品牌修改成功' : '品牌创建成功')
  dialogVisible.value = false
  await load()
}

async function remove(row: Brand) {
  await ElMessageBox.confirm(`确定删除品牌“${row.name}”吗？`, '删除确认', { type: 'warning' })
  await brandApi.remove(row.id)
  ElMessage.success('删除成功')
  await load()
}

onMounted(load)
</script>

<template>
  <div class="page">
    <el-card>
      <div class="toolbar">
        <el-input v-model="query.keyword" clearable placeholder="品牌名称" @keyup.enter="load" />
        <el-button type="primary" @click="query.pageNum = 1; load()">查询</el-button>
        <el-button v-if="canWrite" type="success" @click="openCreate">新增品牌</el-button>
      </div>
      <el-table v-loading="loading" :data="rows" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="品牌名称" min-width="140" />
        <el-table-column prop="firstLetter" label="首字母" width="80" />
        <el-table-column prop="productCount" label="商品数" width="90" />
        <el-table-column label="厂家" width="80"><template #default="{ row }"><el-tag :type="row.factoryStatus ? 'success' : 'info'">{{ row.factoryStatus ? '是' : '否' }}</el-tag></template></el-table-column>
        <el-table-column label="显示" width="80"><template #default="{ row }"><el-tag :type="row.showStatus ? 'success' : 'info'">{{ row.showStatus ? '显示' : '隐藏' }}</el-tag></template></el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column v-if="canWrite" label="操作" width="150" fixed="right">
          <template #default="{ row }"><el-button link type="primary" @click="openEdit(row)">编辑</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" layout="total, sizes, prev, pager, next" @change="load" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑品牌' : '新增品牌'" width="620px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="品牌名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="首字母"><el-input v-model="form.firstLetter" maxlength="1" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="厂家品牌"><el-switch v-model="form.factoryStatus" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="是否显示"><el-switch v-model="form.showStatus" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="Logo"><el-input v-model="form.logo" /></el-form-item>
        <el-form-item label="专区图片"><el-input v-model="form.bigPic" /></el-form-item>
        <el-form-item label="品牌故事"><el-input v-model="form.brandStory" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 20px; }.toolbar { display:flex; gap:10px; margin-bottom:16px; }.toolbar .el-input { width:240px; }.el-pagination { margin-top:16px; justify-content:flex-end; }
</style>
