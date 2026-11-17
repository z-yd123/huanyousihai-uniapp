/* ============================================================================
   环游四海 V1.0 · 前端状态与业务动作（uni-app · Vue3 reactive）

   与 mock 版本的区别：本文件不再持有任何样例数据，所有状态都来自后端 /app/**。
   页面层保持「同步读状态 + 异步触发动作」的用法：
     - 读：currentUser / isVip / myOrders() / myCommissions() ... 都是对本地缓存的选择器
     - 写：login() / createOrder() / payOrder() ... 均为 async，返回 { ok, message, ... }
   金额全程以「分」为单位参与计算，展示时才格式化为元。
   ========================================================================== */
import { reactive, computed } from 'vue'
import { authApi, catalogApi, memberApi, orderApi, walletApi } from '@/api/hys'
import { getToken, setToken } from '@/api/request'
import { regions, copy, IMAGES } from './data.js'
import { money, VIP_PRICE, RECHARGE_MAX, guards } from '../utils/commission.js'

/* ------------------------------------------------------------------ 状态 */
const state = reactive({
  loggedIn: Boolean(getToken()),
  booted: false,
  member: null,
  /* 通讯录：当前用户 + 直推（用于互转收款人快捷选择、推荐人展示） */
  users: [],
  routes: [],
  products: [],
  vipProduct: null,
  orders: [],
  orderDetail: null,
  commissions: [],
  commissionSummary: { total: 0, direct: 0, indirect: 0, pending: 0, count: 0 },
  wallet: [],
  walletSummary: { balance: 0, frozen: 0, commissionTotal: 0, rechargeTotal: 0, withdrawTotal: 0 },
  albums: [],
  share: { teamCount: 0, directCount: 0, monthNew: 0, inviteCode: '', referrerName: '', notice: '' },
  directs: [],
  inviteQr: '',
  notice: '',
  regions,
  copy,
  images: IMAGES,
  inviteFrom: '',
  toast: ''
})

const currentUser = computed(() => state.member || emptyUser())
const isVip = computed(() => guards.isVipActive(currentUser.value))
const vipState = computed(() => guards.vipState(currentUser.value))

function emptyUser() {
  return {
    id: 0,
    name: '未登录',
    phone: '',
    region: [],
    level: '普通',
    vipExpireAt: '',
    balance: 0,
    frozen: 0,
    greenPoints: 0,
    createdAt: ''
  }
}

/* -------------------------------------------------------------- 视图模型 */
const ORDER_STATUS_TEXT = { 0: '待付款', 1: '待收货', 2: '已完成', 3: '售后单', 4: '已退款', 5: '已取消' }

const TXN_TEXT = {
  RECHARGE: '充值',
  TRANSFER_OUT: '互转转出',
  TRANSFER_IN: '互转转入',
  WITHDRAW: '提现',
  COMMISSION: '佣金',
  COMMISSION_REVERSE: '佣金回冲',
  ORDER_PAY: '消费',
  REFUND: '退款',
  VIP_RENEW: '续费 VIP',
  WITHDRAW_UNFREEZE: '提现解冻'
}

const TXN_STATUS = { WITHDRAW: '审核中', COMMISSION_REVERSE: '已回冲' }

function toMember(m) {
  if (!m) return null
  return {
    id: m.memberId,
    name: m.nickname,
    phone: m.phone,
    region: [m.province, m.city, m.district].filter(Boolean),
    level: m.vipActive ? 'VIP' : '普通',
    vipActive: Boolean(m.vipActive),
    vipExpireAt: m.vipExpireAt ? String(m.vipExpireAt).substring(0, 10) : '',
    balance: Number(m.balance || 0),
    frozen: Number(m.frozen || 0),
    greenPoints: Number(m.greenPoints || 0),
    referrerName: m.referrerName || '',
    referrerPhone: m.referrerPhone || '',
    inviteCode: m.inviteCode || '',
    createdAt: m.createTime ? String(m.createTime).substring(0, 10) : ''
  }
}

