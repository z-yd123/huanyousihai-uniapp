<template>
  <view class="poster">
    <view class="canvas">
      <image class="land" :src="landscape" mode="aspectFill" />
      <view class="shade" />
      <view class="qr-box">
        <view class="qr-mask">
          <hys-icon name="qr" :size="72" tone="ink" />
        </view>
        <text class="qr-t">个人小程序码</text>
        <text class="qr-s">清晰区 ≥100px</text>
      </view>
      <view class="caption">
        <text class="cap-name">{{ name }} 的邀请</text>
        <text class="cap-code">邀请码 {{ inviteCode }}</text>
      </view>
      <view v-if="generating" class="gen">
        <view class="gen-ring" />
        <text class="gen-t">海报生成中…</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'hys-poster',
  props: {
    landscape: { type: String, default: '' },
    name: { type: String, default: '会员' },
    inviteCode: { type: String, default: '' },
    generating: { type: Boolean, default: false }
  }
}
</script>

<style scoped>
.canvas {
  position: relative;
  width: 100%;
  height: 0;
  padding-bottom: 128%;
  border-radius: 24rpx;
  overflow: hidden;
  background: #fffdf8;
  border: 1rpx solid rgba(228, 224, 213, 0.9);
}
.land {
  position: absolute;
  left: 12rpx;
  right: 12rpx;
  top: 12rpx;
  width: calc(100% - 24rpx);
  height: calc(100% - 24rpx);
  border-radius: 16rpx;
}
.shade {
  position: absolute;
  left: 12rpx;
  right: 12rpx;
  bottom: 12rpx;
  height: 40%;
  border-radius: 16rpx;
  background: linear-gradient(180deg, rgba(28, 43, 38, 0) 0%, rgba(28, 43, 38, 0.42) 100%);
}
.qr-box {
  position: absolute;
  right: 44rpx;
  bottom: 132rpx;
  width: 240rpx;
  padding: 16rpx;
  background: #fffdf8;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 8rpx 24rpx rgba(28, 43, 38, 0.16);
}
.qr-mask {
  width: 150rpx;
  height: 150rpx;
  border-radius: 8rpx;
  background: #f6f2e9;
  display: flex;
  align-items: center;
  justify-content: center;
}
.qr-t {
  margin-top: 10rpx;
  font-size: 20rpx;
  color: #816338;
  font-weight: 600;
}
.qr-s {
  font-size: 18rpx;
  color: #55615a;
}
.caption {
  position: absolute;
  left: 44rpx;
  bottom: 44rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.cap-name {
  font-size: 26rpx;
  font-weight: 600;
  color: #fffdf8;
}
.cap-code {
  font-size: 22rpx;
  color: rgba(255, 253, 248, 0.86);
  font-variant-numeric: tabular-nums;
}
.gen {
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: rgba(28, 43, 38, 0.35);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
}
@keyframes spin { to { transform: rotate(360deg); } }
.gen-ring {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 253, 248, 0.35);
  border-top-color: #fffdf8;
  animation: spin 0.9s linear infinite;
}
.gen-t {
  font-size: 24rpx;
  color: #fffdf8;
}
</style>
