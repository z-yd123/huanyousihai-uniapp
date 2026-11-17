<template>
  <view class="ss card">
    <view class="ss-col">
      <text class="ss-v">{{ referrer || '—' }}</text>
      <text class="ss-k">推荐人</text>
    </view>
    <view class="ss-sep" />
    <view class="ss-col">
      <text class="ss-v num">{{ teamCount }}</text>
      <text class="ss-k">团队人数</text>
    </view>
    <view class="ss-sep" />
    <view class="ss-col">
      <text class="ss-v num">{{ directCount }}</text>
      <text class="ss-k">个人人数</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'hys-share-stat',
  props: {
    referrer: { type: String, default: '' },
    teamCount: { type: [Number, String], default: 0 },
    directCount: { type: [Number, String], default: 0 }
  }
}
</script>

<style scoped>
.ss {
  display: flex;
  align-items: center;
  padding: 32rpx 0;
}
.ss-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}
.ss-v {
  font-size: 40rpx;
  font-weight: 700;
  color: var(--c-ink);
  line-height: 1.2;
}
.ss-k {
  font-size: 22rpx;
  color: var(--c-slate);
}
.ss-sep {
  width: 1rpx;
  height: 56rpx;
  background: var(--c-mist);
}
</style>