function toRouteCard(c) {
  const selfDrive = c.routeType === 'SELF_DRIVE'
  return {
    id: c.productId,
    type: selfDrive ? '自驾游' : '组团游',
    title: c.title,
    subtitle: c.subtitle || '',
    days: c.days || 0,
    nights: c.nights || 0,
    cover: c.cover || IMAGES.dali,
    normalPrice: Number(c.normalPrice || 0),
    vipPrice: Number(c.vipPrice || 0),
    price: Number(c.price || c.normalPrice || 0),
    departCity: '',
    capacity: 0,
    sold: c.sales || 0,
    rating: '4.8',
    gather: { time: '', place: '' },
    tags: c.tag ? [c.tag] : [],
    selfDrive,
    video: false,
    // 自驾游途经点：后端暂无独立字段，详情加载后按行程标题补齐
    ways: [],
    itinerary: [],
    gallery: { hotel: [], food: [], spot: [] },
    detail: '',
    includes: '',
    excludes: '',
    loaded: false
  }
}

function toProductCard(c) {
  return {
    id: c.productId,
    type: '特产',
    title: c.title,
    cover: c.cover || IMAGES.mushroom,
    normalPrice: Number(c.normalPrice || 0),
    vipPrice: Number(c.vipPrice || 0),
    price: Number(c.price || c.normalPrice || 0),
    origin: c.subtitle || '',
    sales: c.sales || 0,
    spec: c.subtitle || '',
    desc: c.subtitle || '',
    detail: '',
    images: [],
    stock: 0,
    loaded: false
  }
}

/** 详情补全：卡片数据 + 行程分段 / 图集 / 费用说明 */
function mergeDetail(card, d, isRoute) {
  const images = d.images || []
  const days = (d.dayList || []).map((item, index) => ({
    day: 'DAY ' + String(item.dayNo || index + 1).padStart(2, '0'),
    title: item.title || '',
    meet: [item.gatherTime, item.gatherPlace].filter(Boolean).join(' · '),
    copy: item.content || '',
    img: (item.images && item.images[0]) || card.cover
  }))
  if (isRoute) {
    return {
      ...card,
      title: d.title || card.title,
      subtitle: d.subtitle || card.subtitle,
      cover: d.cover || card.cover,
      days: d.days || card.days,
      nights: d.nights || card.nights,
      normalPrice: Number(d.normalPrice || card.normalPrice),
      vipPrice: Number(d.vipPrice || card.vipPrice),
      price: Number(d.price || card.price),
      departCity: d.departCity || '',
      capacity: Number(d.stock || 0),
      sold: Number(d.sales || card.sold),
      gather: { time: d.gatherTime || '', place: d.gatherPlace || '' },
      tags: [
        d.includes ? '含' + d.includes.split('、')[0] : '',
        d.excludes ? '不含' + d.excludes.split('、')[0] : ''
      ].filter(Boolean),
      ways: selfDriveWays(d),
      includes: d.includes || '',
      excludes: d.excludes || '',
      detail: d.detail || '',
      video: Boolean(d.videoUrl),
      itinerary: days,
      gallery: {
        hotel: images.slice(0, 2),
        food: images.slice(2, 4),
        spot: images.slice(0, 3)
      },
      loaded: true
    }
  }
  return {
    ...card,
    title: d.title || card.title,
    cover: d.cover || card.cover,
    normalPrice: Number(d.normalPrice || card.normalPrice),
    vipPrice: Number(d.vipPrice || card.vipPrice),
    price: Number(d.price || card.price),
    sales: Number(d.sales || card.sales),
    spec: d.subtitle || card.spec,
    desc: d.subtitle || card.desc,
    detail: d.detail || '',
    images,
    stock: Number(d.stock || 0),
    loaded: true
  }
}

/** 自驾游「途经城市」清单：取每日行程标题（后端不额外存 ways 字段） */
function selfDriveWays(d) {
  if (d.routeType !== 'SELF_DRIVE') return []
  return (d.dayList || []).map((item) => item.title).filter(Boolean)
}

function toVipProduct(card) {
  if (!card) return null
  return {
    id: card.productId,
    type: '权益',
    title: card.title,
    kicker: 'VIP MEMBERSHIP',
    copy: card.subtitle || copy.vip398,
    price: Number(card.price || card.normalPrice || VIP_PRICE),
    refPrice: 398000,
    benefits: ['线路与特产双价中的 VIP 价', '专属 AI 相册网站（独立域名）', '相册站入口：按参团批次查看旅行相册'],
    cover: card.cover || IMAGES.poster
  }
}

