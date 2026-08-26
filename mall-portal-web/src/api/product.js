import request from '@/utils/request'

function normalizeProduct(item = {}) {
  return {
    id: item.id,
    name: item.name,
    brief: item.subTitle,
    picUrl: item.pic || '',
    retailPrice: Number(item.price || 0),
    counterPrice: Number(item.originalPrice || 0)
  }
}

async function getCategoryProducts(data) {
  const res = await request({
    url: '/products',
    method: 'get',
    params: {
      categoryId: data.cateId,
      pageNum: data.pageNum,
      pageSize: data.pageSize
    }
  })

  return {
    data: {
      goods: (res.data?.list || []).map(normalizeProduct),
      category: {
        id: data.cateId,
        name: '商品分类'
      }
    }
  }
}

export const firstCategoryGoods = getCategoryProducts
export const secondCategoryGoods = getCategoryProducts
