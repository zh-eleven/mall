import request from '@/utils/request'

function parseSpecs(value) {
  if (Array.isArray(value)) return value

  try {
    const result = JSON.parse(value || '[]')
    return Array.isArray(result) ? result : []
  } catch {
    return []
  }
}

export async function getDetail(goodsId) {
  const res = await request({
    url: `/products/${goodsId}`,
    method: 'get'
  })

  const product = res.data || {}
  const skuRows = (product.skus || []).map(sku => ({
    ...sku,
    specs: parseSpecs(sku.specData)
  }))

  const groups = new Map()

  skuRows.forEach(sku => {
    sku.specs.forEach(spec => {
      const key = String(spec.attributeId || spec.name)
      const group = groups.get(key) || {
        name: spec.name || '规格',
        valueList: []
      }

      if (!group.valueList.some(item => item.value === spec.value)) {
        group.valueList.push({
          id: `${key}:${spec.value}`,
          value: spec.value,
          picUrl: sku.pic || product.pic || ''
        })
      }

      groups.set(key, group)
    })
  })

  const gallery = [product.pic, ...(product.albumPics || [])]
    .filter((value, index, array) =>
      value && array.indexOf(value) === index
    )

  return {
    ...res,
    data: {
      info: {
        id: product.id,
        name: product.name,
        brief: product.subTitle,
        retailPrice: Number(product.price || 0),
        counterPrice: Number(product.originalPrice || 0),
        picUrl: product.pic || '',
        gallery,
        detail:
          product.detailHtml ||
          product.detailDesc ||
          product.description ||
          '',
        virtualSales: 0
      },

      attributes: (product.attributes || []).map(item => ({
        attribute: item.name,
        value: item.value
      })),

      specificationList: [...groups.values()],

      productList: skuRows.map(sku => ({
        id: sku.id,
        price: Number(sku.price || 0),
        number: sku.availableStock || 0,
        picUrl: sku.pic || product.pic || '',
        specifications: sku.specs.map(spec => spec.value)
      }))
    }
  }
}
