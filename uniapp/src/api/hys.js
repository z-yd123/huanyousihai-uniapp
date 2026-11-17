/* ============================================================================
   环游四海 V1.0 · 接口清单（仅 /app/** 小程序端聚合接口）
   后台管理端接口在 ruoyi-vue3 的 src/api/hys/index.js，两侧互不复用。
   ========================================================================== */
import http from './request'

/* ------------------------------ 账号 ------------------------------ */
export const authApi = {
  register: (data) => http.post('/app/auth/register', data),
  login: (data) => http.post('/app/auth/login', data),
  logout: () => http.post('/app/auth/logout'),
  changePassword: (data) => http.post('/app/auth/password', data),
  forgot: (data) => http.post('/app/auth/forgot', data)
}

/* --------------------------- 目录与相册 --------------------------- */
export const catalogApi = {
  home: () => http.get('/app/home'),
  products: (params) => http.get('/app/products', params),
  detail: (productId) => http.get('/app/products/' + productId),
  albums: () => http.get('/app/albums'),
  albumDetail: (albumId) => http.get('/app/albums/' + albumId),
  config: () => http.get('/app/public/config', null, { auth: false })
}

/* ------------------------------ 会员 ------------------------------ */
export const memberApi = {
  profile: () => http.get('/app/member/profile'),
  commissionSummary: () => http.get('/app/member/commission/summary'),
  commissionList: (params) => http.get('/app/member/commission/list', params),
  share: () => http.get('/app/member/share'),
  directs: (params) => http.get('/app/member/share/directs', params),
  inviteQr: (size) => http.get('/app/member/invite-qr', size ? { size } : null),
  renewVip: () => http.post('/app/member/vip/renew')
}

/* ------------------------------ 订单 ------------------------------ */
export const orderApi = {
  create: (data) => http.post('/app/orders', data),
  list: (params) => http.get('/app/orders', params),
  detail: (orderId) => http.get('/app/orders/' + orderId),
  pay: (orderId) => http.post('/app/orders/' + orderId + '/pay'),
  cancel: (orderId) => http.post('/app/orders/' + orderId + '/cancel'),
  confirm: (orderId) => http.post('/app/orders/' + orderId + '/confirm'),
  afterSale: (orderId, reason) => http.post('/app/orders/' + orderId + '/aftersale', { reason })
}

/* ------------------------------ 钱包 ------------------------------ */
export const walletApi = {
  summary: () => http.get('/app/wallet/summary'),
  transactions: (params) => http.get('/app/wallet/transactions', params),
  withdrawQuote: (amount) => http.get('/app/wallet/withdraw/quote', { amount }),
  withdraw: (data) => http.post('/app/wallet/withdraw', data),
  recharge: (data) => http.post('/app/wallet/recharge', data),
  transfer: (data) => http.post('/app/wallet/transfer', data),
  applies: (params) => http.get('/app/wallet/applies', params)
}

export default { authApi, catalogApi, memberApi, orderApi, walletApi }
