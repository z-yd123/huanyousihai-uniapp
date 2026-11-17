<template>
  <view class="crow card">
    <view class="head">
      <hys-badge :label="label" :tone="tone" solid />
      <hys-amount :value="item.amount" cents size="s" class="price" />
      <hys-badge :label="item.status" :tone="statusTone" />
    </view>
    <view class="meta">
      <view class="kv-line">
        <text class="k">来源订单</text>
        <text class="v num">{{ item.orderId }}</text>
      </view>
      <view class="kv-line">
        <text class="k">计佣口径</text>
        <text class="v">两级封顶</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'hys-commission-row',
  props: {
    item: { type: Object, default: () => ({ level: 'DIRECT', amount: 0, status: '已入账' }) }
  },
  computed: {
    label() {
      return this.item.level === 'DIRECT' ? '直推' : '间推'
    },
    tone() {
      return this.item.level === 'DIRECT' ? 'pine' : 'bronze'
    },
    statusTone() {
      if (this.item.status === '已入账') return 'success'
      if (this.item.status === '待发放') return 'warning'
      return 'disabled'
    }
  }
}
</script>

<style scoped>
.crow {
  padding: 20rpx 24rpx 8rpx;
  margin-bottom: 20rpx;
}
.head {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid var(--c-mist);
}
.head .price {
  flex: 1;
  color: var(--c-ink);
}
.meta { padding-bottom: 12rpx; }
.kv-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12rpx 0;
}
.k {
  font-size: 24rpx;
  color: var(--c-slate);
}
.v {
  font-size: 24rpx;
  color: var(--c-ink);
}
</style>
