<template>
  <view class="rcard card" @tap="$emit('tap', item)">
    <view class="cover">
      <image v-if="item.cover" class="cover-img" :src="item.cover" mode="aspectFill" />
      <view v-else class="cover-ph ph"><text>线路封面 · 16:10</text></view>
      <view class="cover-veil" />
    </view>
    <view class="body">
      <view class="line1">
        <view class="tag">{{ item.type }}</view>
        <text class="days">{{ item.days }} 天 {{ item.nights }} 晚</text>
      </view>
      <text class="title">{{ item.title }}</text>
      <view class="prices">
        <view class="p-left">
          <text class="p-k">普通价</text>
          <hys-amount :value="item.normalPrice" cents size="s" class="price-normal" />
        </view>
        <view class="p-right">
          <text class="p-k p-k-vip">VIP 价</text>
          <hys-amount :value="item.vipPrice" cents size="s" class="price-vip" />
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'hys-route-card',
  props: {
    item: { type: Object, default: () => ({}) }
  },
  emits: ['tap']
}
</script>

<style scoped>
.rcard {
  overflow: hidden;
  margin-bottom: 28rpx;
}
.cover {
  position: relative;
  width: 100%;
  height: 0;
  padding-bottom: 62.5%;
  background: var(--c-mist);
}
.cover-img {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
}
.cover-ph {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  border-radius: 0;
  background: var(--c-mist);
}
.cover-veil {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background: rgba(28, 43, 38, 0.04);
}
.body {
  padding: 20rpx 24rpx 24rpx;
}
.line1 {
  display: flex;
  align-items: center;
  gap: 12rpx;
}
.tag {
  height: 38rpx;
  padding: 0 12rpx;
  border: 1rpx solid var(--c-pine);
  border-radius: 6rpx;
  font-size: 20rpx;
  color: var(--c-pine);
  display: inline-flex;
  align-items: center;
}
.days {
  font-size: 22rpx;
  color: var(--c-slate);
}
.title {
  display: block;
  margin-top: 12rpx;
  font-size: 32rpx;
  font-weight: 600;
  color: var(--c-ink);
  line-height: 1.35;
}
.prices {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 16rpx;
}
.p-left,
.p-right {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}
.p-k {
  font-size: 22rpx;
  color: var(--c-slate);
}
.p-k-vip { color: var(--c-vip-bronze); }
</style>
