<template>
  <view class="vipb" :class="['vipb-' + state]">
    <text class="vipb-t">{{ label }}</text>
  </view>
</template>

<script>
export default {
  name: 'hys-vip-badge',
  props: {
    state: { type: String, default: 'active' } // active | soon | expired | normal
  },
  computed: {
    label() {
      if (this.state === 'active') return 'VIP 有效'
      if (this.state === 'soon') return 'VIP 即将到期'
      if (this.state === 'expired') return 'VIP 已过期'
      return '普通会员'
    }
  }
}
</script>

<style scoped>
.vipb {
  display: inline-flex;
  align-items: center;
  height: 40rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  border: 1rpx solid var(--c-vip-bronze);
  white-space: nowrap;
  flex-shrink: 0;
}
.vipb-t {
  font-size: 22rpx;
  font-weight: 500;
  color: var(--c-vip-bronze);
}
.vipb-soon {
  border-color: var(--c-warning);
}
.vipb-soon .vipb-t { color: var(--c-warning); }
.vipb-expired {
  border-color: var(--c-mist);
}
.vipb-expired .vipb-t { color: var(--c-slate); }
.vipb-normal {
  border-color: var(--c-mist);
}
.vipb-normal .vipb-t { color: var(--c-slate); }
</style>
