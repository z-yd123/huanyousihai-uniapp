<template>
  <view class="page">
    <hys-navbar title="环游四海" />

    <view class="wrap">
      <!-- 页头 -->
      <view class="head">
        <view class="head-l">
          <text class="h1">环游四海</text>
          <text class="head-sub">线上组织线路 · 线下执行游玩</text>
        </view>
        <view class="nav-vip" @tap="go('/pages/vip/index')">
          <text class="nav-vip-t">{{ isVip ? 'VIP 会员' : '升级 VIP' }}</text>
        </view>
      </view>

      <!-- 分类 -->
      <hys-tabs v-model="tab" :items="tabs" />

      <!-- 精选线路 -->
      <view class="sec">
        <view class="sec-l">
          <text class="sec-no">01</text>
          <text class="sec-title">精选线路</text>
        </view>
        <text class="sec-more" @tap="more">更多</text>
      </view>

      <hys-state v-if="loading" type="loading" :rows="2" />
      <hys-state
        v-else-if="!list.length"
        type="empty"
        title="暂无可报名的线路"
        desc="下拉可刷新，或切换其他分类看看"
        action-text="刷新"
        @action="refresh"
      />
      <view v-else>
        <view v-for="(r, i) in list" :key="r.id" class="reveal" :style="{ animationDelay: i * 60 + 'ms' }">
          <hys-route-card :item="r" @tap="openRoute(r)" />
        </view>
      </view>

      <view class="tip">
        <text class="tip-t">两级封顶、合法合规 · 线路与特产均为普通会员价 / VIP 会员价双价展示</text>
      </view>
    </view>
  </view>
</template>

<script>
import store from '@/store/index.js'

const TABS = ['全部', '组团游', '自驾游']

export default {
  data() {
    return {
      tab: '全部',
      tabs: TABS,
      loading: true,
      timer: null
    }
  },
  computed: {
    isVip() {
      return store.isVip.value
    },
    list() {
      const all = store.state.routes
      if (this.tab === '全部') return all
      return all.filter((r) => r.type === this.tab)
    }
  },
  onLoad() {
    this.timer = setTimeout(() => {
      this.loading = false
    }, 620)
  },
  async onPullDownRefresh() {
    this.loading = true
    try {
      await store.refreshCatalog()
    } catch (e) {
      store.toast(e.message)
    }
    this.loading = false
    uni.stopPullDownRefresh()
  },
  onUnload() {
    if (this.timer) clearTimeout(this.timer)
  },

  methods: {
    async refresh() {
      this.loading = true
      try {
        await store.refreshCatalog()
      } catch (e) {
        store.toast(e.message)
      }
      this.loading = false
    },
    more() {
      uni.showToast({ title: '已展示全部线路', icon: 'none' })
    },
    openRoute(r) {
      uni.navigateTo({ url: '/pages/route/detail?id=' + r.id })
    },
    go(url) {
      uni.navigateTo({ url })
    }
  }
}
</script>

<style scoped>
.nav-vip {
  height: 52rpx;
  padding: 0 20rpx;
  border: 1rpx solid var(--c-vip-bronze);
  border-radius: 999rpx;
  display: flex;
  align-items: center;
}
.nav-vip-t {
  font-size: 22rpx;
  color: var(--c-vip-bronze);
  font-weight: 500;
}
.head {
  padding: 8rpx 0 24rpx;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}
.head-l { flex: 1; min-width: 0; }
.head-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: var(--c-slate);
}
.sec-l {
  display: flex;
  align-items: baseline;
}
.tip {
  padding: 40rpx 0 60rpx;
}
.tip-t {
  font-size: 22rpx;
  color: var(--c-slate);
  line-height: 1.6;
}
</style>
