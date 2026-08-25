<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { brandApi, categoryApi, productApi } from '@/apis/backend'
import type { Brand, Product, ProductCategory, ProductDetail } from '@/types/backend'
import { useUserStore } from '@/stores/user'

type SkuDraft = { skuCode: string; price: number; stock: number; lowStock: number; pic: string; specsText: string }
const userStore = useUserStore()
const canWrite = userStore.hasAuthority('product:write')
const loading = ref(false)
const rows = ref<Product[]>([])
const total = ref(0)
const brands = ref<Brand[]>([])
const categories = ref<ProductCategory[]>([])
const query = reactive({ keyword: '', brandId: undefined as number | undefined, categoryId: undefined as number | undefined, publishStatus: undefined as number | undefined, pageNum: 1, pageSize: 10 })
const dialogVisible = ref(false)
const editingId = ref<number>()
const editingHasSkus = ref(false)
const form = reactive({
  brandId: undefined as number | undefined, productCategoryId: undefined as number | undefined,
  name: '', subTitle: '', productSn: '', price: 0, originalPrice: undefined as number | undefined,
  stock: 0, lowStock: 0, unit: '', weight: undefined as number | undefined,
  newStatus: 0, recommendStatus: 0, verifyStatus: 0, sort: 0,
  pic: '', albumPics: '', description: '', detailTitle: '', detailDesc: '', detailHtml: '',
})
const detailVisible = ref(false)
const detail = ref<ProductDetail>()
const detailTab = ref('base')
const attrDraft = ref<Array<{ productAttributeId: number; value: string }>>([])
const skuDraft = ref<SkuDraft[]>([])

const categoryOptions = computed(() => {
  const convert = (items: ProductCategory[]): Array<Record<string, unknown>> => items.map(item => ({ value: item.id, label: item.name, disabled: item.level === 0, children: item.children?.length ? convert(item.children) : undefined }))
  return convert(categories.value)
})

async function load() {
  loading.value = true
  try { const res = await productApi.page(query); rows.value=res.data.list; total.value=res.data.total } finally { loading.value=false }
}
async function loadLookups() {
  const tasks: Promise<unknown>[] = []
  if (userStore.hasAuthority('brand:read')) tasks.push(brandApi.page({ pageNum:1, pageSize:100 }).then(res => { brands.value=res.data.list }))
  if (userStore.hasAuthority('category:read')) tasks.push(categoryApi.tree().then(res => { categories.value=res.data }))
  await Promise.allSettled(tasks)
}
function resetForm() { Object.assign(form, { brandId:undefined, productCategoryId:undefined, name:'', subTitle:'', productSn:'', price:0, originalPrice:undefined, stock:0, lowStock:0, unit:'', weight:undefined, newStatus:0, recommendStatus:0, verifyStatus:0, sort:0, pic:'', albumPics:'', description:'', detailTitle:'', detailDesc:'', detailHtml:'' }) }
function openCreate() { editingId.value=undefined; editingHasSkus.value=false; resetForm(); dialogVisible.value=true }
async function openEdit(row: Product) {
  const res = await productApi.detail(row.id)
  editingId.value=row.id; editingHasSkus.value=res.data.skus.length > 0
  const product=res.data.product
  resetForm(); Object.assign(form, {
    brandId:product.brandId, productCategoryId:product.productCategoryId,
    name:product.name, subTitle:product.subTitle||'', productSn:product.productSn,
    price:Number(product.price), originalPrice:product.originalPrice == null ? undefined : Number(product.originalPrice),
    stock:product.stock, lowStock:product.lowStock, unit:product.unit||'', weight:product.weight == null ? undefined : Number(product.weight),
    newStatus:product.newStatus, recommendStatus:product.recommendStatus, verifyStatus:product.verifyStatus, sort:product.sort,
    pic:product.pic||'', albumPics:product.albumPics||'', description:product.description||'', detailTitle:product.detailTitle||'', detailDesc:product.detailDesc||'', detailHtml:product.detailHtml||'',
  }); dialogVisible.value=true
}
async function save() {
  if (!form.productCategoryId || !form.name.trim() || !form.productSn.trim()) return ElMessage.warning('请填写分类、名称和货号')
  const payload: Record<string, unknown> = { ...form }
  if (editingId.value && editingHasSkus.value) { delete payload.price; delete payload.stock; delete payload.productCategoryId }
  if (editingId.value) await productApi.update(editingId.value, payload)
  else await productApi.create(payload)
  ElMessage.success('保存成功'); dialogVisible.value=false; await load()
}
async function togglePublish(row: Product) {
  await productApi.publish(row.id, row.publishStatus ? 0 : 1); ElMessage.success(row.publishStatus ? '商品已下架' : '商品已上架'); await load()
}
async function remove(row: Product) { await ElMessageBox.confirm(`确定删除商品“${row.name}”吗？`, '删除确认', {type:'warning'}); await productApi.remove(row.id); ElMessage.success('删除成功'); await load() }
function parseSpecs(specData?: string) { try { const items=JSON.parse(specData || '[]') as Array<{attributeId:number;value:string}>; return JSON.stringify(items.map(i=>({productAttributeId:i.attributeId,value:i.value}))) } catch { return '[]' } }
async function openDetail(row: Product) {
  detail.value=(await productApi.detail(row.id)).data
  attrDraft.value=detail.value.attributeValues.map(item=>({productAttributeId:item.productAttributeId,value:item.value}))
  skuDraft.value=detail.value.skus.map(item=>({skuCode:item.skuCode,price:Number(item.price),stock:item.stock,lowStock:item.lowStock,pic:item.pic||'',specsText:parseSpecs(item.specData)}))
  detailTab.value='base'; detailVisible.value=true
}
function addAttr() { attrDraft.value.push({productAttributeId:0,value:''}) }
async function saveAttrs() { if (!detail.value) return; await productApi.setAttributeValues(detail.value.product.id, attrDraft.value); ElMessage.success('属性值保存成功'); await refreshDetail() }
function addSku() { skuDraft.value.push({skuCode:'',price:Number(detail.value?.product.price || 0),stock:0,lowStock:0,pic:'',specsText:'[]'}) }
async function saveSkus() {
  if (!detail.value) return
  let skus: unknown[]
  try { skus=skuDraft.value.map(item=>({skuCode:item.skuCode,price:item.price,stock:item.stock,lowStock:item.lowStock,pic:item.pic||undefined,specs:JSON.parse(item.specsText)})) } catch { return ElMessage.error('SKU 规格 JSON 格式错误') }
  await productApi.setSkus(detail.value.product.id, skus); ElMessage.success('SKU 保存成功'); await refreshDetail(); await load()
}
async function refreshDetail() { if (!detail.value) return; detail.value=(await productApi.detail(detail.value.product.id)).data }
onMounted(() => Promise.all([load(), loadLookups()]))
</script>

