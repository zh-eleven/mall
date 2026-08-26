import request from '@/utils/request'

function parseSpecifications(value) {
  try {
    const result = Array.isArray(value) ? value : JSON.parse(value || '[]')
    return result.map(item => item.value).filter(Boolean)
  } catch {
    return []
  }
}

function normalizeCartItem(item) {
  return {
    id: item.id,
    goodsId: item.productId,
    productId: item.skuId,
    goodsName: item.productName,
    picUrl: item.pic || '',
    specifications: parseSpecifications(item.specData),
    price: Number(item.price || 0),
    number: item.quantity,
    maxNum: item.available ? item.availableStock : 0,
    checked: item.selected,
    available: item.available
  }
}

export async function getCartList() {
  const res = await request({
    url: '/members/me/cart',
    method: 'get'
  })

  return {
    ...res,
    data: (res.data || []).map(normalizeCartItem)
  }
}

export async function getCartGoodsCount() {
  const res = await request({
    url: '/members/me/cart',
    method: 'get'
  })

  return {
    ...res,
    data: (res.data || []).reduce(
      (total, item) => total + Number(item.quantity || 0),
      0
    )
  }
}

export function addCart(data) {
  return request({
    url: '/members/me/cart',
    method: 'post',
    data: {
      skuId: data.productId,
      quantity: data.number
    }
  })
}

export async function addDefaultGoodsProduct(data) {
  const detailRes = await request({
    url: `/products/${data.goodsId}`,
    method: 'get'
  })

  const sku = (detailRes.data?.skus || []).find(
    item => Number(item.availableStock || 0) > 0
  )

  if (!sku) {
    throw new Error('该商品暂时无可用库存')
  }

  return request({
    url: '/members/me/cart',
    method: 'post',
    data: {
      skuId: sku.id,
      quantity: Number(data.number || 1)
    }
  })
}

export function updateCart(data) {
  return request({
    url: `/members/me/cart/${data.id}/selected`,
    method: 'patch',
    data: {
      selected: data.checked
    }
  })
}

export function changeNumber(cartId, number) {
  return request({
    url: `/members/me/cart/${cartId}/quantity`,
    method: 'patch',
    data: {
      quantity: number
    }
  })
}

export function addNumber(cartId, number) {
  return changeNumber(cartId, number)
}

export function minusNumber(cartId, number) {
  return changeNumber(cartId, number)
}

export function deleteCart(cartId) {
  return request({
    url: `/members/me/cart/${cartId}`,
    method: 'delete'
  })
}

export async function getCheckedGoods() {
  const res = await getCartList()

  return {
    ...res,
    data: res.data.filter(item => item.checked && item.available)
  }
}
