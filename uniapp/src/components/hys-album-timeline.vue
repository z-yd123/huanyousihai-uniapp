<template>
  <view class="alb" :style="{ width: colWidth }">
    <view class="cover">
      <image v-if="album.cover" class="cover-img" :src="album.cover" mode="aspectFill" />
      <view v-else class="ph cover-ph" />
      <view v-if="album.videos" class="play">
        <hys-icon name="play" :size="32" :hex="'#FFFDF8'" />
      </view>
    </view>
    <view class="meta">
      <text class="season">{{ album.season }}</text>
      <text class="route">{{ album.route }}</text>
      <text class="count">照片 {{ album.photos }} / 视频 {{ album.videos }}</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'hys-album-timeline',
  props: {
    album: { type: Object, default: () => ({}) },
    columns: { type: Number, default: 1 }
  },
  computed: {
    colWidth() {
      if (this.columns >= 4) return 'calc(25% - 18rpx)'
      if (this.columns === 3) return 'calc(33.333% - 16rpx)'
      if (this.columns === 2) return 'calc(50% - 12rpx)'
      return '100%'
    }
  }
}
</script>

<style scoped>
.alb {
  margin-bottom: 24rpx;
}
.cover {
  position: relative;
  width: 100%;
  height: 0;
  padding-bottom: 62.5%;
  border-radius: 16rpx;
  overflow: hidden;
  background: var(--c-mist);
}
.cover-img {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
}
.cover-ph {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  border-radius: 0;
}
.play {
  position: absolute;
  right: 14rpx;
  bottom: 14rpx;
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  background: rgba(28, 43, 38, 0.42);
  display: flex;
  align-items: center;
  justify-content: center;
}
.meta {
  padding: 14rpx 4rpx 0;
}
.season {
  font-size: 20rpx;
  color: var(--c-vip-bronze);
  letter-spacing: 1rpx;
}
.route {
  display: block;
  margin-top: 4rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: var(--c-ink);
}
.count {
  display: block;
  margin-top: 4rpx;
  font-size: 22rpx;
  color: var(--c-slate);
}
</style>
