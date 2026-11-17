<template>
  <view class="pcard card" @tap="$emit('tap', item)">
    <view class="cover">
      <image v-if="item.cover" class="cover-img" :src="item.cover" mode="aspectFill" />
      <view v-else class="ph cover-ph"><text>1:1 商品图</text></view>
    </view>
    <view class="body">
      <text class="title">{{ item.title }}</text>
      <view class="prices">
        <view class="row1">
          <text class="k-vip">VIP</text>
          <hys-amount :value="item.vipPrice" cents size="s" class="price-vip" />
        </view>
        <view class="row2">
          <text class="k">普通价</text>
          <hys-amount :value="item.normalPrice" cents size="s" class="price-normal" />
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'hys-product-card',
  props: {
    item: { type: Object, default: () => ({}) }
  },
  emits: ['tap']
}
</script>

<style scoped>
.pcard {
  overflow: hidden;
}
.cover {
  position: relative;
  width: 100%;
  height: 0;
  padding-bottom: 100%;
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
}
.body {
  padding: 18rpx 20rpx 22rpx;
}
.title {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--c-ink);
  line-height: 1.4;
}
.prices {
  margin-top: 12rpx;
}
.row1 {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}
.k-vip {
  font-size: 20rpx;
  color: var(--c-vip-bronze);
  font-weight: 600;
}
.row2 {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
  margin-top: 4rpx;
}
.k {
  font-size: 20rpx;
  color: var(--c-slate);
}
</style>
