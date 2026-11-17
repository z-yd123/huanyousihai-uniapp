<template>
  <view class="pp">
    <view class="pp-item">
      <text class="pp-k">普通价</text>
      <hys-amount :value="normalPrice" cents size="s" class="price-normal" />
    </view>
    <view class="pp-item">
      <text class="pp-k pp-k-vip">VIP 价</text>
      <hys-amount :value="vipPrice" cents size="s" class="price-vip" />
    </view>
  </view>
</template>

<script>
export default {
  name: 'hys-price-pair',
  props: {
    normalPrice: { type: Number, default: 0 },
    vipPrice: { type: Number, default: 0 },
    current: { type: String, default: '' }
  }
}
</script>

<style scoped>
.pp {
  display: flex;
  align-items: center;
  gap: 24rpx;
  flex-wrap: wrap;
}
.pp-item {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}
.pp-k {
  font-size: 22rpx;
  color: var(--c-slate);
}
.pp-k-vip { color: var(--c-vip-bronze); }
</style>
