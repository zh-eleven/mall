import { request } from '@/utils/http'
import type {
  Admin,
  AdminPayload,
  AttributeCategory,
  AttributeValue,
  Brand,
  Member,
  OrderDetail,
  OrderSummary,
  PageResult,
  Product,
  ProductAttribute,
  ProductCategory,
  ProductDetail,
  Refund,
  Resource,
  Role,
  Sku,
} from '@/types/backend'

export const adminApi = {
  list: () => request<Admin[]>({ url: '/admin/users' }),
  create: (data: AdminPayload) => request<Admin>({ method: 'POST', url: '/admin/users', data }),
  update: (id: number, data: AdminPayload) => request<Admin>({ method: 'PATCH', url: `/admin/users/${id}`, data }),
  remove: (id: number) => request<void>({ method: 'DELETE', url: `/admin/users/${id}` }),
  roles: (id: number) => request<Role[]>({ url: `/admin/users/${id}/roles` }),
  setRoles: (id: number, roleIds: number[]) => request<void>({ method: 'PUT', url: `/admin/users/${id}/roles`, data: { roleIds } }),
}

export const roleApi = {
  list: () => request<Role[]>({ url: '/admin/roles' }),
  create: (data: Partial<Role>) => request<Role>({ method: 'POST', url: '/admin/roles', data }),
  update: (id: number, data: Partial<Role>) => request<Role>({ method: 'PATCH', url: `/admin/roles/${id}`, data }),
  remove: (id: number) => request<void>({ method: 'DELETE', url: `/admin/roles/${id}` }),
  resources: (id: number) => request<Resource[]>({ url: `/admin/roles/${id}/resources` }),
  setResources: (id: number, resourceIds: number[]) => request<void>({ method: 'PUT', url: `/admin/roles/${id}/resources`, data: { resourceIds } }),
}

export const resourceApi = {
  list: () => request<Resource[]>({ url: '/admin/resources' }),
  create: (data: Partial<Resource>) => request<Resource>({ method: 'POST', url: '/admin/resources', data }),
  update: (id: number, data: Partial<Resource>) => request<Resource>({ method: 'PATCH', url: `/admin/resources/${id}`, data }),
  remove: (id: number) => request<void>({ method: 'DELETE', url: `/admin/resources/${id}` }),
}

export const memberApi = {
  page: (params: Record<string, unknown>) => request<PageResult<Member>>({ url: '/admin/members', params }),
  detail: (id: number) => request<Member>({ url: `/admin/members/${id}` }),
  setStatus: (id: number, status: number) => request<Member>({ method: 'PATCH', url: `/admin/members/${id}/status`, data: { status } }),
}

export const brandApi = {
  page: (params: Record<string, unknown>) => request<PageResult<Brand>>({ url: '/admin/brands', params }),
  get: (id: number) => request<Brand>({ url: `/admin/brands/${id}` }),
  create: (data: Partial<Brand>) => request<Brand>({ method: 'POST', url: '/admin/brands', data }),
  update: (id: number, data: Partial<Brand>) => request<Brand>({ method: 'PATCH', url: `/admin/brands/${id}`, data }),
  remove: (id: number) => request<void>({ method: 'DELETE', url: `/admin/brands/${id}` }),
}

export const categoryApi = {
  tree: () => request<ProductCategory[]>({ url: '/admin/product-categories/tree' }),
  get: (id: number) => request<ProductCategory>({ url: `/admin/product-categories/${id}` }),
  create: (data: Partial<ProductCategory>) => request<ProductCategory>({ method: 'POST', url: '/admin/product-categories', data }),
  update: (id: number, data: Partial<ProductCategory>) => request<ProductCategory>({ method: 'PATCH', url: `/admin/product-categories/${id}`, data }),
  remove: (id: number) => request<void>({ method: 'DELETE', url: `/admin/product-categories/${id}` }),
  attributes: (id: number) => request<ProductAttribute[]>({ url: `/admin/product-categories/${id}/attributes` }),
  setAttributes: (id: number, attributeIds: number[]) => request<ProductAttribute[]>({ method: 'PUT', url: `/admin/product-categories/${id}/attributes`, data: { attributeIds } }),
}

