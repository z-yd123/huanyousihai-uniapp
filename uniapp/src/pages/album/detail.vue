<template>
  <view class="page">
    <hys-navbar :title="album ? album.route : '相册详情'" back />

    <view v-if="album" class="wrap">
      <view class="head">
        <text class="season">{{ album.season }}</text>
        <text class="route">{{ album.route }}</text>
        <text class="count">照片 {{ album.photos }} / 视频 {{ album.videos }}</text>
      </view>

      <!-- 响应式 1 → 2 → 3 → 4 列大图网格（手机 3 列） -->
      <view class="grid">
        <view v-for="(img, i) in album.thumbs" :key="i" class="cell" @tap="preview(i)">
          <image class="cell-img" :src="img" mode="aspectFill" />
          <view v-if="i === 2" class="video-cell">
            <hys-icon name="play" :size="30" :hex="'#FFFDF8'" />
          </view>
        </view>
      </view>

      <hys-notice class="mt16" type="plain" text="批次照片 · 视频播放入口；运营按批次代传，会员用自己的账号登录浏览" />
      <view class="bottom-pad" />
    </view>

    <hys-state v-else type="empty" title="还没有旅行相册，参团后运营会为你上传" />
  </view>
</template>

<script>
import store from '@/store/index.js'

export default {
  data() {
    return { album: null }
  },
  async onLoad(q) {
    const id = Number(q.id)
    this.album = store.state.albums.find((a) => String(a.id) === String(id)) || null
    try {
      this.album = (await store.albumDetail(id)) || this.album
    } catch (e) {
      if (!this.album) store.toast(e.message)
    }
  },
  methods: {
    preview(i) {
      const urls = this.album.media && this.album.media.length ? this.album.media.map((m) => m.url) : this.album.thumbs
      uni.previewImage({ current: i, urls })
    }
  }
}
</script>

<style scoped>
.head { padding: 20rpx 0 24rpx; }
.season {
  font-size: 22rpx;
  color: var(--c-vip-bronze);
  letter-spacing: 1rpx;
}
.route {
  display: block;
  margin-top: 6rpx;
  font-size: 40rpx;
  font-weight: 700;
  color: var(--c-ink);
}
.count {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: var(--c-slate);
}
.grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}
.cell {
  width: calc(33.333% - 6rpx);
  height: 0;
  padding-bottom: calc(33.333% - 6rpx);
  position: relative;
  border-radius: 8rpx;
  overflow: hidden;
  background: var(--c-mist);
}
.cell-img {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
}
.video-cell {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background: rgba(28, 43, 38, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
}
.mt16 { margin-top: 16rpx; }
.bottom-pad { height: 60rpx; }
</style>
