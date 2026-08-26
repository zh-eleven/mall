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

export async function getHomeData() {
  const [categoryRes, productRes] = await Promise.all([
    request({
      url: '/product-categories/tree',
      method: 'get'
    }),
    request({
      url: '/products',
      method: 'get',
      params: {
        pageNum: 1,
        pageSize: 10
      }
    })
  ])

  const categories = categoryRes.data || []
  const products = (productRes.data?.list || []).map(normalizeProduct)

  return {
    data: {
      bannerList: products.slice(0, 3).map(item => ({
        id: item.id,
        name: item.name,
        imgUrl: item.picUrl,
        jumpUrl: `#/detail/${item.id}`
      })),

      diamondList: categories.map(item => ({
        id: item.id,
        name: item.name,
        iconUrl: item.icon || '',
        jumpType: 1
      })),

      newGoodsList: products.slice(0, 4),
      hotGoodsList: products
    }
  }
}

export async function getRecommonGoodsList(query) {
  const res = await request({
    url: '/products',
    method: 'get',
    params: {
      pageNum: query.pageNum,
      pageSize: query.pageSize
    }
  })

  return {
    ...res,
    data: (res.data?.list || []).map(normalizeProduct)
  }
}

export function getMallConfig() {
  return Promise.resolve({
    data: {
      freightLimit: 0
    }
  })
}