export const attributeCategoryApi = {
  page: (params: Record<string, unknown>) => request<PageResult<AttributeCategory>>({ url: '/admin/product-attribute-categories', params }),
  get: (id: number) => request<AttributeCategory>({ url: `/admin/product-attribute-categories/${id}` }),
  create: (data: { name: string }) => request<AttributeCategory>({ method: 'POST', url: '/admin/product-attribute-categories', data }),
  update: (id: number, data: { name: string }) => request<AttributeCategory>({ method: 'PATCH', url: `/admin/product-attribute-categories/${id}`, data }),
  remove: (id: number) => request<void>({ method: 'DELETE', url: `/admin/product-attribute-categories/${id}` }),
}

export const attributeApi = {
  page: (params: Record<string, unknown>) => request<PageResult<ProductAttribute>>({ url: '/admin/product-attributes', params }),
  get: (id: number) => request<ProductAttribute>({ url: `/admin/product-attributes/${id}` }),
  create: (data: Partial<ProductAttribute>) => request<ProductAttribute>({ method: 'POST', url: '/admin/product-attributes', data }),
  update: (id: number, data: Partial<ProductAttribute>) => request<ProductAttribute>({ method: 'PATCH', url: `/admin/product-attributes/${id}`, data }),
  remove: (id: number) => request<void>({ method: 'DELETE', url: `/admin/product-attributes/${id}` }),
}

export const productApi = {
  page: (params: Record<string, unknown>) => request<PageResult<Product>>({ url: '/admin/products', params }),
  get: (id: number) => request<Product>({ url: `/admin/products/${id}` }),
  detail: (id: number) => request<ProductDetail>({ url: `/admin/products/${id}/detail` }),
  create: (data: Partial<Product>) => request<Product>({ method: 'POST', url: '/admin/products', data }),
  update: (id: number, data: Partial<Product>) => request<Product>({ method: 'PATCH', url: `/admin/products/${id}`, data }),
  publish: (id: number, publishStatus: number) => request<Product>({ method: 'PUT', url: `/admin/products/${id}/publish-status`, data: { publishStatus } }),
  remove: (id: number) => request<void>({ method: 'DELETE', url: `/admin/products/${id}` }),
  attributeValues: (id: number) => request<AttributeValue[]>({ url: `/admin/products/${id}/attribute-values` }),
  setAttributeValues: (id: number, values: Array<{ productAttributeId: number; value: string }>) => request<AttributeValue[]>({ method: 'PUT', url: `/admin/products/${id}/attribute-values`, data: { values } }),
  skus: (id: number) => request<Sku[]>({ url: `/admin/products/${id}/skus` }),
  setSkus: (id: number, skus: unknown[]) => request<Sku[]>({ method: 'PUT', url: `/admin/products/${id}/skus`, data: { skus } }),
}

export const orderApi = {
  page: (params: Record<string, unknown>) => request<PageResult<OrderSummary>>({ url: '/admin/orders', params }),
  detail: (id: number) => request<OrderDetail>({ url: `/admin/orders/${id}` }),
  ship: (id: number, data: { deliveryCompany: string; deliverySn: string }) => request<OrderDetail>({ method: 'PATCH', url: `/admin/orders/${id}/ship`, data }),
}

export const refundApi = {
  page: (params: Record<string, unknown>) => request<PageResult<Refund>>({ url: '/admin/refunds', params }),
  detail: (id: number) => request<Refund>({ url: `/admin/refunds/${id}` }),
  approve: (id: number, adminNote?: string) => request<Refund>({ method: 'PATCH', url: `/admin/refunds/${id}/approve`, data: { adminNote } }),
  reject: (id: number, adminNote: string) => request<Refund>({ method: 'PATCH', url: `/admin/refunds/${id}/reject`, data: { adminNote } }),
}
