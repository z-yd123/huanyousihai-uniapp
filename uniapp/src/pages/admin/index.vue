<template>
  <view class="page">
    <hys-navbar title="运营后台入口" back />

    <view class="wrap">
      <view class="hero card">
        <text class="hero-k">ADMIN CONSOLE</text>
        <text class="hero-t">后台是独立的 Vue3 管理端</text>
        <text class="hero-d">
          环游四海采用「uni-app 小程序端 + Vue3 管理端 + Spring Boot 服务端」三端分离，后台菜单、权限、资金审核都在
          ruoyi-vue3 管理端完成。小程序端只提供会员自助能力，不内置任何后台操作入口。
        </text>
      </view>

      <view class="sec">
        <view class="sec-l">
          <text class="sec-no">01</text>
          <text class="sec-title">管理端能力范围</text>
        </view>
      </view>
      <view class="mods card">
        <view v-for="m in modules" :key="m.name" class="mrow">
          <text class="mk">{{ m.name }}</text>
          <text class="mv">{{ m.desc }}</text>
        </view>
      </view>

      <view class="sec">
        <view class="sec-l">
          <text class="sec-no">02</text>
          <text class="sec-title">如何进入</text>
        </view>
      </view>
      <view class="card steps">
        <view class="step">
          <text class="step-no">1</text>
          <text class="step-t">启动服务端 ruoyi-admin（默认 8080 端口）</text>
        </view>
        <view class="step">
          <text class="step-no">2</text>
          <text class="step-t">启动管理端 ruoyi-vue3（默认 80 端口，账号 admin / admin123）</text>
        </view>
        <view class="step">
          <text class="step-no">3</text>
          <text class="step-t">左侧菜单「环游四海」下可见 9 个业务模块</text>
        </view>
      </view>

      <view class="sec">
        <view class="sec-l">
          <text class="sec-no">03</text>
          <text class="sec-title">权限边界</text>
        </view>
      </view>
      <view class="card notes">
        <text class="note">小程序端令牌与后台令牌互不通用：小程序走 /app/** + 会员 JWT，后台走 /system/** + 管理员 JWT。</text>
        <text class="note">资金入账、佣金补发、推荐关系补绑只有后台能做，且全部留有操作日志。</text>
        <text class="note">异常对账（未发满 200 元的差额）在后台「佣金异常对账」页人工核销，小程序端不可见。</text>
      </view>

      <hys-notice class="mt20" type="info" text="分销层级固定两级（直推 + 间推），后台不提供任何放大层级的配置入口" />
      <view class="bottom-pad" />
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      modules: [
        { name: '运营看板', desc: '会员 / VIP / 订单 / GMV / 佣金汇总、近 7 日趋势、异常告警' },
        { name: '线路与商品', desc: '双价维护、上下架、按天行程录入、库存与销量' },
        { name: '会员管理', desc: '资料维护、异常账号补绑推荐人、重置密码' },
        { name: '订单管理', desc: '订单查询、发货、退款（退余额 + 回冲佣金 + 回滚库存）' },
        { name: '佣金明细', desc: '直推 / 间推两级明细、按订单追溯' },
        { name: '异常对账', desc: '佣金未发满 200 元的差额账本，人工核销' },
        { name: '资金审核', desc: '充值登记核对、提现审核（6% 手续费，通过即出款）' },
        { name: '相册管理', desc: '按参团批次代传素材、上下架相册站' },
        { name: '业务参数', desc: '拨比 / 费率 / 上限等配置，合规红线由服务端强校验' }
      ]
    }
  }
}
</script>

<style scoped>
.hero {
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}
.hero-k {
  font-size: 20rpx;
  letter-spacing: 2rpx;
  color: var(--c-slate);
}
.hero-t {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--c-ink);
}
.hero-d {
  font-size: 24rpx;
  line-height: 1.6;
  color: var(--c-slate);
}
.mods {
  padding: 8rpx 24rpx;
}
.mrow {
  display: flex;
  gap: 16rpx;
  padding: 18rpx 0;
  border-bottom: 1rpx solid var(--c-mist);
}
.mrow:last-child {
  border-bottom: none;
}
.mk {
  width: 160rpx;
  flex-shrink: 0;
  font-size: 26rpx;
  font-weight: 600;
  color: var(--c-ink);
}
.mv {
  flex: 1;
  font-size: 24rpx;
  line-height: 1.5;
  color: var(--c-slate);
}
.steps,
.notes {
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
.step {
  display: flex;
  align-items: center;
  gap: 14rpx;
}
.step-no {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: var(--c-pine);
  color: #fff;
  font-size: 22rpx;
  text-align: center;
  line-height: 36rpx;
  flex-shrink: 0;
}
.step-t {
  font-size: 24rpx;
  color: var(--c-ink);
}
.note {
  font-size: 24rpx;
  line-height: 1.6;
  color: var(--c-slate);
}
.mt20 {
  margin-top: 20rpx;
}
.bottom-pad {
  height: 40rpx;
}
</style>