function toOrder(o) {
  const routeLike = o.productType === 'ROUTE'
  return {
    id: o.orderId,
    orderNo: o.orderNo,
    userId: o.memberId,
    kind: routeLike ? '线路' : o.productType === 'VIP_RIGHTS' ? '权益' : '商品',
    itemId: o.productId,
    title: o.productTitle || '',
    spec: `${o.quantity || 1} 件${o.travelDate ? ' · 出行 ' + String(o.travelDate).substring(0, 10) : ''}`,
    quantity: o.quantity || 1,
    unitPrice: Number(o.unitPrice || 0),
    amount: Number(o.payAmount || o.amount || 0),
    status: ORDER_STATUS_TEXT[o.status] || '未知',
    statusCode: o.status,
    contactName: o.contactName || '',
    contactPhone: o.contactPhone || '',
    travelDate: o.travelDate ? String(o.travelDate).substring(0, 10) : '',
    afterSale: o.afterSale || 0,
    refundReason: o.refundReason || '',
    createdAt: fmtTime(o.createTime),
    thumb: o.productCover || IMAGES.dali
  }
}

function toCommission(c) {
  return {
    id: c.commissionId,
    orderId: c.orderId,
    orderNo: c.orderNo,
    beneficiaryId: c.memberId,
    level: c.level,
    amount: Number(c.amount || 0),
    status: c.status === 2 ? '已回冲' : '已入账',
    fromUserId: c.memberId,
    productTitle: c.productTitle || '',
    createdAt: fmtTime(c.settleTime)
  }
}

function toWalletTxn(t) {
  return {
    id: t.txnId,
    type: TXN_TEXT[t.txnType] || t.txnType,
    rawType: t.txnType,
    amount: Number(t.amount || 0),
    fee: Number(t.fee || 0),
    balanceAfter: Number(t.balanceAfter || 0),
    counterparty: t.counterpartyName || t.remark || '—',
    status: TXN_STATUS[t.txnType] || '已入账',
    createdAt: fmtTime(t.createTime)
  }
}

function toAlbum(a) {
  const media = a.mediaList || []
  const photos = media.filter((m) => m.category !== 'VIDEO')
  const videos = media.filter((m) => m.category === 'VIDEO')
  const thumbs = (photos.length ? photos : media).map((m) => m.url).slice(0, 8)
  return {
    id: a.albumId,
    season: a.title,
    route: a.productTitle || '',
    travelDate: a.travelDate ? String(a.travelDate).substring(0, 10) : '',
    photos: photos.length,
    videos: videos.length,
    cover: a.cover || thumbs[0] || IMAGES.dali,
    thumbs,
    description: a.description || '',
    media
  }
}

function toDirect(d) {
  return {
    id: d.memberId,
    name: d.nickname,
    phone: d.phone,
    level: d.vipActive ? 'VIP' : '普通',
    vipActive: Boolean(d.vipActive),
    team: Number(d.teamCount || 0),
    direct: Number(d.directCount || 0),
    createdAt: d.createTime ? String(d.createTime).substring(0, 10) : ''
  }
}

function fmtTime(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').substring(0, 16)
}

/* ------------------------------------------------------------ 同步选择器 */
function userById(id) {
  return state.users.find((u) => String(u.id) === String(id)) || null
}

function userByPhone(phone) {
  return state.users.find((u) => u.phone === phone) || null
}

function maskPhone(phone) {
  if (!phone || phone.length < 11) return phone || '—'
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}

function priceFor(item) {
  if (!item) return 0
  // 后端已按当前身份算好适用价，本地只在缺失时按 guards 兜底
  if (item.price) return item.price
  return guards.priceFor(item, currentUser.value)
}

/** 团队 / 个人人数口径：团队含全部后代，个人仅直推 —— 由后端按 referrer_path 计算 */
function teamStats() {
  return {
    team: Number(state.share.teamCount || 0),
    direct: Number(state.share.directCount || 0),
    monthNew: Number(state.share.monthNew || 0)
  }
}

function directList() {
  return state.directs
}

function myOrders(status) {
  if (!status || status === '全部订单' || status === '全部') return state.orders
  return state.orders.filter((o) => o.status === status)
}

function myCommissions() {
  return state.commissions
}

function myWallet() {
  return state.wallet
}

function orderCounts() {
  const base = { 待付款: 0, 待收货: 0, 售后单: 0, 全部订单: state.orders.length }
  state.orders.forEach((o) => {
    if (base[o.status] != null) base[o.status] += 1
  })
  return base
}

