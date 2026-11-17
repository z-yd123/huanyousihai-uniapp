<template>
  <view class="page">
    <hys-navbar title="订单中心" />

    <view class="wrap">
      <hys-tabs v-model="tab" :items="tabs" size="sm" />

      <view class="list">
        <hys-state v-if="loading" type="loading" :rows="2" />
        <hys-state
          v-else-if="!list.length"
          type="empty"
          :title="emptyTitle"
          :desc="emptyDesc"
          action-text="去看看线路"
          @action="goRoutes"
        />
        <hys-order-card
          v-for="o in list"
          :key="o.id"
          :order="o"
          @tap="open(o)"
        />
      </view>

      <hys-notice class="mt24" type="plain" text="商品订单与旅游线路订单统一纳入：待付款 / 待收货 / 售后单 / 全部订单" />
      <view class="bottom-pad" />
    </view>
  </view>
</template>

<script>
import store from '@/store/index.js'

export default {
  data() {
    return {
      tab: '待付款',
      tabs: ['待付款', '待收货', '售后单', '全部订单'],
      loading: true
    }
  },
  computed: {
    list() {
      return store.myOrders(this.tab)
    },
    emptyTitle() {
      if (this.tab === '售后单') return '没有售后单'
      if (this.tab === '待收货') return '暂无待收货订单'
      if (this.tab === '全部订单') return '还没有订单'
      return '暂无待付款订单'
    },
    emptyDesc() {
      return this.tab === '全部订单' ? '去线路或商城挑一个，订单会自动进到这里' : '这个分类下暂时没有订单'
    }
  },
  onShow() {
    setTimeout(() => {
      this.loading = false
    }, 260)
  },
  methods: {
    open(o) {
      uni.navigateTo({ url: '/pages/order/detail?id=' + o.id })
    },
    goRoutes() {
      uni.switchTab({ url: '/pages/index/index' })
    }
  }
}
</script>

<style scoped>
.list { margin-top: 28rpx; }
.mt24 { margin-top: 24rpx; }
.bottom-pad { height: 60rpx; }
</style>
