import request from '@/utils/request'

export function orderPrepay(data) {
  return request({
    url: `/members/me/orders/${data.orderId}/pay`,
    method: 'patch'
  })
}
