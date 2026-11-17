<template>
  <view class="page">
    <hys-navbar title="环游四海 · 相册" back />

    <view class="wrap">
      <view class="hero">
        <text class="serif hero-t">一辈子的美好回忆</text>
        <text class="hero-s">按参团批次归档的旅行相册 · 衬线标题 + 大留白</text>
        <text class="hero-c">价值 3980 元的 AI 智能相册网站，升级 VIP 会员仅需 398 元</text>
      </view>

      <view class="cover">
        <image v-if="latest" class="cover-img" :src="latest.cover" mode="aspectFill" />
        <view v-else class="ph cover-ph"><text>最近参团封面 · 16:10</text></view>
      </view>

      <view class="sec">
        <view class="sec-l">
          <text class="sec-title">按参团批次</text>
        </view>
        <text class="sec-more">{{ albums.length }} 个批次</text>
      </view>

      <hys-state
        v-if="!albums.length"
        type="empty"
        title="还没有旅行相册，参团后运营会为你上传"
        desc="参团后由运营按批次代传照片与视频"
        action-text="查看线路"
        @action="goRoutes"
      />
      <view v-else class="list">
        <view v-for="a in albums" :key="a.id" @tap="open(a)">
          <hys-album-timeline :album="a" />
        </view>
      </view>

      <hys-notice class="mt16" type="plain" text="运营按参团批次代传照片 / 视频，会员用自己的账号登录浏览" />
      <view class="bottom-pad" />
    </view>
  </view>
</template>

<script>
import store from '@/store/index.js'

export default {
  computed: {
    albums() {
      return store.state.albums
    },
    latest() {
      return this.albums[0]
    }
  },
  methods: {
    open(a) {
      uni.navigateTo({ url: '/pages/album/detail?id=' + a.id })
    },
    goRoutes() {
      uni.switchTab({ url: '/pages/index/index' })
    }
  }
}
</script>

<style scoped>
.hero { padding: 32rpx 0 28rpx; }
.hero-t {
  display: block;
  font-size: 60rpx;
  font-weight: 700;
  color: var(--c-ink);
  line-height: 1.15;
}
.hero-s {
  display: block;
  margin-top: 14rpx;
  font-size: 22rpx;
  color: var(--c-slate);
}
.hero-c {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: var(--c-terracotta);
}
.cover {
  position: relative;
  width: 100%;
  height: 0;
  padding-bottom: 62.5%;
  border-radius: 20rpx;
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
.sec-l { display: flex; align-items: baseline; }
.list { display: flex; flex-direction: column; }
.mt16 { margin-top: 16rpx; }
.bottom-pad { height: 60rpx; }
</style>
