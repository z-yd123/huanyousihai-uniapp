<template>
  <view class="ff" :class="{ 'ff-error': !!error }">
    <view class="ff-label">
      <text v-if="index" class="ff-idx">{{ index }} ·</text>
      <text class="ff-t">{{ label }}</text>
      <text v-if="required" class="ff-req">（必填）</text>
    </view>
    <slot />
    <text v-if="error" class="ff-err">{{ error }}</text>
    <text v-else-if="help" class="ff-help">{{ help }}</text>
  </view>
</template>

<script>
export default {
  name: 'hys-field',
  props: {
    label: { type: String, default: '' },
    index: { type: [String, Number], default: '' },
    required: { type: Boolean, default: false },
    error: { type: String, default: '' },
    help: { type: String, default: '' }
  }
}
</script>

<style scoped>
.ff { margin-bottom: 28rpx; }
.ff-label {
  display: flex;
  align-items: baseline;
  margin-bottom: 12rpx;
}
.ff-idx {
  margin-right: 6rpx;
  font-size: 26rpx;
  color: var(--c-slate);
  font-weight: 600;
}
.ff-t {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--c-ink);
}
.ff-req {
  font-size: 24rpx;
  color: var(--c-slate);
}
.ff-err {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: var(--c-danger);
  line-height: 1.5;
}
.ff-help {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: var(--c-slate);
  line-height: 1.5;
}
</style>
