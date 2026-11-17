<template>
  <view class="nav">
    <view class="nav-status" :style="{ height: statusH + 'px' }" />
    <view class="nav-bar">
      <view v-if="back" class="nav-left" @tap="goBack">
        <hys-icon name="chevron-left" :size="44" tone="ink" />
      </view>
      <view v-else class="nav-left" />
      <text class="nav-title">{{ title }}</text>
      <view class="nav-right">
        <slot name="right" />
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'hys-navbar',
  props: {
    title: { type: String, default: '' },
    back: { type: Boolean, default: false }
  },
  data() {
    let statusH = 20
    try {
      statusH = uni.getSystemInfoSync().statusBarHeight || 20
    } catch (e) {}
    return { statusH }
  },
  methods: {
    goBack() {
      const pages = getCurrentPages()
      if (pages.length > 1) uni.navigateBack()
      else uni.switchTab({ url: '/pages/index/index' })
    }
  }
}
</script>

<style scoped>
.nav {
  position: sticky;
  top: 0;
  z-index: 10;
  background: var(--c-sand);
}
.nav-bar {
  height: 88rpx;
  display: flex;
  align-items: center;
  padding: 0 16rpx;
}
.nav-left,
.nav-right {
  width: 120rpx;
  display: flex;
  align-items: center;
}
.nav-right {
  justify-content: flex-end;
}
.nav-title {
  flex: 1;
  text-align: center;
  font-size: 32rpx;
  font-weight: 600;
  color: var(--c-ink);
  letter-spacing: 1rpx;
}
</style>
