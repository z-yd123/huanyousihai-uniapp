<template>
  <view class="ticket card">
    <view class="stub">
      <text class="stub-k">DAY</text>
      <text class="stub-n">{{ dayNo }}</text>
    </view>
    <view class="notch notch-top" />
    <view class="notch notch-bottom" />
    <view class="main">
      <text class="t-title">{{ item.title }}</text>
      <view v-if="item.meet" class="meet">
        <hys-icon name="clock" :size="26" tone="slate" />
        <text class="meet-t">{{ item.meet }}</text>
      </view>
      <text class="copy">{{ item.copy }}</text>
      <view v-if="item.img" class="photo">
        <image class="photo-img" :src="item.img" mode="aspectFill" />
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'hys-day',
  props: {
    item: { type: Object, default: () => ({}) }
  },
  computed: {
    dayNo() {
      const d = String(this.item.day || 'DAY 01')
      const m = d.match(/\d+/)
      return m ? m[0] : '01'
    }
  }
}
</script>

<style scoped>
.ticket {
  position: relative;
  display: flex;
  gap: 0;
  overflow: hidden;
  margin-bottom: 24rpx;
}
.stub {
  width: 120rpx;
  flex-shrink: 0;
  background: var(--c-pine);
  color: var(--c-on-pine);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 26rpx 0;
}
.stub-k {
  font-size: 20rpx;
  letter-spacing: 2rpx;
  opacity: 0.8;
  color: var(--c-on-pine);
}
.stub-n {
  font-size: 40rpx;
  font-weight: 700;
  line-height: 1.2;
  color: var(--c-on-pine);
  font-variant-numeric: tabular-nums;
}
.notch {
  position: absolute;
  left: 120rpx;
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  background: var(--c-sand);
  transform: translateX(-50%);
  z-index: 2;
}
.notch-top { top: -10rpx; }
.notch-bottom { bottom: -10rpx; }
.main {
  flex: 1;
  min-width: 0;
  padding: 24rpx 24rpx 24rpx 28rpx;
  border-left: 2rpx dashed var(--c-mist);
}
.t-title {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--c-ink);
  line-height: 1.4;
}
.meet {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 10rpx;
}
.meet-t {
  font-size: 22rpx;
  color: var(--c-slate);
}
.copy {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  line-height: 1.7;
  color: var(--c-slate);
}
.photo {
  margin-top: 16rpx;
  height: 0;
  padding-bottom: 42%;
  position: relative;
  border-radius: 12rpx;
  overflow: hidden;
  background: var(--c-mist);
}
.photo-img {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
}
</style>
