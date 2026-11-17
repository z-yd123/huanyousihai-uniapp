/* ============================================================================
   环游四海 V1.0 · 前端静态词典（不含任何业务数据）

   业务数据全部来自后端 /app/**；本文件只保留三样东西：
   - IMAGES：本地占位图（后端返回的封面缺失时兜底）
   - regions：注册页省市三级联动字典（前端交互字典，非业务数据）
   - copy：合规文案兜底（运行时会用 /app/public/config 的配置覆盖）
   ========================================================================== */

const IMG = {
  dali: '/static/img/route-dali.jpg',
  daocheng: '/static/img/route-daocheng.jpg',
  erhai: '/static/img/route-erhai.jpg',
  miao: '/static/img/route-miao.jpg',
  mushroom: '/static/img/product-mushroom.jpg',
  tiedye: '/static/img/product-tiedye.jpg',
  tea: '/static/img/product-tea.jpg',
  flower: '/static/img/product-flower.jpg',
  poster: '/static/img/poster-landscape.jpg'
}

export const IMAGES = IMG

export const regions = {
  云南省: {
    昆明市: ['五华区', '盘龙区', '官渡区', '西山区'],
    大理白族自治州: ['大理市', '洱源县', '剑川县'],
    丽江市: ['古城区', '玉龙纳西族自治县']
  },
  四川省: {
    成都市: ['锦江区', '武侯区', '青羊区', '高新区'],
    甘孜藏族自治州: ['稻城县', '康定市', '理塘县']
  },
  贵州省: {
    黔东南苗族侗族自治州: ['凯里市', '雷山县', '镇远县']
  },
  广东省: {
    广州市: ['天河区', '越秀区', '海珠区'],
    深圳市: ['南山区', '福田区', '宝安区']
  },
  浙江省: {
    杭州市: ['西湖区', '余杭区', '拱墅区']
  },
  北京市: {
    北京市: ['朝阳区', '海淀区', '东城区']
  },
  湖北省: {
    武汉市: ['洪山区', '江汉区', '武昌区', '江夏区'],
    宜昌市: ['西陵区', '夷陵区']
  }
}

/* 合规文案兜底：值来自 hys_config，接口不可用时用这里的默认口径 */
export const copy = {
  vip398: '价值 3980 元的 AI 智能相册网站，升级 VIP 会员仅需 398 元',
  twoLevel: '两级封顶、合法合规',
  commissionRule: '仅直推 / 间推两级',
  withdrawFee: '提现手续费 6%，按分计算向下取整到分',
  withdrawAdvice: '建议用互转或升级 VIP 消费，避免手续费与个人所得税',
  withdrawAudit: '提现将进入人工审核',
  rechargeLimit: '单笔充值不超过 5000 元，请拆分为多次充值',
  rechargeHelp: '单笔不超过 5000 元，超额需拆分',
  referrerRequired: '请填写推荐人码，无邀请码无法注册',
  referrerLocked: '推荐关系为佣金前置；扫码注册成功直达首页',
  referrerRequireTip: '推荐关系为必填项',
  vipExpire: '不续费不再享受 VIP 会员价，取价回落普通会员价；会员资格、团队位置与推荐网络关系保留',
  gapAlert: '计佣已中断：推荐关系缺失（referrer_missing），请补绑后重算',
  teamDef: '团队人数＝全部后代；个人人数＝仅直推（口径固定）',
  emptyRoutes: '暂无可报名的线路',
  emptyOrders: '还没有订单',
  emptyDirect: '还没有直推会员，分享海报邀请第一位吧',
  emptyIncome: '暂无收入明细',
  emptyCommission: '暂无佣金记录',
  emptyAlbum: '还没有旅行相册，参团后运营会为你上传'
}
