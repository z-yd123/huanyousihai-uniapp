/**
 * 环游四海管理端公用常量与格式化工具。
 * 金额一律以「分」在接口层传输，展示时统一除以 100，避免前端出现浮点误差。
 */

export const PRODUCT_TYPE = {
  ROUTE: '旅游线路',
  SPECIAL: '特产商品',
  VIP_RIGHTS: '398 权益'
}

export const ROUTE_TYPE = {
  GROUP: '组团游',
  SELF_DRIVE: '自驾游'
}

export const ORDER_STATUS = {
  0: { text: '待付款', type: 'info' },
  1: { text: '待收货/待出行', type: 'warning' },
  2: { text: '已完成', type: 'success' },
  3: { text: '售后中', type: 'danger' },
  4: { text: '已退款', type: 'info' },
  5: { text: '已取消', type: 'info' }
}

export const AFTER_SALE_STATUS = {
  0: '无',
  1: '申请中',
  2: '已退款',
  3: '已驳回'
}

export const MEMBER_LEVEL = {
  0: { text: '普通会员', type: 'info' },
  1: { text: 'VIP 会员', type: 'warning' }
}

export const MEMBER_STATUS = {
  0: { text: '正常', type: 'success' },
  1: { text: '停用', type: 'info' },
  2: { text: '异常待补绑', type: 'danger' }
}

export const COMMISSION_LEVEL = {
  DIRECT: { text: '直推', type: 'success' },
  INDIRECT: { text: '间推', type: 'warning' }
}

export const APPLY_TYPE = {
  RECHARGE: '充值登记',
  WITHDRAW: '提现申请'
}

export const APPLY_STATUS = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '已通过', type: 'success' },
  2: { text: '已驳回', type: 'info' }
}

/** 分 → 元字符串（两位小数） */
export function yuan(cents) {
  if (cents === null || cents === undefined || cents === '') {
    return '0.00'
  }
  return (Number(cents) / 100).toFixed(2)
}

/** 元 → 分（整数），用于表单提交 */
export function cents(value) {
  if (value === null || value === undefined || value === '') {
    return 0
  }
  return Math.round(Number(value) * 100)
}

export function levelTag(level) {
  return MEMBER_LEVEL[level] || { text: '未知', type: 'info' }
}

export function statusTag(status) {
  return ORDER_STATUS[status] || { text: '未知', type: 'info' }
}
