import request from '@/utils/request'

function normalizeAddress(item) {
  return {
    id: item.id,
    name: item.name,
    tel: item.phoneNumber,
    province: item.province,
    city: item.city,
    county: item.region,
    addressDetail: item.detailAddress,
    postalCode: item.postCode || '',
    isDefault: item.defaultStatus === 1
  }
}

function toBackendData(data, includeDefault = false) {
  const result = {
    name: data.name,
    phoneNumber: data.tel,
    postCode: data.postalCode || '',
    province: data.province,
    city: data.city,
    region: data.county,
    detailAddress: data.addressDetail
  }

  if (includeDefault) {
    result.defaultStatus = data.isDefault ? 1 : 0
  }

  return result
}

export async function getAddress() {
  const res = await request({
    url: '/members/me/addresses',
    method: 'get'
  })

  return {
    ...res,
    data: (res.data || []).map(normalizeAddress)
  }
}

export function addAddress(data) {
  return request({
    url: '/members/me/addresses',
    method: 'post',
    data: toBackendData(data, true)
  })
}

export async function updateAddress(data) {
  const res = await request({
    url: `/members/me/addresses/${data.id}`,
    method: 'patch',
    data: toBackendData(data)
  })

  if (data.isDefault) {
    await setDefaultAddress(data.id)
  }

  return res
}

export function setDefaultAddress(id) {
  return request({
    url: `/members/me/addresses/${id}/default`,
    method: 'patch'
  })
}

export function deleteAddress(id) {
  return request({
    url: `/members/me/addresses/${id}`,
    method: 'delete'
  })
}
