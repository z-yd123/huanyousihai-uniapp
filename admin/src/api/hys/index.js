import request from '@/utils/request'

// ============================ 运营看板 ============================
export function getDashboard() {
  return request({ url: '/hys/dashboard/overview', method: 'get' })
}

// ============================ 会员 ============================
export function listMember(query) {
  return request({ url: '/hys/member/list', method: 'get', params: query })
}

export function getMember(memberId) {
  return request({ url: '/hys/member/' + memberId, method: 'get' })
}

export function updateMember(memberId, data) {
  return request({ url: '/hys/member/' + memberId, method: 'put', data })
}

export function rebindReferrer(memberId, data) {
  return request({ url: '/hys/member/' + memberId + '/rebind', method: 'post', data })
}

export function resetMemberPassword(memberId, password) {
  return request({ url: '/hys/member/' + memberId + '/resetPwd', method: 'post', data: { password } })
}

// ============================ 线路与商品 ============================
export function listProduct(query) {
  return request({ url: '/hys/product/list', method: 'get', params: query })
}

export function getProduct(productId) {
  return request({ url: '/hys/product/' + productId, method: 'get' })
}

export function addProduct(data) {
  return request({ url: '/hys/product', method: 'post', data })
}

export function updateProduct(data) {
  return request({ url: '/hys/product', method: 'put', data })
}

export function changeProductStatus(productId, status) {
  return request({ url: '/hys/product/' + productId + '/status', method: 'put', params: { status } })
}

export function delProduct(productId) {
  return request({ url: '/hys/product/' + productId, method: 'delete' })
}

// ============================ 订单 ============================
export function listOrder(query) {
  return request({ url: '/hys/order/list', method: 'get', params: query })
}

export function getOrder(orderId) {
  return request({ url: '/hys/order/' + orderId, method: 'get' })
}

export function shipOrder(orderId) {
  return request({ url: '/hys/order/' + orderId + '/ship', method: 'post' })
}

export function refundOrder(orderId, reason) {
  return request({ url: '/hys/order/' + orderId + '/refund', method: 'post', data: { reason } })
}

export function rejectAfterSale(orderId) {
  return request({ url: '/hys/order/' + orderId + '/reject', method: 'post' })
}

// ============================ 佣金 / 异常对账 ============================
export function listCommission(query) {
  return request({ url: '/hys/commission/list', method: 'get', params: query })
}

export function commissionStats() {
  return request({ url: '/hys/commission/stats', method: 'get' })
}

export function listPayoutGap(query) {
  return request({ url: '/hys/commission/gap/list', method: 'get', params: query })
}

export function closePayoutGap(gapId, remark) {
  return request({ url: '/hys/commission/gap/' + gapId + '/close', method: 'post', data: { remark } })
}

// ============================ 资金审核 ============================
export function listFundApply(query) {
  return request({ url: '/hys/fund/list', method: 'get', params: query })
}

export function auditFundApply(applyId, pass, remark) {
  return request({ url: '/hys/fund/' + applyId + '/audit', method: 'post', data: { pass, remark } })
}

// ============================ 相册 ============================
export function listAlbum(query) {
  return request({ url: '/hys/album/list', method: 'get', params: query })
}

export function getAlbum(albumId) {
  return request({ url: '/hys/album/' + albumId, method: 'get' })
}

export function addAlbum(data) {
  return request({ url: '/hys/album', method: 'post', data })
}

export function updateAlbum(data) {
  return request({ url: '/hys/album', method: 'put', data })
}

export function delAlbum(albumId) {
  return request({ url: '/hys/album/' + albumId, method: 'delete' })
}

// ============================ 业务参数 ============================
export function listConfig(query) {
  return request({ url: '/hys/config/list', method: 'get', params: query })
}

export function updateConfig(configKey, configValue) {
  return request({ url: '/hys/config', method: 'put', data: { configKey, configValue } })
}
