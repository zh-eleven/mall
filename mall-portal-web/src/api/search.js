import request from '@/utils/request'

function normalizeProduct(item = {}) {
  return {
    id: item.id,
    name: item.name,
    brief: item.subTitle,
    picUrl: item.pic || '',
    retailPrice: Number(item.price || 0),
    counterPrice: Number(item.originalPrice || 0),
    virtualSales: 0
  }
}

export async function getHotList() {
  const res = await request({
    url: '/product-categories/tree',
    method: 'get'
  })

  const categories = res.data || []

  return {
    data: {
      defaultSearch: categories[0]?.name || '搜索商品',
      hotStrings: categories
        .flatMap(item => [
          item.name,
          ...(item.children || []).map(child => child.name)
        ])
        .filter(Boolean)
        .slice(0, 10)
    }
  }
}

export async function getSearchList(data) {
  const res = await request({
    url: '/products',
    method: 'get',
    params: {
      keyword: data.keyword,
      pageNum: data.pageNum,
      pageSize: data.pageSize
    }
  })

  return {
    ...res,
    data: (res.data?.list || []).map(normalizeProduct)
  }
}

export async function getSearchSuggest(data) {
  const res = await request({
    url: '/products',
    method: 'get',
    params: {
      keyword: data.keyword,
      pageNum: 1,
      pageSize: 5
    }
  })

  return {
    ...res,
    data: (res.data?.list || [])
      .map(item => item.name)
      .filter(Boolean)
  }
}
