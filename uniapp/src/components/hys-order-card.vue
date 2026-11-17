<template>
  <view class="ocard card" @tap="$emit('tap', order)">
    <view class="top">
      <view class="thumb">
        <image v-if="order.thumb" class="thumb-img" :src="order.thumb" mode="aspectFill" />
        <view v-else class="ph thumb-ph" />
      </view>
      <view class="info">
        <text class="o-title">{{ order.title }}</text>
        <view class="tags">
          <view class="kind">{{ order.kind }}</view>
          <hys-badge :label="order.status" :tone="tone" />
        </view>
      </view>
    </view>
    <view class="bottom">
      <hys-amount :value="order.amount" cents size="m" class="o-amt" />
      <view class="detail" @tap.stop="$emit('tap', order)">
        <text class="detail-t">查看详情</text>
        <hys-icon name="chevron-right" :size="28" tone="slate" />
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'hys-order-card',
  props: {
    order: { type: Object, default: () => ({}) }
  },
  emits: ['tap'],
  computed: {
    tone() {
      const s = this.order.status
      if (s === '待付款') return 'warning'
      if (s === '待收货') return 'info'
      if (s === '已完成') return 'success'
      if (s === '售后单' || s === '售后中') return 'danger'
      return 'slate'
    }
  }
}
</script>

<style scoped>
.ocard {
  padding: 24rpx;
  margin-bottom: 24rpx;
}
.top {
  display: flex;
  gap: 20rpx;
}
.thumb {
  width: 120rpx;
  height: 120rpx;
  border-radius: 12rpx;
  overflow: hidden;
  background: var(--c-mist);
  flex-shrink: 0;
}
.thumb-img { width: 100%; height: 100%; }
.thumb-ph { width: 100%; height: 100%; border-radius: 0; }
.info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 4rpx 0;
}
.o-title {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--c-ink);
  line-height: 1.4;
}
.tags {
  display: flex;
  align-items: center;
  gap: 12rpx;
}
.kind {
  font-size: 22rpx;
  color: var(--c-slate);
}
.bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 24rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid var(--c-mist);
}
.detail {
  display: flex;
  align-items: center;
  gap: 4rpx;
}
.detail-t {
  font-size: 24rpx;
  color: var(--c-slate);
}
</style>
