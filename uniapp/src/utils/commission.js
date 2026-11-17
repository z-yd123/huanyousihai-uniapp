/* ============================================================================
   环游四海 V1.0 · 佣金（提成）算法引擎
   口径来源：AGENT.md §6（2026-11-13 已冻结）
   ---------------------------------------------------------------------------
   硬约束（COMPLIANCE）
     1. 分销层级只有两级：DIRECT 直推 + INDIRECT 间推；无第三级、不顺延、不改派。
     2. 每笔 398 升级订单拨比恒为 200.00 元。
     3. 直推序号 1、2 → 直推 100 + 间推 100；序号 ≥ 3 → 直推 200 + 间推 0。
     4. 提现手续费 6%，按分计算向下取整到分。
   ========================================================================== */

export const MAX_DEPTH = 2            // COMPLIANCE: 两级是国家红线，写成常量而非配置
export const PAYOUT_TOTAL = 20000     // 分：每单拨比 200.00 元
export const VIP_PRICE = 39800        // 分：398.00 元
export const VIP_RENEW = 9900         // 分：99.00 元/年（Q-03 比例待定）
export const RECHARGE_MAX = 500000    // 分：单笔充值上限 5000.00 元
export const WITHDRAW_FEE_RATE = 0.06 // COMPLIANCE: 6%

export const CONFIG = {
  payout_total: PAYOUT_TOTAL,
  first_two_second_tier: 'to_indirect_100',
  withdraw_fee_rate: WITHDRAW_FEE_RATE,
  recharge_single_max: RECHARGE_MAX,
  renew_commission_rate: 0,          // Q-03 待确认，默认不计佣
  vip_expire_affects_commission: false // Q-04b 待确认
}

/* ------------------------------------------------------------ 金额工具（分） */
export const money = {
  toCents(yuan) {
    return Math.round(Number(yuan) * 100)
  },
  toYuan(cents) {
    return cents / 100
  },
  /* ¥1,234.00 */
  fmt(cents, withSymbol) {
    const n = (cents / 100).toFixed(2)
    const parts = n.split('.')
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    return (withSymbol === false ? '' : '¥') + parts.join('.')
  },
  /* 1,234.00（不带符号） */
  plain(cents) {
    const n = (cents / 100).toFixed(2)
    const parts = n.split('.')
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    return parts.join('.')
  }
}

/* 提现手续费：6%，按分向下取整 */
export function withdrawFee(cents) {
  if (!(cents > 0)) return 0
  // COMPLIANCE: 整数运算，3333 分 × 6 ÷ 100 = 199.98 → 199 分
  return Math.floor((cents * 6) / 100)
}

export function withdrawSettle(cents) {
  const fee = withdrawFee(cents)
  return {
    amount: cents,
    fee,
    arrive: cents - fee,
    feeRate: CONFIG.withdraw_fee_rate,
    expr: money.fmt(cents) + ' × 6% = ' + money.fmt(fee)
  }
}

/* ------------------------------------------------- §6.1 拨比拆分（纯函数） */
export function payoutFor(directIndex) {
  if (!Number.isInteger(directIndex) || directIndex < 1) {
    throw new Error('directIndex 必须为 >= 1 的整数，收到：' + directIndex)
  }
  if (directIndex <= 2) {
    return { direct: 10000, indirect: 10000 }
  }
  return { direct: 20000, indirect: 0 }
}

/* ---------------------------------------------------- 计佣主流程（升级 398） */
export function settleUpgrade(input) {
  const amount = input.amountCents == null ? VIP_PRICE : input.amountCents
  const hasReferrer = !!input.referrerId
  const hasIndirect = !!input.referrerOfReferrerId

  /* COMPLIANCE: 间推人必然存在（注册强制绑定推荐人）
     取不到 → 数据异常 → 中断计佣并告警（禁止顺延 / 改派 / 静默少发） */
  if (!hasReferrer || !hasIndirect) {
    const split = hasReferrer && input.directIndex >= 1 ? payoutFor(input.directIndex) : null
    return {
      ok: false,
      aborted: true,
      lines: [],
      totalPaid: 0,
      beneficiaryCount: 0,
      maxDepth: 0,
      gap: {
        orderId: input.orderId,
        newUserId: input.newUserId,
        reason: 'referrer_missing',
        amount: split ? split.indirect : PAYOUT_TOTAL,
        note: hasReferrer ? '推荐关系缺失：直推人无上级' : '推荐关系缺失：订单会员无推荐人'
      },
      alert: '计佣已中断：推荐关系缺失（referrer_missing），请补绑后重算'
    }
  }

  const split2 = payoutFor(input.directIndex)
  const lines = [
    {
      beneficiaryId: input.referrerId,
      level: 'DIRECT',
      depth: 1,
      amount: split2.direct,
      orderId: input.orderId,
      status: 'PENDING'
    }
  ]

  if (split2.indirect > 0) {
    lines.push({
      beneficiaryId: input.referrerOfReferrerId,
      level: 'INDIRECT',
      depth: 2,
      amount: split2.indirect,
      orderId: input.orderId,
      status: 'PENDING'
    })
  }

  const record = {
    ok: true,
    aborted: false,
    orderId: input.orderId,
    newUserId: input.newUserId,
    amountCents: amount,
    directIndex: input.directIndex,
    lines,
    totalPaid: lines.reduce((s, l) => s + l.amount, 0),
    beneficiaryCount: lines.length,
    maxDepth: lines.reduce((m, l) => Math.max(m, l.depth), 0),
    gap: null
  }

  assertRecord(record)
  return record
}

