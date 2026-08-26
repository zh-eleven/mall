import request from '@/utils/request'

export function submit(data) {
  return request({
    url: '/members/me/orders',
    method: 'post',
    data: {
      submitToken: data.submitToken,
      addressId: data.addressId,
      note: data.note
    }
  })
}

export async function statusCount() {
  const statuses = [0, 1, 2, 3]

  const results = await Promise.all(
    statuses.map(status =>
      request({
        url: '/members/me/orders',
        method: 'get',
        params: {
          status,
          pageNum: 1,
          pageSize: 1
        }
      })
    )
  )

  return {
    data: {
      unpaid: Number(results[0].data?.total || 0),
      unship: Number(results[1].data?.total || 0),
      unrecv: Number(results[2].data?.total || 0),
      uncomment: Number(results[3].data?.total || 0)
    }
  }
}

export async function orderDetail(orderId) {
  const res = await request({
    url: `/members/me/orders/${orderId}`,
    method: 'get'
  })

  const order = res.data || {}
  const receiver = order.receiver || {}

  return {
    ...res,
    data: {
      id: order.orderId,
      orderSn: order.orderSn,
      orderStatus: order.status,
      orderStatusText: order.statusDescription,

      orderPrice: Number(order.totalAmount || 0),
      actualPrice: Number(order.payAmount || 0),
      freightPrice: 0,
      couponPrice: 0,
      refundAmount: 0,

      createTime: order.createTime,
      payTime: order.paymentTime,
      payType: order.paymentTime ? 99 : null,
      payTypeText: order.paymentTime ? '测试支付' : '--',

      address: [
        receiver.name,
        receiver.phoneNumber,
        receiver.province,
        receiver.city,
        receiver.region,
        receiver.detailAddress
      ]
        .filter(Boolean)
        .join(' '),

      message: order.note,

      orderGoodsVOList: (order.items || []).map(item => ({
        id: item.id,
        goodsId: item.productId,
        productId: item.skuId,
        goodsName: item.productName,
        picUrl: item.productPic || '',
        specifications: parseOrderSpecifications(item.specData),
        price: Number(item.productPrice || 0),
        number: item.quantity,
        subtotal: Number(item.subtotal || 0),
        comment: 0
      })),

      handleOption: {
        cancel: order.status === 0,
        pay: order.status === 0,
        confirm: order.status === 2,
        refund: order.status === 1,
        delete: false,
        comment: false
      }
    }
  }
}

export function searchResult(orderId) {
  return request({
    url: `/order/searchResult/${orderId}`,
    method: 'get',
    params: { t: Date.now() }
  })
}

export async function orderList(data) {
  const showType = Number(data.showType || 0)

  const res = await request({
    url: '/members/me/orders',
    method: 'get',
    params: {
      status: showType === 0 ? undefined : showType - 1,
      pageNum: data.pageNum,
      pageSize: data.pageSize
    }
  })

  return {
    ...res,
    data: {
      data: (res.data?.list || []).map(normalizeOrder),
      page: Number(res.data?.pageNum || 1),
      pages: Number(res.data?.totalPages || 0),
      total: Number(res.data?.total || 0)
    }
  }
}

export function orderCancel(orderId) {
  return request({
    url: `/members/me/orders/${orderId}/cancel`,
    method: 'patch'
  })
}

export function orderRefund(orderId) {
  return request({
    url: `/members/me/orders/${orderId}/refunds`,
    method: 'post',
    data: {
      reason: '用户申请退款'
    }
  })
}

export function orderDelete(orderId) {
  return request({ url: `/order/delete/${orderId}`, method: 'post' })
}

export function orderConfirm(orderId) {
  return request({
    url: `/members/me/orders/${orderId}/confirm-receipt`,
    method: 'patch'
  })
}

export function previewOrder(addressId) {
  return request({
    url: '/members/me/orders/preview',
    method: 'post',
    data: {
      addressId
    }
  })
}

function parseOrderSpecifications(value) {
  try {
    const result = Array.isArray(value)
      ? value
      : JSON.parse(value || '[]')

    return result.map(item => item.value).filter(Boolean)
  } catch {
    return []
  }
}

function normalizeOrder(item) {
  const quantity = Number(item.totalQuantity || 0)

  return {
    id: item.orderId,
    orderSn: item.orderSn,
    orderStatus: item.status,
    orderStatusText: item.statusDescription,
    actualPrice: Number(item.payAmount || 0),
    freightPrice: 0,
    createTime: item.createTime,

    goodsList: item.firstProductName
      ? [
        {
          id: item.orderId,
          goodsId: 0,
          goodsName:
            item.itemCount > 1
              ? `${item.firstProductName} 等${item.itemCount}件商品`
              : item.firstProductName,
          picUrl: item.firstProductPic || '',
          number: quantity,
          price:
            quantity > 0
              ? Number(item.totalAmount || 0) / quantity
              : 0,
          specifications: []
        }
      ]
      : [],

    handleOption: {
      cancel: item.status === 0,
      pay: item.status === 0,
      confirm: item.status === 2,
      refund: item.status === 1,
      delete: false,
      comment: false
    }
  }
}
