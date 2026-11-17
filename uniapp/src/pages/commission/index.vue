<template>
  <view class="page">
    <hys-navbar title="佣金明细" back />

    <view class="wrap">
      <hys-notice type="info" text="两级封顶、合法合规" />

      <view class="total card">
        <text class="total-k">本期佣金合计</text>
        <view class="total-row">
          <hys-amount :value="total" cents size="l" class="total-amt" />
          <text class="total-s">仅直推 / 间推两级</text>
        </view>
      </view>

      <view class="sec">
        <view class="sec-l">
          <text class="sec-no">01</text>
          <text class="sec-title">明细</text>
        </view>
        <text class="sec-more">{{ rows.length }} 笔</text>
      </view>

      <hys-state
        v-if="!rows.length"
        type="empty"
        title="暂无佣金记录"
        desc="仅直推 / 间推两级；每笔 398 订单拨比合计恒为 200 元"
        action-text="查看我的分享"
        @action="goShare"
      />
      <hys-commission-row v-for="c in rows" :key="c.id" :item="c" />

      <hys-notice class="mt16" type="plain" text="每笔 398 订单拨比恒为 200 元：直推序号 1、2 为直推 100 + 间推 100；第 3 个起直推 200、间推 0" />
      <view class="bottom-pad" />
    </view>
  </view>
</template>

<script>
import store from '@/store/index.js'

export default {
  computed: {
    rows() {
      return store.myCommissions().filter((c) => c.amount > 0)
    },
    total() {
      return this.rows.filter((c) => c.status === '已入账').reduce((s, c) => s + c.amount, 0)
    }
  },
  methods: {
    goShare() {
      uni.navigateTo({ url: '/pages/share/index' })
    }
  }
}
</script>

<style scoped>
.total {
  margin-top: 20rpx;
  padding: 24rpx;
}
.total-k {
  font-size: 24rpx;
  color: var(--c-slate);
}
.total-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 8rpx;
}
.total-amt { color: var(--c-ink); }
.total-s {
  font-size: 22rpx;
  color: var(--c-slate);
}
.sec-l { display: flex; align-items: baseline; }
.mt16 { margin-top: 16rpx; }
.bottom-pad { height: 60rpx; }
</style>
