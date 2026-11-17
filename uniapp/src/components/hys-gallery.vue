<template>
  <view class="gal">
    <view class="gal-tabs">
      <view
        v-for="g in groups"
        :key="g.key"
        class="gal-tab"
        :class="{ on: g.key === active }"
        @tap="active = g.key"
      >
        <hys-icon :name="g.icon" :size="30" :tone="g.key === active ? 'pine' : 'slate'" />
        <text class="gal-tab-t">{{ g.label }}</text>
      </view>
    </view>
    <scroll-view class="gal-scroll" scroll-x :show-scrollbar="false">
      <view class="gal-row">
        <view v-for="(img, i) in images" :key="i" class="gal-item" @tap="preview(i)">
          <image v-if="img" class="gal-img" :src="img" mode="aspectFill" />
          <view v-else class="ph gal-ph"><text>素材图</text></view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  name: 'hys-gallery',
  props: {
    gallery: { type: Object, default: () => ({ hotel: [], food: [], spot: [] }) }
  },
  data() {
    return {
      active: 'hotel',
      groups: [
        { key: 'hotel', label: '酒店', icon: 'bed' },
        { key: 'food', label: '餐饮', icon: 'utensils' },
        { key: 'spot', label: '景区', icon: 'mountain' }
      ]
    }
  },
  computed: {
    images() {
      const list = this.gallery[this.active] || []
      return list.length ? list : [null, null, null]
    }
  },
  methods: {
    preview(i) {
      const urls = (this.gallery[this.active] || []).filter(Boolean)
      if (!urls.length) return
      uni.previewImage({ current: i, urls })
    }
  }
}
</script>

<style scoped>
.gal-tabs {
  display: flex;
  gap: 16rpx;
  margin-bottom: 20rpx;
}
.gal-tab {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  border: 1rpx solid var(--c-mist);
  border-radius: 12rpx;
  background: var(--c-paper);
}
.gal-tab.on {
  border-color: var(--c-pine);
  background: rgba(31, 92, 75, 0.06);
}
.gal-tab-t {
  font-size: 26rpx;
  color: var(--c-slate);
}
.gal-tab.on .gal-tab-t {
  color: var(--c-pine);
  font-weight: 600;
}
.gal-scroll {
  width: 100%;
  white-space: nowrap;
}
.gal-row {
  display: inline-flex;
  gap: 16rpx;
}
.gal-item {
  width: 380rpx;
  height: 240rpx;
  border-radius: 16rpx;
  overflow: hidden;
  background: var(--c-mist);
}
.gal-img {
  width: 100%;
  height: 100%;
}
.gal-ph {
  width: 100%;
  height: 100%;
  border-radius: 0;
}
</style>
