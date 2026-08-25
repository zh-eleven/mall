<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { attributeApi, attributeCategoryApi } from '@/apis/backend'
import type { AttributeCategory, ProductAttribute } from '@/types/backend'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const canWrite = userStore.hasAuthority('attribute:write')
const activeTab = ref('attributes')
const categories = ref<AttributeCategory[]>([])
const attributes = ref<ProductAttribute[]>([])
const categoryQuery = reactive({ keyword: '', pageNum: 1, pageSize: 100 })
const attrQuery = reactive({ keyword: '', categoryId: undefined as number | undefined, type: undefined as number | undefined, pageNum: 1, pageSize: 10 })
const attrTotal = ref(0)
const categoryDialog = ref(false)
const editingCategoryId = ref<number>()
const categoryName = ref('')
const attrDialog = ref(false)
const editingAttrId = ref<number>()
const attrForm = reactive({ productAttributeCategoryId: undefined as number | undefined, name: '', selectType: 0, inputType: 0, inputList: '', sort: 0, filterType: 0, searchType: 0, relatedStatus: 0, handAddStatus: 0, type: 0 })

async function loadCategories() { categories.value = (await attributeCategoryApi.page(categoryQuery)).data.list }
async function loadAttributes() { const res = await attributeApi.page(attrQuery); attributes.value = res.data.list; attrTotal.value = res.data.total }
function openCategory(row?: AttributeCategory) { editingCategoryId.value = row?.id; categoryName.value = row?.name || ''; categoryDialog.value = true }
async function saveCategory() { if (!categoryName.value.trim()) return ElMessage.warning('请输入分类名称'); if (editingCategoryId.value) await attributeCategoryApi.update(editingCategoryId.value, { name: categoryName.value }); else await attributeCategoryApi.create({ name: categoryName.value }); categoryDialog.value=false; ElMessage.success('保存成功'); await loadCategories() }
async function removeCategory(row: AttributeCategory) { await ElMessageBox.confirm(`确定删除“${row.name}”吗？`, '删除确认', { type:'warning' }); await attributeCategoryApi.remove(row.id); await loadCategories() }
function openAttribute(row?: ProductAttribute) { editingAttrId.value=row?.id; Object.assign(attrForm, row ? { productAttributeCategoryId:row.productAttributeCategoryId,name:row.name,selectType:row.selectType,inputType:row.inputType,inputList:row.inputList||'',sort:row.sort,filterType:row.filterType,searchType:row.searchType,relatedStatus:row.relatedStatus,handAddStatus:row.handAddStatus,type:row.type } : { productAttributeCategoryId: categories.value[0]?.id, name:'', selectType:0, inputType:0, inputList:'', sort:0, filterType:0, searchType:0, relatedStatus:0, handAddStatus:0, type:0 }); attrDialog.value=true }
async function saveAttribute() { if (!attrForm.productAttributeCategoryId || !attrForm.name.trim()) return ElMessage.warning('请填写属性分类和名称'); if (editingAttrId.value) await attributeApi.update(editingAttrId.value, attrForm); else await attributeApi.create(attrForm); attrDialog.value=false; ElMessage.success('保存成功'); await Promise.all([loadAttributes(), loadCategories()]) }
async function removeAttribute(row: ProductAttribute) { await ElMessageBox.confirm(`确定删除属性“${row.name}”吗？`, '删除确认', {type:'warning'}); await attributeApi.remove(row.id); await loadAttributes() }
onMounted(() => Promise.all([loadCategories(), loadAttributes()]))
</script>

