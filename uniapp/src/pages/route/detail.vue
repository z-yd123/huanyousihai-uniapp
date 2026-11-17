<template>
  <view class="page">
    <hys-navbar title="线路详情" back />

    <scroll-view class="scroll" scroll-y>
      <!-- 头图 16:10 · 低清占位 → 淡入 -->
      <view class="hero">
        <image v-if="route.cover" class="hero-img" :src="route.cover" mode="aspectFill" />
        <view v-else class="hero-ph ph"><text>头图 16:10</text></view>
        <view v-if="route.video" class="video-pill" @tap="playVideo">
          <hys-icon name="play" :size="28" :hex="'#FFFDF8'" />
          <text class="video-t">视频</text>
        </view>
      </view>

      <view class="wrap body">
        <text class="display">{{ route.title }}</text>
        <view class="tags">
          <view class="tag tag-pine">{{ route.type }}</view>
          <text class="days">{{ route.days }} 天 {{ route.nights }} 晚</text>
          <text class="rating">评分 {{ route.rating }}</text>
        </view>

        <!-- 双价 / 当前适用价 -->
        <view class="price-card card">
          <view class="p-left">
            <text class="p-k">当前适用价</text>
            <hys-amount :value="price" cents size="l" class="price-vip" />
          </view>
          <view class="p-right">
            <view class="p-line">
              <text class="p-k2">普通价</text>
              <hys-amount :value="route.normalPrice" cents size="s" class="price-normal" />
            </view>
            <view v-if="vipOk" class="vip-ok"><text class="vip-ok-t">VIP 价已生效</text></view>
            <view v-else-if="vipExpired" class="vip-ok expired"><text class="vip-ok-t">VIP 已过期，按普通价结算</text></view>
          </view>
        </view>

        <!-- 集合信息 -->
        <view class="gather card">
          <view class="g-row">
            <view class="g-l">
              <hys-icon name="clock" :size="30" tone="ink" />
              <text class="g-k">集合时间</text>
            </view>
            <text class="g-v">{{ route.gather.time }}</text>
          </view>
          <view class="g-row">
            <view class="g-l">
              <hys-icon name="map-pin" :size="30" tone="slate" />
              <text class="g-k">集合地点</text>
            </view>
            <text class="g-v">{{ route.gather.place }}</text>
          </view>
        </view>

        <!-- 02 行程票根 -->
        <view class="sec">
          <view class="sec-l">
            <text class="sec-no">02</text>
            <text class="sec-title">行程票根</text>
          </view>
          <text class="sec-more">{{ route.itinerary.length }} 天</text>
        </view>
        <hys-day v-for="d in route.itinerary" :key="d.day" :item="d" />

        <!-- 03 素材画廊 -->
        <view class="sec">
          <view class="sec-l">
            <text class="sec-no">03</text>
            <text class="sec-title">素材画廊</text>
          </view>
        </view>
        <hys-gallery :gallery="route.gallery" />

        <!-- 自驾游说明 -->
        <view v-if="route.selfDrive" class="drive card">
          <view class="drive-head">
            <hys-icon name="warning" :size="30" tone="warning" />
            <text class="drive-t">不含大巴与导游 · 自驾游专属说明</text>
          </view>
          <view class="way" v-for="(w, i) in route.ways" :key="i">
            <text class="way-no">{{ i + 1 }}</text>
            <text class="way-t">{{ w }}</text>
          </view>
          <text class="drive-s">途经城市 / 餐厅 / 酒店结构化清单（不提供地图）</text>
        </view>

        <view class="date card">
          <text class="date-k">出行日期</text>
          <picker mode="date" :value="travelDate" :start="today" @change="onDate">
            <view class="date-pick">
              <text class="date-v">{{ travelDate || '请选择出行日期' }}</text>
              <hys-icon name="chevron-right" :size="28" tone="slate" />
            </view>
          </picker>
        </view>
        <text class="date-tip">出行日期用于接站与酒店安排，下单后可在订单详情查看；出发前 48 小时可申请售后。</text>

        <hys-notice class="notice" type="info" text="价值 3980 元的 AI 智能相册网站，升级 VIP 会员仅需 398 元" />
        <hys-notice class="notice" type="plain" text="提现与分销口径：两级封顶、合法合规，仅直推 / 间推两级" />

        <view class="dock-pad" />
      </view>
    </scroll-view>

    <!-- 底部报名 -->
    <view class="dock">
      <view class="dock-l">
        <text class="dock-k">当前适用价</text>
        <hys-amount :value="price" cents size="m" class="price-vip" />
      </view>
      <view class="btn btn-primary dock-btn" @tap="signup">
        <text>报名下单</text>
      </view>
    </view>
  </view>
</template>

<script>
import store from '@/store/index.js'
import { guards } from '@/utils/commission.js'

