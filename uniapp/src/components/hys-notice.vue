<template>
  <view class="notice" :class="'notice-' + type">
    <hys-icon :name="iconName" :size="34" :tone="iconTone" />
    <text class="notice-t">{{ text }}</text>
  </view>
</template>

<script>
export default {
  name: 'hys-notice',
  props: {
    text: { type: String, default: '' },
    type: { type: String, default: 'info' } // info | vip | danger | plain
  },
  computed: {
    iconName() {
      if (this.type === 'danger') return 'error'
      if (this.type === 'vip') return 'gift'
      return 'info'
    },
    iconTone() {
      if (this.type === 'danger') return 'danger'
      if (this.type === 'vip') return 'bronze'
      return 'info'
    }
  }
}
</script>

<style scoped>
.notice {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  padding: 18rpx 20rpx;
  border-radius: 16rpx;
  border: 1rpx solid var(--c-mist);
  background: var(--c-paper);
}
.notice-t {
  flex: 1;
  font-size: 22rpx;
  line-height: 1.5;
  color: var(--c-slate);
}
.notice-info {
  border-color: rgba(44, 90, 122, 0.35);
}
.notice-info .notice-t { color: var(--c-info); }
.notice-danger {
  border-color: rgba(178, 59, 46, 0.4);
}
.notice-danger .notice-t { color: var(--c-danger); }
.notice-vip {
  border-color: rgba(129, 99, 56, 0.35);
  background: transparent;
}
.notice-vip .notice-t { color: var(--c-vip-bronze); }
</style>