/* -------------------------------------------------- 99 元续费（Q-03 待定） */
export function settleRenew(input) {
  const rate = CONFIG.renew_commission_rate
  if (!rate) {
    return {
      ok: false,
      aborted: true,
      pending: 'Q-03',
      lines: [],
      totalPaid: 0,
      beneficiaryCount: 0,
      maxDepth: 0,
      gap: null,
      alert: 'Q-03 未结论：99 元续费提成比例待定，当前不计佣'
    }
  }
  const amount = Math.floor(VIP_RENEW * rate)
  const lines = [
    {
      beneficiaryId: input.referrerId,
      level: 'DIRECT',
      depth: 1,
      amount,
      orderId: input.orderId,
      status: 'PENDING'
    }
  ]
  return { ok: true, aborted: false, lines, totalPaid: amount, beneficiaryCount: 1, maxDepth: 1, gap: null }
}

/* -------------------------------------------------------------- 断言层 */
export function assertRecord(r) {
  const errs = []
  // COMPLIANCE: 每单恰好拨满 200 元
  if (r.totalPaid !== PAYOUT_TOTAL) errs.push('拨比异常：合计 ' + r.totalPaid + ' 分 ≠ 20000 分')
  // COMPLIANCE: 只有两级
  if (r.maxDepth > MAX_DEPTH) errs.push('层级异常：出现第 ' + r.maxDepth + ' 级受益人')
  r.lines.forEach((l) => {
    if (l.level !== 'DIRECT' && l.level !== 'INDIRECT') errs.push('层级枚举非法：' + l.level)
  })
  // COMPLIANCE: 同一人不得同时拿直推奖与间推奖
  const ids = r.lines.map((l) => l.beneficiaryId)
  if (new Set(ids).size !== ids.length) errs.push('受益人重复')
  if (errs.length) {
    r.complianceErrors = errs
    throw new Error('[COMPLIANCE] ' + errs.join('；'))
  }
  return true
}

/* ------------------------------------------------------------------ 校验 */
export const guards = {
  checkRecharge(cents) {
    if (!(cents > 0)) return { ok: false, message: '请输入充值金额' }
    if (cents > RECHARGE_MAX) {
      return {
        ok: false,
        code: 'RECHARGE_LIMIT',
        message: '单笔充值不超过 5000 元，请拆分为多次充值',
        suggestion: Math.ceil(cents / RECHARGE_MAX)
      }
    }
    return { ok: true }
  },
  checkWithdraw(cents, balance) {
    if (!(cents > 0)) return { ok: false, message: '请输入提现金额' }
    if (cents > balance) return { ok: false, code: 'INSUFFICIENT', message: '余额不足' }
    return { ok: true }
  },
  checkTransfer(cents, balance) {
    if (!(cents > 0)) return { ok: false, message: '请输入转账金额' }
    if (cents > balance) return { ok: false, code: 'INSUFFICIENT', message: '余额不足，可通过充值后完成互转' }
    return { ok: true }
  },
  /* Q-04 已确认：不续费不再享受 VIP 价，取价回落普通价 */
  isVipActive(user, now) {
    if (!user || user.level !== 'VIP') return false
    if (!user.vipExpireAt) return true
    return new Date(user.vipExpireAt.replace(/-/g, '/')).getTime() > (now || Date.now())
  },
  priceFor(item, user, now) {
    return guards.isVipActive(user, now) ? item.vipPrice : item.normalPrice
  },
  /* C-20 VIP 三态：有效 / 即将到期（≤30 天）/ 已过期 */
  vipState(user, now) {
    if (!user || user.level !== 'VIP' || !user.vipExpireAt) return 'none'
    const left = new Date(user.vipExpireAt.replace(/-/g, '/')).getTime() - (now || Date.now())
    if (left <= 0) return 'expired'
    if (left <= 30 * 86400000) return 'soon'
    return 'active'
  }
}

export default {
  MAX_DEPTH,
  PAYOUT_TOTAL,
  VIP_PRICE,
  VIP_RENEW,
  RECHARGE_MAX,
  CONFIG,
  money,
  payoutFor,
  withdrawFee,
  withdrawSettle,
  settleUpgrade,
  settleRenew,
  assertRecord,
  guards
}