export default {
  data() {
    const now = new Date()
    const p = (n) => String(n).padStart(2, '0')
    return {
      id: '',
      route: null,
      buying: false,
      travelDate: '',
      today: `${now.getFullYear()}-${p(now.getMonth() + 1)}-${p(now.getDate())}`
    }
  },
  computed: {
    vipOk() {
      return guards.isVipActive(store.currentUser.value)
    },
    vipExpired() {
      return store.currentUser.value.level === 'VIP' && !this.vipOk
    },
    price() {
      if (!this.route) return 0
      return guards.priceFor(this.route, store.currentUser.value)
    }
  },
  async onLoad(q) {
    this.id = Number(q.id || 0)
    this.route = store.state.routes.find((r) => String(r.id) === String(this.id)) || store.state.routes[0] || null
    if (!this.route) {
      // 目录尚未加载（冷启动直达）时先拉目录
      await store.refreshCatalog().catch(() => {})
      this.route = store.state.routes.find((r) => String(r.id) === String(this.id)) || store.state.routes[0] || null
    }
    if (this.route) {
      try {
        this.route = (await store.ensureDetail(this.route.id, true)) || this.route
      } catch (e) {
        store.toast(e.message)
      }
    }
  },
  methods: {
    playVideo() {
      uni.showToast({ title: '小视频素材由运营上传', icon: 'none' })
    },
    onDate(e) {
      this.travelDate = e.detail.value
    },
    async signup() {
      if (!store.state.loggedIn) {
        uni.navigateTo({ url: '/pages/auth/login' })
        return
      }
      if (!this.route || this.buying) return
      if (!this.travelDate) {
        store.toast('请先选择出行日期')
        return
      }
      this.buying = true
      const r = await store.createOrder({ itemId: this.route.id, quantity: 1, travelDate: this.travelDate })
      this.buying = false
      if (!r.ok) {
        store.toast(r.message)
        return
      }
      store.toast('下单成功，待付款')
      setTimeout(() => {
        uni.navigateTo({ url: '/pages/order/detail?id=' + r.order.id })
      }, 500)
    }
  }
}
</script>

<style scoped>
.date {
  margin-top: 16rpx;
  padding: 20rpx 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.date-k {
  font-size: 26rpx;
  color: var(--c-slate);
}
.date-pick {
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.date-v {
  font-size: 28rpx;
  color: var(--c-ink);
  font-weight: 600;
}
.date-tip {
  display: block;
  margin: 10rpx 8rpx 0;
  font-size: 22rpx;
  line-height: 1.5;
  color: var(--c-slate);
}
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}
.scroll { flex: 1; height: 0; }
.hero {
  position: relative;
  width: 100%;
  height: 0;
  padding-bottom: 62.5%;
  background: var(--c-mist);
}
.hero-img {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
}
.hero-ph {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  border-radius: 0;
}
.video-pill {
  position: absolute;
  right: 32rpx;
  bottom: 28rpx;
  height: 56rpx;
  padding: 0 20rpx;
  border-radius: 999rpx;
  background: rgba(28, 43, 38, 0.55);
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.video-t {
  font-size: 22rpx;
  color: #fffdf8;
}
.body { padding-top: 28rpx; }
.tags {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-top: 16rpx;
}
.days,
.rating {
  font-size: 24rpx;
  color: var(--c-slate);
}
.price-card {
  margin-top: 28rpx;
  padding: 24rpx;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}
.p-k {
  font-size: 22rpx;
  color: var(--c-slate);
}
.p-left {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}
.p-left .price-vip { display: block; }
.p-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8rpx;
  padding-top: 6rpx;
}
.p-line {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}
.p-k2 {
  font-size: 22rpx;
  color: var(--c-slate);
}
.vip-ok {
  margin-top: 4rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  background: var(--c-vip-surface);
  white-space: nowrap;
}
.vip-ok-t {
  font-size: 20rpx;
  color: var(--c-vip-bronze);
}
.vip-ok.expired {
  background: rgba(178, 59, 46, 0.08);
}
.vip-ok.expired .vip-ok-t { color: var(--c-danger); }

.gather {
  margin-top: 24rpx;
  padding: 8rpx 24rpx;
}
.g-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 0;
}
.g-row + .g-row { border-top: 1rpx solid var(--c-mist); }
.g-l {
  display: flex;
  align-items: center;
  gap: 12rpx;
}
.g-k {
  font-size: 26rpx;
  color: var(--c-slate);
}
.g-v {
  font-size: 26rpx;
  color: var(--c-ink);
  font-weight: 500;
}
.sec-l { display: flex; align-items: baseline; }

.drive {
  margin-top: 24rpx;
  padding: 24rpx;
}
.drive-head {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 16rpx;
}
.drive-t {
  font-size: 24rpx;
  color: var(--c-warning);
  font-weight: 500;
}
.way {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 10rpx 0;
}
.way-no {
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  background: var(--c-disabled-surface);
  color: var(--c-slate);
  font-size: 20rpx;
  text-align: center;
  line-height: 32rpx;
}
.way-t {
  font-size: 24rpx;
  color: var(--c-ink);
}
.drive-s {
  display: block;
  margin-top: 12rpx;
  font-size: 22rpx;
  color: var(--c-slate);
}
.notice { margin-top: 20rpx; }

.dock-l {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}
.dock-k {
  font-size: 22rpx;
  color: var(--c-slate);
}
.dock-btn {
  width: 320rpx;
  flex-shrink: 0;
}
</style>
