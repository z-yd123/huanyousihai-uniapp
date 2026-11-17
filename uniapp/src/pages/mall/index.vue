<template>
  <view class="page">
    <hys-navbar title="商城" />

    <view class="wrap">
      <!-- VIP 权益卡 -->
      <view class="vip-card">
        <view class="vip-top">
          <view class="vip-pill"><text class="vip-pill-t">VIP 权益</text></view>
        </view>
        <text class="vip-title">{{ vip.title }}</text>
        <hys-notice class="vip-notice" type="info" :text="vip.copy" />
        <view class="btn btn-vip vip-btn" @tap="upgrade">
          <text>{{ isVip ? '已开通 · 查看相册站' : '立即升级 · ¥398.00' }}</text>
        </view>
      </view>
      <text class="vip-note">权益说明：VIP 会员价 · 专属 AI 相册网站 · 相册站入口</text>

      <!-- 各地特产 -->
      <view class="sec">
        <view class="sec-l">
          <text class="sec-no">01</text>
          <text class="sec-title">各地特产</text>
        </view>
        <text class="sec-more" @tap="more">更多</text>
      </view>

      <hys-state v-if="loading" type="loading" :rows="1" />
      <view v-else class="grid">
        <hys-product-card
          v-for="p in products"
          :key="p.id"
          :item="p"
          class="grid-item"
          @tap="openProduct(p)"
        />
      </view>

      <!-- 余额提示 / 升级引导 -->
      <view class="bal-card card">
        <text class="bal-t">{{ balTip }}</text>
        <view class="bal-acts">
          <view class="btn btn-primary btn-sm bal-btn" @tap="go('/pages/wallet/recharge?tab=transfer')">
            <text>互转</text>
          </view>
          <view class="btn btn-vip btn-sm bal-btn" @tap="go('/pages/wallet/recharge?tab=recharge')">
            <text>充值</text>
          </view>
        </view>
      </view>

      <hys-notice class="mt24" type="plain" text="特产订单与线路订单统一进入订单中心：待付款 / 待收货 / 售后单 / 全部订单" />
      <view class="bottom-pad" />
    </view>
  </view>
</template>

<script>
import store from '@/store/index.js'

export default {
  data() {
    return { loading: true, upgrading: false }
  },
  computed: {
    vip() {
      return store.state.vipProduct
    },
    products() {
      return store.state.products
    },
    isVip() {
      return store.isVip.value
    },
    balance() {
      const u = store.currentUser.value
      return u ? u.balance : 0
    },
    balTip() {
      const yuan = (this.balance / 100).toFixed(2)
      if (this.isVip) return `VIP 权益已生效 · 当前余额 ¥${yuan}，可用于续费或购买特产`
      if (this.balance >= this.vip.price) return `余额 ¥${yuan} 已足够，可直接完成升级`
      return `余额 ¥${yuan} 不足，可通过互转或充值后完成升级`
    }
  },
  onLoad() {
    setTimeout(() => {
      this.loading = false
    }, 420)
  },
  methods: {
    more() {
      uni.showToast({ title: '已展示全部特产', icon: 'none' })
    },
    openProduct(p) {
      uni.navigateTo({ url: '/pages/product/detail?id=' + p.id })
    },
    go(url) {
      uni.navigateTo({ url })
    },
    async upgrade() {
      if (this.isVip) {
        uni.navigateTo({ url: '/pages/album/index' })
        return
      }
      if (this.upgrading) return
      this.upgrading = true
      const r = await store.upgradeVip()
      this.upgrading = false
      if (!r.ok) {
        uni.showModal({
          title: '余额不足',
          content: '余额不足，可通过互转或充值后完成升级',
          confirmText: '去充值',
          cancelText: '去互转',
          success: (res) => {
            uni.navigateTo({
              url: res.confirm ? '/pages/wallet/recharge?tab=recharge' : '/pages/wallet/recharge?tab=transfer'
            })
          }
        })
        return
      }
      // 佣金由服务端结算，这里只展示口径，不参与计算
      const tip = 'VIP 已开通，权益已生效。佣金按单笔 200 元两级封顶结算，可在「佣金明细」查看。'
      uni.showModal({
        title: 'VIP 已开通',
        content: tip,
        confirmText: '看相册站',
        cancelText: '知道了',
        success: (res) => {
          if (res.confirm) uni.navigateTo({ url: '/pages/album/index' })
        }
      })
    }
  }
}
</script>

<style scoped>
.vip-card {
  margin-top: 12rpx;
  padding: 28rpx 24rpx 24rpx;
  border-radius: 24rpx;
  background: var(--c-vip-surface);
  border: 1rpx solid rgba(129, 99, 56, 0.22);
}
.vip-pill {
  display: inline-flex;
  align-items: center;
  height: 36rpx;
  padding: 0 16rpx;
  border-radius: 999rpx;
  border: 1rpx solid var(--c-vip-bronze);
  margin-bottom: 16rpx;
}
.vip-pill-t {
  font-size: 20rpx;
  color: var(--c-vip-bronze);
}
.vip-title {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: var(--c-ink);
  letter-spacing: 0.5rpx;
}
.vip-notice {
  margin-top: 16rpx;
  background: transparent;
}
.vip-btn {
  margin-top: 20rpx;
}
.vip-note {
  display: block;
  margin-top: 14rpx;
  font-size: 22rpx;
  color: var(--c-slate);
}
.sec-l { display: flex; align-items: baseline; }
.grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}
.grid-item {
  width: calc(50% - 10rpx);
}
.bal-card {
  margin-top: 32rpx;
  padding: 24rpx;
}
.bal-t {
  font-size: 26rpx;
  color: var(--c-ink);
  line-height: 1.5;
}
.bal-acts {
  display: flex;
  gap: 16rpx;
  margin-top: 20rpx;
}
.bal-btn {
  flex: 1;
  height: 76rpx;
}
.mt24 { margin-top: 24rpx; }
.bottom-pad { height: 60rpx; }
</style>
