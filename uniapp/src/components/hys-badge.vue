<template>
  <view class="badge" :class="['badge-' + tone, solid ? 'solid' : '']">
    <text class="badge-t">{{ label }}</text>
  </view>
</template>

<script>
export default {
  name: 'hys-badge',
  props: {
    label: { type: String, default: '' },
    tone: { type: String, default: 'slate' }, // success | warning | danger | info | pine | bronze | disabled | slate
    solid: { type: Boolean, default: false }
  }
}
</script>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  font-weight: 500;
  line-height: 1.3;
  white-space: nowrap;
  flex-shrink: 0;
}
.badge-t { font-size: 22rpx; font-weight: 500; }

.badge-success { background: rgba(46, 107, 79, 0.1); }
.badge-success .badge-t { color: var(--c-success); }
.badge-warning { background: rgba(149, 99, 18, 0.12); }
.badge-warning .badge-t { color: var(--c-warning); }
.badge-danger { background: rgba(178, 59, 46, 0.1); }
.badge-danger .badge-t { color: var(--c-danger); }
.badge-info { background: rgba(44, 90, 122, 0.1); }
.badge-info .badge-t { color: var(--c-info); }
.badge-pine { background: rgba(31, 92, 75, 0.1); }
.badge-pine .badge-t { color: var(--c-pine); }
.badge-bronze { background: var(--c-vip-surface); }
.badge-bronze .badge-t { color: var(--c-vip-bronze); }
.badge-disabled { background: var(--c-disabled-surface); }
.badge-disabled .badge-t { color: var(--c-disabled); }
.badge-slate { background: var(--c-disabled-surface); }
.badge-slate .badge-t { color: var(--c-slate); }

/* 实心（直推 / 间推 徽标） */
.badge.solid { padding: 8rpx 16rpx; }
.badge.solid .badge-t { color: #fff; }
.badge-pine.solid { background: var(--c-pine); }
.badge-pine.solid .badge-t { color: var(--c-on-pine); }
.badge-bronze.solid { background: var(--c-vip-bronze); }
.badge-bronze.solid .badge-t { color: #fffdf8; }
</style>
