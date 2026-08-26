import request from '@/utils/request'

let categoryTreePromise

function loadCategoryTree() {
  if (!categoryTreePromise) {
    categoryTreePromise = request({
      url: '/product-categories/tree',
      method: 'get'
    })
      .then(res => res.data || [])
      .catch(error => {
        categoryTreePromise = null
        throw error
      })
  }

  return categoryTreePromise
}

function normalizeCategory(item = {}) {
  return {
    id: item.id,
    text: item.name,
    name: item.name,
    icon: item.icon || '',
    picUrl: item.icon || ''
  }
}

export async function getCategoryData() {
  const tree = await loadCategoryTree()
  const first = tree[0]

  return {
    data: {
      categoryList: tree.map(normalizeCategory),
      currentCategory: first ? normalizeCategory(first) : {},
      subCategoryList: (first?.children || []).map(normalizeCategory)
    }
  }
}

export async function getCategoryContent(query) {
  const tree = await loadCategoryTree()
  const current =
    tree.find(item => Number(item.id) === Number(query.id)) || tree[0]

  return {
    data: {
      currentCategory: current ? normalizeCategory(current) : {},
      subCategoryList: (current?.children || []).map(normalizeCategory)
    }
  }
}
