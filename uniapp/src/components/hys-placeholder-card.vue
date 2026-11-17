<template>
  <view class="pc card">
    <view class="badge-row">
      <view class="ph-badge"><text class="ph-badge-t">即将上线</text></view>
    </view>
    <text class="pc-name">{{ feature }}</text>
    <text class="pc-desc">该功能将在后续版本上线，1.0 仅保留入口</text>
    <view class="pc-btn btn btn-primary" @tap="$emit('back')">
      <text>返回个人中心</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'hys-placeholder-card',
  props: {
    feature: { type: String, default: '绿色积分' }
  },
  emits: ['back']
}
</script>

<style scoped>
.pc {
  padding: 40rpx 32rpx 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 32rpx;
}
.badge-row { margin-bottom: 24rpx; }
.ph-badge {
  padding: 6rpx 20rpx;
  border-radius: 8rpx;
  background: var(--c-disabled-surface);
}
.ph-badge-t {
  font-size: 22rpx;
  color: var(--c-disabled);
}
.pc-name {
  font-size: 36rpx;
  font-weight: 700;
  color: var(--c-ink);
}
.pc-desc {
  margin-top: 14rpx;
  font-size: 24rpx;
  color: var(--c-slate);
  text-align: center;
  line-height: 1.6;
}
.pc-btn {
  width: 100%;
  margin-top: 36rpx;
}
</style>
