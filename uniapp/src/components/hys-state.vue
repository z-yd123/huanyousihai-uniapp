<template>
  <view class="state">
    <!-- ST-L 加载：与真实内容同构的骨架屏，不用全屏遮罩转圈 -->
    <view v-if="type === 'loading'" class="sk-wrap">
      <view v-for="i in rows" :key="i" class="sk-card card">
        <view class="sk sk-img" />
        <view class="sk-body">
          <view class="sk sk-line" style="width: 62%" />
          <view class="sk sk-line" style="width: 40%; margin-top: 14rpx" />
          <view class="sk sk-price" />
        </view>
      </view>
    </view>

    <view v-else class="box">
      <hys-icon :name="iconName" :size="64" :tone="iconTone" />
      <text class="st-title">{{ title }}</text>
      <text v-if="desc" class="st-desc">{{ desc }}</text>
      <view v-if="actionText" class="st-act btn btn-secondary btn-sm" @tap="$emit('action')">
        <text>{{ actionText }}</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'hys-state',
  props: {
    type: { type: String, default: 'empty' }, // loading | empty | error | success
    title: { type: String, default: '' },
    desc: { type: String, default: '' },
    actionText: { type: String, default: '' },
    rows: { type: Number, default: 2 }
  },
  emits: ['action'],
  computed: {
    iconName() {
      if (this.type === 'error') return 'error'
      if (this.type === 'success') return 'success'
      return 'refresh'
    },
    iconTone() {
      if (this.type === 'error') return 'danger'
      if (this.type === 'success') return 'success'
      return 'disabled'
    }
  }
}
</script>

<style scoped>
.state { padding: 8rpx 0; }
.sk-wrap { padding-top: 8rpx; }
.sk-card {
  overflow: hidden;
  margin-bottom: 28rpx;
}
.sk-img {
  width: 100%;
  height: 380rpx;
  border-radius: 0;
}
.sk-body { padding: 24rpx; }
.sk-line { height: 28rpx; }
.sk-price { height: 24rpx; width: 46%; margin-top: 22rpx; }

.box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 96rpx 48rpx;
}
.st-title {
  margin-top: 24rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: var(--c-ink);
  text-align: center;
}
.st-desc {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: var(--c-slate);
  text-align: center;
  line-height: 1.6;
}
.st-act {
  margin-top: 32rpx;
  padding: 0 40rpx;
}
</style>