function commissionTotal() {
  return Number(state.commissionSummary.total || 0)
}

/* ------------------------------------------------------------ 异步加载器 */
let bootPromise = null

/* 页面显示时的定向刷新（由 main.js 的全局 mixin 调用）：
   只刷新当前页面真正需要的切片，避免每次切页都打 6 个接口。
   600ms 内重复触发直接忽略（tab 快速来回切时很常见）。 */
const REFRESH_MAP = [
  { test: /^pages\/index\/index/, run: () => (state.routes.length ? null : refreshCatalog()) },
  { test: /^pages\/mall\/index/, run: () => (state.products.length ? null : refreshCatalog()) },
  { test: /^pages\/order\/(index|detail)/, run: () => withLogin(refreshOrders) },
  { test: /^pages\/mine\/index/, run: () => withLogin(() => Promise.all([refreshProfile(), refreshShare()])) },
  { test: /^pages\/share\/index/, run: () => withLogin(refreshShare) },
  { test: /^pages\/commission\/index/, run: () => withLogin(() => Promise.all([refreshCommissions(), refreshProfile()])) },
  { test: /^pages\/wallet\/(income|recharge|withdraw)/, run: () => withLogin(() => Promise.all([refreshWallet(), refreshProfile()])) },
  { test: /^pages\/album\/(index|detail)/, run: () => withLogin(refreshAlbums) },
  { test: /^pages\/vip\/index/, run: () => withLogin(refreshProfile) },
  { test: /^pages\/poster\/index/, run: () => withLogin(refreshShare) }
]

let lastRefresh = 0

function withLogin(fn) {
  return state.loggedIn ? fn() : null
}

function onPageShow(path) {
  if (!path) return null
  const now = Date.now()
  if (now - lastRefresh < 600) return null
  const hit = REFRESH_MAP.find((item) => item.test.test(path))
  if (!hit) return null
  lastRefresh = now
  const result = hit.run()
  if (result && result.catch) result.catch(() => {})
  return result
}

/** 启动期一次性加载：公开口径 + 目录（登录态数据由 refreshAccount 负责） */
function boot() {
  if (bootPromise) return bootPromise
  bootPromise = (async () => {
    await Promise.all([refreshConfig().catch(() => {}), refreshCatalog().catch(() => {})])
    if (state.loggedIn) {
      await refreshAccount()
    }
    state.booted = true
  })().catch(() => {
    bootPromise = null
  })
  return bootPromise
}

async function refreshConfig() {
  const res = await catalogApi.config()
  const data = res.data || {}
  state.notice = data.notice || ''
  if (data.vipPrice) {
    copy.vip398 = '价值 3980 元的 AI 智能相册网站，升级 VIP 会员仅需 ' + money.fmt(Number(data.vipPrice))
  }
  return data
}

async function refreshCatalog() {
  const res = await catalogApi.home()
  const home = res.data || {}
  state.vipProduct = toVipProduct(home.vipRights)
  state.routes = (home.routes || []).map(toRouteCard)
  state.products = (home.specials || []).map(toProductCard)
  if (home.notice) state.notice = home.notice
  // 详情（行程分段 / 图集）后台补齐，不阻塞首屏渲染
  prefetchDetails()
  return home
}

let prefetching = false

function prefetchDetails() {
  if (prefetching) return
  prefetching = true
  const tasks = [
    ...state.routes.map((r) => ensureDetail(r.id, true)),
    ...state.products.map((p) => ensureDetail(p.id, false))
  ]
  Promise.all(tasks)
    .catch(() => {})
    .finally(() => {
      prefetching = false
    })
}

/** 详情补全：详情页进入时调用，未加载时才发请求 */
async function ensureDetail(productId, isRoute) {
  if (isRoute === undefined) {
    isRoute = state.routes.some((item) => String(item.id) === String(productId))
  }
  const list = isRoute ? state.routes : state.products
  const index = list.findIndex((item) => String(item.id) === String(productId))
  if (index < 0) return null
  if (list[index].loaded) return list[index]
  const res = await catalogApi.detail(productId)
  const merged = mergeDetail(list[index], res.data || {}, isRoute)
  const next = list.slice()
  next[index] = merged
  if (isRoute) state.routes = next
  else state.products = next
  return merged
}