<template><div class="page"><el-card>
  <div class="toolbar"><el-input v-model="query.keyword" clearable placeholder="名称/货号"/><el-select v-model="query.brandId" clearable placeholder="品牌"><el-option v-for="item in brands" :key="item.id" :label="item.name" :value="item.id"/></el-select><el-cascader v-model="query.categoryId" :options="categoryOptions" :props="{emitPath:false,checkStrictly:true}" clearable placeholder="分类"/><el-select v-model="query.publishStatus" clearable placeholder="上架状态"><el-option label="下架" :value="0"/><el-option label="上架" :value="1"/></el-select><el-button type="primary" @click="query.pageNum=1;load()">查询</el-button><el-button v-if="canWrite" type="success" @click="openCreate">新增商品</el-button></div>
  <el-table v-loading="loading" :data="rows" border><el-table-column prop="id" label="ID" width="70"/><el-table-column prop="name" label="商品名称" min-width="180"/><el-table-column prop="productSn" label="货号" width="140"/><el-table-column prop="price" label="价格" width="100"/><el-table-column prop="stock" label="库存" width="90"/><el-table-column label="状态" width="90"><template #default="{row}"><el-tag :type="row.publishStatus ? 'success':'info'">{{ row.publishStatus?'上架':'下架' }}</el-tag></template></el-table-column><el-table-column prop="createTime" label="创建时间" width="180"/><el-table-column label="操作" width="250" fixed="right"><template #default="{row}"><el-button link type="primary" @click="openDetail(row)">详情/SKU</el-button><el-button v-if="canWrite" link type="primary" @click="openEdit(row)">编辑</el-button><el-button v-if="canWrite" link type="warning" @click="togglePublish(row)">{{row.publishStatus?'下架':'上架'}}</el-button><el-button v-if="canWrite" link type="danger" @click="remove(row)">删除</el-button></template></el-table-column></el-table>
  <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" layout="total, sizes, prev, pager, next" @change="load"/>