<template><div class="page"><el-card><el-tabs v-model="activeTab">
  <el-tab-pane label="属性列表" name="attributes">
    <div class="toolbar"><el-input v-model="attrQuery.keyword" clearable placeholder="属性名称" /><el-select v-model="attrQuery.categoryId" clearable placeholder="属性分类"><el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" /></el-select><el-select v-model="attrQuery.type" clearable placeholder="类型"><el-option label="规格" :value="0" /><el-option label="参数" :value="1" /></el-select><el-button type="primary" @click="attrQuery.pageNum=1;loadAttributes()">查询</el-button><el-button v-if="canWrite" type="success" @click="openAttribute()">新增属性</el-button></div>
    <el-table :data="attributes" border><el-table-column prop="id" label="ID" width="70"/><el-table-column prop="name" label="属性名称"/><el-table-column label="分类" width="150"><template #default="{row}">{{ categories.find(i=>i.id===row.productAttributeCategoryId)?.name || row.productAttributeCategoryId }}</template></el-table-column><el-table-column label="类型" width="90"><template #default="{row}"><el-tag>{{ row.type ? '参数' : '规格' }}</el-tag></template></el-table-column><el-table-column prop="inputList" label="可选值" min-width="180"/><el-table-column prop="sort" label="排序" width="70"/><el-table-column v-if="canWrite" label="操作" width="140"><template #default="{row}"><el-button link type="primary" @click="openAttribute(row)">编辑</el-button><el-button link type="danger" @click="removeAttribute(row)">删除</el-button></template></el-table-column></el-table>
    <el-pagination v-model:current-page="attrQuery.pageNum" v-model:page-size="attrQuery.pageSize" :total="attrTotal" layout="total, sizes, prev, pager, next" @change="loadAttributes" />
  </el-tab-pane>
  <el-tab-pane label="属性分类" name="categories"><div class="toolbar"><el-button v-if="canWrite" type="primary" @click="openCategory()">新增属性分类</el-button></div><el-table :data="categories" border><el-table-column prop="id" label="ID" width="80"/><el-table-column prop="name" label="名称"/><el-table-column prop="attributeCount" label="规格数"/><el-table-column prop="paramCount" label="参数数"/><el-table-column v-if="canWrite" label="操作" width="140"><template #default="{row}"><el-button link type="primary" @click="openCategory(row)">编辑</el-button><el-button link type="danger" @click="removeCategory(row)">删除</el-button></template></el-table-column></el-table></el-tab-pane>
</el-tabs></el-card>
<el-dialog v-model="categoryDialog" title="属性分类" width="480px"><el-form label-width="90px"><el-form-item label="分类名称" required><el-input v-model="categoryName"/></el-form-item></el-form><template #footer><el-button @click="categoryDialog=false">取消</el-button><el-button type="primary" @click="saveCategory">保存</el-button></template></el-dialog>
<el-dialog v-model="attrDialog" title="商品属性" width="680px"><el-form :model="attrForm" label-width="110px"><el-form-item label="属性分类" required><el-select v-model="attrForm.productAttributeCategoryId"><el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id"/></el-select></el-form-item><el-form-item label="属性名称" required><el-input v-model="attrForm.name"/></el-form-item><el-form-item label="属性类型"><el-radio-group v-model="attrForm.type"><el-radio :value="0">规格</el-radio><el-radio :value="1">参数</el-radio></el-radio-group></el-form-item><el-form-item label="选择类型"><el-select v-model="attrForm.selectType"><el-option label="唯一" :value="0"/><el-option label="单选" :value="1"/><el-option label="多选" :value="2"/></el-select></el-form-item><el-form-item label="录入方式"><el-radio-group v-model="attrForm.inputType"><el-radio :value="0">手工</el-radio><el-radio :value="1">列表</el-radio></el-radio-group></el-form-item><el-form-item label="可选值"><el-input v-model="attrForm.inputList" placeholder="逗号分隔"/></el-form-item><el-form-item label="排序"><el-input-number v-model="attrForm.sort" :min="0"/></el-form-item><el-form-item label="允许新增"><el-switch v-model="attrForm.handAddStatus" :active-value="1" :inactive-value="0"/></el-form-item></el-form><template #footer><el-button @click="attrDialog=false">取消</el-button><el-button type="primary" @click="saveAttribute">保存</el-button></template></el-dialog>
</div></template>
<style scoped>.page{padding:20px}.toolbar{display:flex;gap:10px;margin-bottom:16px}.toolbar .el-input,.toolbar .el-select{width:190px}.el-pagination{margin-top:16px;justify-content:flex-end}</style>