/** 登录态相关的全部数据（并行拉取，单个失败不影响其它模块） */
async function refreshAccount() {
  return Promise.allSettled([
    refreshProfile(),
    refreshOrders(),
    refreshCommissions(),
    refreshWallet(),
    refreshAlbums(),
    refreshShare()
  ])
}

async function refreshProfile() {
  const res = await memberApi.profile()
  state.member = toMember(res.data)
  syncUsers()
  return state.member
}

async function refreshOrders() {
  const res = await orderApi.list({ pageNum: 1, pageSize: 50 })
  state.orders = (res.rows || []).map(toOrder)
  return state.orders
}

async function refreshOrderDetail(orderId) {
  const res = await orderApi.detail(orderId)
  state.orderDetail = toOrder(res.data)
  return state.orderDetail
}

async function refreshCommissions() {
  const [list, summary] = await Promise.all([
    memberApi.commissionList({ pageNum: 1, pageSize: 50 }),
    memberApi.commissionSummary()
  ])
  state.commissions = (list.rows || []).map(toCommission)
  state.commissionSummary = summary.data || state.commissionSummary
  return state.commissions
}

async function refreshWallet() {
  const [summary, txns] = await Promise.all([
    walletApi.summary(),
    walletApi.transactions({ pageNum: 1, pageSize: 50 })
  ])
  state.walletSummary = summary.data || state.walletSummary
  state.wallet = (txns.rows || []).map(toWalletTxn)
  if (state.member && summary.data) {
    state.member.balance = Number(summary.data.balance || 0)
    state.member.frozen = Number(summary.data.frozen || 0)
  }
  return state.wallet
}

async function refreshAlbums() {
  const res = await catalogApi.albums()
  state.albums = (res.data || []).map(toAlbum)
  return state.albums
}

async function albumDetail(albumId) {
  const res = await catalogApi.albumDetail(albumId)
  return toAlbum(res.data)
}

async function refreshShare() {
  const [share, directs] = await Promise.all([
    memberApi.share(),
    memberApi.directs({ pageNum: 1, pageSize: 50 })
  ])
  state.share = share.data || state.share
  state.directs = (directs.rows || []).map(toDirect)
  state.inviteFrom = state.share.referrerName || state.inviteFrom
  syncUsers()
  return state.share
}

async function refreshInviteQr(size) {
  const res = await memberApi.inviteQr(size)
  state.inviteQr = (res.data || {}).dataUrl || ''
  return state.inviteQr
}

/** 通讯录：当前用户 + 直推，供互转收款人 / 推荐人展示复用 */
function syncUsers() {
  const list = []
  if (state.member) {
    list.push({
      id: state.member.id,
      name: state.member.name,
      phone: state.member.phone,
      level: state.member.level,
      region: state.member.region,
      referrerId: null,
      hidden: false
    })
  }
  state.directs.forEach((d) => {
    list.push({
      id: d.id,
      name: d.name,
      phone: d.phone,
      level: d.level,
      region: [],
      referrerId: state.member ? state.member.id : null,
      hidden: false
    })
  })
  state.users = list
}

/* ------------------------------------------------------------------ 动作 */
function toast(msg) {
  state.toast = msg
  uni.showToast({ title: msg, icon: 'none', duration: 1800 })
}

function fail(message) {
  return { ok: false, message }
}

/** 注册：推荐人码必填（佣金链路前置条件），成功即登录 */
async function register(form) {
  const errors = {}
  if (!form.referrerCode) errors.referrerCode = copy.referrerRequired
  if (!form.name) errors.name = '请填写名称'
  if (!form.phone) errors.phone = '请填写账号 / 手机号'
  else if (!/^1[3-9]\d{9}$/.test(form.phone)) errors.phone = '手机号格式不正确'
  if (!form.region || form.region.length !== 3 || !form.region[2]) errors.region = '请选择省 / 市 / 区（需选到区级）'
  if (!form.password) errors.password = '请设置密码'
  else if (form.password.length < 6) errors.password = '密码至少 6 位'
  if (Object.keys(errors).length) return { ok: false, errors }

  try {
    const res = await authApi.register({
      referrerCode: form.referrerCode,
      nickname: form.name,
      phone: form.phone,
      password: form.password,
      province: form.region[0],
      city: form.region[1],
      district: form.region[2]
    })
    const data = res.data || {}
    if (data.token) setToken(data.token)
    state.loggedIn = true
    if (data.member) state.member = toMember(data.member)
    await refreshAccount()
    state.inviteFrom = (state.member && state.member.referrerName) || state.inviteFrom
    return { ok: true, user: state.member }
  } catch (e) {
    return { ok: false, message: e.message, errors: { phone: e.message } }
  }
}