</el-card>
<el-dialog v-model="dialogVisible" :title="editingId?'编辑商品':'新增商品'" width="820px"><el-form :model="form" label-width="100px"><el-row :gutter="16"><el-col :span="12"><el-form-item label="商品名称" required><el-input v-model="form.name"/></el-form-item></el-col><el-col :span="12"><el-form-item label="商品货号" required><el-input v-model="form.productSn"/></el-form-item></el-col><el-col :span="12"><el-form-item label="商品分类" required><el-cascader v-model="form.productCategoryId" :options="categoryOptions" :props="{emitPath:false}" style="width:100%"/></el-form-item></el-col><el-col :span="12"><el-form-item label="品牌"><el-select v-model="form.brandId" clearable><el-option v-for="item in brands" :key="item.id" :label="item.name" :value="item.id"/></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="销售价格"><el-input-number v-model="form.price" :min="0" :precision="2" :disabled="editingHasSkus"/></el-form-item></el-col><el-col :span="12"><el-form-item label="市场价格"><el-input-number v-model="form.originalPrice" :min="0" :precision="2"/></el-form-item></el-col><el-col :span="12"><el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" :disabled="editingHasSkus"/></el-form-item></el-col><el-col :span="12"><el-form-item label="预警库存"><el-input-number v-model="form.lowStock" :min="0"/></el-form-item></el-col><el-col :span="12"><el-form-item label="单位"><el-input v-model="form.unit"/></el-form-item></el-col><el-col :span="12"><el-form-item label="排序"><el-input-number v-model="form.sort" :min="0"/></el-form-item></el-col><el-col :span="24"><el-form-item label="副标题"><el-input v-model="form.subTitle"/></el-form-item></el-col><el-col :span="24"><el-form-item label="主图地址"><el-input v-model="form.pic"/></el-form-item></el-col><el-col :span="24"><el-form-item label="商品描述"><el-input v-model="form.description" type="textarea"/></el-form-item></el-col><el-col :span="24"><el-form-item label="详情 HTML"><el-input v-model="form.detailHtml" type="textarea" :rows="5"/></el-form-item></el-col></el-row></el-form><template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template></el-dialog>
<el-dialog v-model="detailVisible" title="商品详情与库存配置" width="1000px"><el-tabs v-if="detail" v-model="detailTab"><el-tab-pane label="商品详情" name="base"><el-descriptions :column="3" border><el-descriptions-item label="名称">{{detail.product.name}}</el-descriptions-item><el-descriptions-item label="货号">{{detail.product.productSn}}</el-descriptions-item><el-descriptions-item label="价格">￥{{detail.product.price}}</el-descriptions-item><el-descriptions-item label="库存">{{detail.product.stock}}</el-descriptions-item><el-descriptions-item label="分类ID">{{detail.product.productCategoryId}}</el-descriptions-item><el-descriptions-item label="状态">{{detail.product.publishStatus?'上架':'下架'}}</el-descriptions-item></el-descriptions></el-tab-pane>
<el-tab-pane label="属性值" name="attrs"><el-alert v-if="detail.skus.length" type="warning" :closable="false" title="存在 SKU 时后端不允许修改属性值，请先在 SKU 页签清空 SKU。"/><el-table :data="attrDraft" border><el-table-column label="属性ID"><template #default="{row}"><el-input-number v-model="row.productAttributeId" :min="1"/></template></el-table-column><el-table-column label="属性值（多值逗号分隔）"><template #default="{row}"><el-input v-model="row.value"/></template></el-table-column><el-table-column v-if="canWrite" width="80"><template #default="{$index}"><el-button link type="danger" @click="attrDraft.splice($index,1)">移除</el-button></template></el-table-column></el-table><div class="detail-actions" v-if="canWrite"><el-button @click="addAttr">新增属性值</el-button><el-button type="primary" @click="saveAttrs">保存属性值</el-button></div></el-tab-pane>
<el-tab-pane label="SKU" name="skus"><el-alert type="info" :closable="false" title='规格填写 JSON 数组，例如 [{"productAttributeId":1,"value":"红色"}]。保存空列表可清空 SKU。'/><el-table :data="skuDraft" border><el-table-column label="SKU编码" width="150"><template #default="{row}"><el-input v-model="row.skuCode"/></template></el-table-column><el-table-column label="价格" width="130"><template #default="{row}"><el-input-number v-model="row.price" :min="0" :precision="2" controls-position="right"/></template></el-table-column><el-table-column label="库存" width="110"><template #default="{row}"><el-input-number v-model="row.stock" :min="0" controls-position="right"/></template></el-table-column><el-table-column label="预警" width="110"><template #default="{row}"><el-input-number v-model="row.lowStock" :min="0" controls-position="right"/></template></el-table-column><el-table-column label="规格 JSON" min-width="280"><template #default="{row}"><el-input v-model="row.specsText"/></template></el-table-column><el-table-column v-if="canWrite" width="80"><template #default="{$index}"><el-button link type="danger" @click="skuDraft.splice($index,1)">移除</el-button></template></el-table-column></el-table><div class="detail-actions" v-if="canWrite"><el-button @click="addSku">新增 SKU</el-button><el-button type="primary" @click="saveSkus">保存全部 SKU</el-button></div></el-tab-pane></el-tabs></el-dialog>
</div></template>
<style scoped>.page{padding:20px}.toolbar{display:flex;gap:8px;margin-bottom:16px;flex-wrap:wrap}.toolbar .el-input,.toolbar .el-select,.toolbar .el-cascader{width:170px}.el-pagination{margin-top:16px;justify-content:flex-end}.detail-actions{margin-top:14px;text-align:right}</style>
