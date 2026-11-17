<template>
  <view class="page">
    <hys-navbar title="VIP 权益" back />

    <view class="wrap">
      <view class="hero">
        <hys-vip-badge :state="badgeState" />
        <text class="hero-t">{{ vip.title }}</text>
        <text class="hero-s">有效期 · {{ user.vipExpireAt || '—' }}</text>
      </view>

      <view class="sec">
        <view class="sec-l">
          <text class="sec-no">01</text>
          <text class="sec-title">权益说明</text>
        </view>
      </view>
      <view class="group card">
        <view class="row">
          <view class="row-main">
            <text class="row-t">VIP 会员价</text>
            <text class="row-d">线路与特产双价中的 VIP 价</text>
          </view>
        </view>
        <view class="row">
          <view class="row-main">
            <text class="row-t">专属 AI 相册网站</text>
            <text class="row-d">价值 3980 元的 AI 智能相册网站，升级 VIP 会员仅需 398 元</text>
          </view>
        </view>
        <view class="row" @tap="go('/pages/album/index')">
          <view class="row-main">
            <text class="row-t">相册站入口</text>
            <text class="row-d">按参团批次查看旅行相册</text>
          </view>
          <hys-icon name="chevron-right" :size="30" tone="slate" />
        </view>
      </view>

      <view class="sec">
        <view class="sec-l">
          <text class="sec-no">02</text>
          <text class="sec-title">续费</text>
        </view>
      </view>
      <view class="renew card">
        <view class="renew-row">
          <text class="renew-t">第二年起 99 元/年</text>
          <hys-amount :value="9900" cents size="s" class="price-vip" />
        </view>
        <text class="renew-s">续费权益与旅游绑定：相册继续代运营上传</text>
        <view class="btn btn-primary mt16" @tap="renew">
          <text>续费</text>
        </view>
      </view>

      <hys-notice class="mt20" type="info" text="不续费不再享受 VIP 会员价，取价回落普通会员价；会员资格、团队位置与推荐网络关系保留" />
      <view class="bottom-pad" />
    </view>
  </view>
</template>

<script>
import store from '@/store/index.js'

export default {
  data() {
    return { renewing: false }
  },
  computed: {
    user() {
      return store.currentUser.value
    },
    vip() {
      return store.state.vipProduct
    },
    badgeState() {
      const s = store.vipState.value
      if (s === 'active' || s === 'soon' || s === 'expired') return s
      return 'normal'
    }
  },
  methods: {
    go(url) {
      uni.navigateTo({ url })
    },
    async renew() {
      if (this.renewing) return
      this.renewing = true
      const r = await store.renewVip()
      this.renewing = false
      if (!r.ok) {
        uni.showModal({
          title: '余额不足',
          content: r.message,
          confirmText: '去充值',
          success: (res) => {
            if (res.confirm) uni.navigateTo({ url: '/pages/wallet/recharge?tab=recharge' })
          }
        })
        return
      }
      uni.showModal({
        title: '续费成功',
        content: '有效期已延长至 ' + (r.expireAt || '—') + (r.note ? '\n' + r.note : ''),
        showCancel: false
      })
    }
  }
}
</script>

<style scoped>
.hero {
  margin-top: 12rpx;
  padding: 28rpx 24rpx;
  border-radius: 24rpx;
  background: var(--c-vip-surface);
  border: 1rpx solid rgba(129, 99, 56, 0.22);
}
.hero-t {
  display: block;
  margin-top: 16rpx;
  font-size: 40rpx;
  font-weight: 700;
  color: var(--c-ink);
}
.hero-s {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: var(--c-vip-bronze);
}
.sec-l { display: flex; align-items: baseline; }
.group { padding: 0 24rpx; }
.renew { padding: 24rpx; }
.renew-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}
.renew-t {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--c-ink);
}
.renew-s {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: var(--c-slate);
}
.mt16 { margin-top: 16rpx; }
.mt20 { margin-top: 20rpx; }
.bottom-pad { height: 60rpx; }
</style>