async function login(phone, password) {
  if (!phone) return fail('请填写手机号')
  if (!password) return fail('请填写密码')
  try {
    const res = await authApi.login({ phone, password })
    const data = res.data || {}
    if (data.token) setToken(data.token)
    state.loggedIn = true
    if (data.member) state.member = toMember(data.member)
    await refreshAccount()
    return { ok: true, user: state.member }
  } catch (e) {
    return fail(e.message)
  }
}

/** 登出：先通知后端把 token_version +1，再清本地令牌（接口失败也必须清本地） */
async function logout() {
  try {
    await authApi.logout()
  } catch (e) {
    /* 令牌可能已过期，忽略即可 */
  }
  setToken('')
  state.loggedIn = false
  state.member = null
  state.users = []
  state.orders = []
  state.orderDetail = null
  state.commissions = []
  state.wallet = []
  state.albums = []
  state.directs = []
  state.share = { teamCount: 0, directCount: 0, monthNew: 0, inviteCode: '', referrerName: '', notice: '' }
  state.commissionSummary = { total: 0, direct: 0, indirect: 0, pending: 0, count: 0 }
  state.walletSummary = { balance: 0, frozen: 0, commissionTotal: 0, rechargeTotal: 0, withdrawTotal: 0 }
  bootPromise = null
}

/* 充值：登记后由运营核对到账（单笔 ≤ 5000 元） */
async function recharge(yuan) {
  const cents = money.toCents(yuan)
  const check = guards.checkRecharge(cents)
  if (!check.ok) return check
  try {
    const res = await walletApi.recharge({ amount: cents })
    return { ok: true, cents, apply: res.data }
  } catch (e) {
    return fail(e.message)
  }
}

/* 互转：requestId 作为幂等键，网络重试不会重复扣款 */
async function transfer(phone, yuan) {
  const cents = money.toCents(yuan)
  const check = guards.checkTransfer(cents, currentUser.value.balance)
  if (!check.ok) return check
  if (phone === currentUser.value.phone) return fail('不能转给自己')
  try {
    await walletApi.transfer({
      toPhone: phone,
      amount: cents,
      requestId: 'T' + Date.now() + Math.floor(Math.random() * 1000)
    })
    await Promise.all([refreshWallet(), refreshProfile().catch(() => {})])
    return { ok: true, cents }
  } catch (e) {
    return fail(e.message)
  }
}

/* 提现：6% 手续费（服务端按分向下取整），提交后进入人工审核 */
async function withdraw(yuan, account) {
  const cents = money.toCents(yuan)
  const check = guards.checkWithdraw(cents, currentUser.value.balance)
  if (!check.ok) return check
  if (!account || !account.accountName) return fail('请填写收款人姓名')
  if (!/^\d{12,25}$/.test(account.accountNo || '')) return fail('收款账号应为 12-25 位数字')
  if (!account.bankName) return fail('请填写开户行')
  try {
    const res = await walletApi.withdraw({
      amount: cents,
      accountName: account.accountName,
      accountNo: account.accountNo,
      bankName: account.bankName
    })
    const apply = res.data || {}
    await refreshWallet()
    const fee = Number(apply.fee || 0)
    const arrive = Number(apply.actualAmount || cents - fee)
    return { ok: true, settle: { amount: cents, fee, arrive, actual: arrive } }
  } catch (e) {
    return fail(e.message)
  }
}

/** 提现试算（不改状态，只算手续费与到账金额） */
async function withdrawQuote(cents) {
  try {
    const res = await walletApi.withdrawQuote(cents)
    return { ok: true, quote: res.data }
  } catch (e) {
    return fail(e.message)
  }
}

/* 下单：价格一律由服务端按当前身份取值，前端只传商品与数量 */
async function createOrder({ itemId, quantity = 1, contactName, contactPhone, travelDate, remark }) {
  if (!state.loggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return fail('请先登录')
  }
  try {
    const res = await orderApi.create({
      productId: itemId,
      quantity,
      contactName: contactName || currentUser.value.name,
      contactPhone: contactPhone || currentUser.value.phone,
      travelDate: travelDate || null,
      remark: remark || ''
    })
    const order = toOrder(res.data)
    await refreshOrders()
    return { ok: true, order }
  } catch (e) {
    return fail(e.message)
  }
}

