export type PageResult<T> = {
  pageNum: number
  pageSize: number
  total: number
  totalPages: number
  list: T[]
}

export type Status = number

export type Admin = {
  id: number
  username: string
  nickname?: string
  email?: string
  avatar?: string
  note?: string
  status: Status
  loginTime?: string
  createTime: string
  updateTime: string
}

export type AdminPayload = Partial<Omit<Admin, 'id' | 'username' | 'createTime' | 'updateTime' | 'loginTime'>> & {
  username?: string
  password?: string
}

export type Role = {
  id: number
  name: string
  code: string
  description?: string
  status: Status
  sort: number
  createTime: string
  updateTime: string
}

export type Resource = {
  id: number
  name: string
  code: string
  urlPattern?: string
  httpMethod?: string
  description?: string
  status: Status
  createTime: string
  updateTime: string
}

export type Member = {
  id: number
  username: string
  nickname?: string
  phone?: string
  email?: string
  avatar?: string
  gender?: number
  birthday?: string
  status: Status
  createTime: string
  updateTime: string
}

export type Brand = {
  id: number
  name: string
  firstLetter?: string
  sort: number
  factoryStatus: Status
  showStatus: Status
  productCount: number
  productCommentCount: number
  logo?: string
  bigPic?: string
  brandStory?: string
  createTime: string
  updateTime: string
}

export type ProductCategory = {
  id: number
  parentId: number
  name: string
  level: number
  productCount: number
  productUnit?: string
  navStatus: Status
  showStatus: Status
  sort: number
  icon?: string
  keywords?: string
  description?: string
  children: ProductCategory[]
}

export type AttributeCategory = {
  id: number
  name: string
  attributeCount: number
  paramCount: number
}

export type ProductAttribute = {
  id: number
  productAttributeCategoryId: number
  name: string
  selectType: number
  inputType: number
  inputList?: string
  sort: number
  filterType: number
  searchType: number
  relatedStatus: Status
  handAddStatus: Status
  type: Status
}

export type Product = {
  id: number
  brandId?: number
  productCategoryId: number
  name: string
  subTitle?: string
  productSn: string
  price: number
  originalPrice?: number
  stock: number
  lowStock: number
  unit?: string
  weight?: number
  publishStatus: Status
  newStatus: Status
  recommendStatus: Status
  verifyStatus: Status
  sort: number
  pic?: string
  albumPics?: string
  description?: string
  detailTitle?: string
  detailDesc?: string
  detailHtml?: string
  createTime: string
  updateTime: string
}

export type AttributeValue = {
  id: number
  productId: number
  productAttributeId: number
  value: string
}

export type Sku = {
  id: number
  productId: number
  skuCode: string
  price: number
  stock: number
  lockedStock: number
  availableStock: number
  lowStock: number
  pic?: string
  specKey?: string
  specData?: string
}

export type ProductDetail = { product: Product; attributeValues: AttributeValue[]; skus: Sku[] }

export type OrderSummary = {
  orderId: number
  orderSn: string
  memberId: number
  status: number
  statusDescription: string
  totalAmount: number
  payAmount: number
  receiverName: string
  receiverPhone: string
  itemCount: number
  totalQuantity: number
  firstProductName?: string
  firstProductPic?: string
  createTime: string
}

export type OrderDetail = OrderSummary & {
  note?: string
  receiver: Record<string, string>
  delivery: Record<string, string | undefined>
  paymentTime?: string
  cancelTime?: string
  updateTime: string
  items: Array<{
    id: number
    productId: number
    skuId: number
    skuCode: string
    productName: string
    productPic?: string
    specData?: string
    productPrice: number
    quantity: number
    subtotal: number
  }>
}

export type Refund = {
  refundId: number
  refundSn: string
  orderId: number
  orderSn: string
  memberId: number
  refundAmount: number
  reason: string
  status: number
  statusDescription: string
  adminNote?: string
  handleTime?: string
  createTime: string
  updateTime: string
}