async function payOrder(orderId) {
  try {
    await orderApi.pay(orderId)
    await Promise.all([refreshOrders(), refreshWallet(), refreshProfile().catch(() => {})])
    await refreshOrderDetail(orderId).catch(() => {})
    return { ok: true }
  } catch (e) {
    if (/余额/.test(e.message)) return { ok: false, code: 'INSUFFICIENT', message: e.message }
    return fail(e.message)
  }
}

async function cancelOrder(orderId) {
  try {
    await orderApi.cancel(orderId)
    await refreshOrders()
    return { ok: true }
  } catch (e) {
    return fail(e.message)
  }
}

async function confirmOrder(orderId) {
  try {
    await orderApi.confirm(orderId)
    await refreshOrders()
    return { ok: true }
  } catch (e) {
    return fail(e.message)
  }
}

async function applyAfterSale(orderId, reason) {
  try {
    await orderApi.afterSale(orderId, reason)
    await refreshOrders()
    return { ok: true }
  } catch (e) {
    return fail(e.message)
  }
}

/* 升级 VIP：下单 398 权益 → 余额支付 → 服务端升级并触发两级计佣 */
async function upgradeVip() {
  const vip = state.vipProduct
  if (!vip) return fail('会员权益暂时不可用，请稍后重试')
  const created = await createOrder({ itemId: vip.id, quantity: 1 })
  if (!created.ok) return created
  const paid = await payOrder(created.order.id)
  if (!paid.ok) return { ...paid, order: created.order }
  await Promise.all([refreshProfile(), refreshCommissions(), refreshShare()])
  return { ok: true, order: created.order }
}

/* 续费 99 元/年（Q-03：默认不计佣，服务端在返回值里给出说明） */
async function renewVip() {
  try {
    const res = await memberApi.renewVip()
    await Promise.all([refreshProfile(), refreshWallet(), refreshCommissions()])
    const data = res.data || {}
    const expireAt = data.vipExpireAt ? String(data.vipExpireAt).substring(0, 10) : ''
    return { ok: true, expireAt, note: data.note }
  } catch (e) {
    if (/余额/.test(e.message)) return { ok: false, code: 'INSUFFICIENT', message: e.message }
    return fail(e.message)
  }
}

async function changePassword(oldPwd, newPwd) {
  if (!oldPwd) return fail('请填写原密码')
  if (!newPwd || newPwd.length < 6) return fail('新密码至少 6 位')
  try {
    await authApi.changePassword({ oldPassword: oldPwd, newPassword: newPwd })
    // 改密后服务端 token_version +1，旧令牌全部失效，必须重新登录
    setToken('')
    state.loggedIn = false
    return { ok: true, relogin: true }
  } catch (e) {
    return fail(e.message)
  }
}

async function resetPassword(phone, newPwd, nickname) {
  if (!phone) return fail('请填写手机号')
  if (!nickname) return fail('请填写注册时的名称')
  if (!newPwd || newPwd.length < 6) return fail('新密码至少 6 位')
  try {
    await authApi.forgot({ phone, nickname, newPassword: newPwd })
    return { ok: true }
  } catch (e) {
    return fail(e.message)
  }
}

export default {
  state,
  currentUser,
  isVip,
  vipState,
  userById,
  userByPhone,
  maskPhone,
  priceFor,
  teamStats,
  directList,
  myOrders,
  myCommissions,
  myWallet,
  orderCounts,
  commissionTotal,
  /* 加载器 */
  boot,
  onPageShow,
  refreshConfig,
  refreshCatalog,
  refreshAccount,
  refreshProfile,
  refreshOrders,
  refreshOrderDetail,
  refreshCommissions,
  refreshWallet,
  refreshAlbums,
  refreshShare,
  refreshInviteQr,
  albumDetail,
  ensureDetail,
  /* 动作 */
  register,
  login,
  logout,
  recharge,
  transfer,
  withdraw,
  withdrawQuote,
  createOrder,
  payOrder,
  cancelOrder,
  confirmOrder,
  applyAfterSale,
  upgradeVip,
  renewVip,
  changePassword,
  resetPassword,
  toast,
  money,
  RECHARGE_MAX,
  VIP_PRICE
}
