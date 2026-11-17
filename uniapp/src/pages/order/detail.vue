<template>
  <view class="page">
    <hys-navbar title="订单详情" back />

    <view v-if="order" class="wrap">
      <view class="head card">
        <view class="head-l">
          <hys-badge :label="order.status" :tone="tone" />
          <text class="head-t">{{ tip }}</text>
        </view>
        <text class="order-no num">订单号 {{ order.id }}</text>
      </view>

      <view class="item card">
        <view class="thumb">
          <image v-if="order.thumb" class="thumb-img" :src="order.thumb" mode="aspectFill" />
          <view v-else class="ph thumb-ph" />
        </view>
        <view class="meta">
          <text class="t">{{ order.title }}</text>
          <text class="s">{{ order.spec }}</text>
          <view class="tags">
            <text class="kind">{{ order.kind }}</text>
          </view>
        </view>
        <hys-amount :value="order.amount" cents size="m" class="amt" />
      </view>

      <view class="info card">
        <view class="kv">
          <text class="kv-k">下单时间</text>
          <text class="kv-v num">{{ order.createdAt }}</text>
        </view>
        <view class="kv">
          <text class="kv-k">订单类型</text>
          <text class="kv-v">{{ order.kind }}</text>
        </view>
        <view class="kv">
          <text class="kv-k">实付金额</text>
          <hys-amount :value="order.amount" cents size="s" class="kv-v price-vip" />
        </view>
        <view class="kv">
          <text class="kv-k">计佣口径</text>
          <text class="kv-v">两级封顶</text>
        </view>
      </view>

      <hys-notice class="mt20" type="info" text="售后可退款将同步回冲对应佣金记录，订单状态不可非法跳变" />

      <view v-if="order.status === '待付款'" class="acts">
        <view class="btn btn-primary" @tap="pay">
          <text>{{ paying ? '支付中…' : '余额支付' }}</text>
        </view>
        <view class="btn btn-secondary" @tap="cancel">
          <text>取消订单</text>
        </view>
      </view>
      <view v-else-if="order.status === '待收货'" class="acts">
        <view class="btn btn-primary" @tap="confirm">
          <text>确认收货 / 出行</text>
        </view>
        <view class="btn btn-secondary" @tap="afterSale">
          <text>申请售后</text>
        </view>
      </view>
      <view v-else class="acts">
        <view class="btn btn-secondary" @tap="back">
          <text>返回订单中心</text>
        </view>
      </view>
      <view class="bottom-pad" />
    </view>

    <hys-state v-else type="error" title="订单不存在" action-text="返回订单中心" @action="back" />
  </view>
</template>

<script>
import store from '@/store/index.js'

export default {
  data() {
    return { id: '', order: null, paying: false }
  },
  computed: {
    tone() {
      const s = this.order ? this.order.status : ''
      if (s === '待付款') return 'warning'
      if (s === '待收货') return 'info'
      if (s === '已完成') return 'success'
      return 'slate'
    },
    tip() {
      const s = this.order ? this.order.status : ''
      if (s === '待付款') return '等待付款，付款后进入待收货'
      if (s === '待收货') return '平台已受理，等待出行 / 发货'
      if (s === '已完成') return '订单已完成'
      return ''
    }
  },
  async onLoad(q) {
    this.id = Number(q.id)
    this.order = store.state.orders.find((o) => String(o.id) === String(q.id)) || null
    // 列表里可能没有该订单（例如从分享链接直接进入），统一回源详情接口取权威状态
    try {
      this.order = await store.refreshOrderDetail(this.id)
    } catch (e) {
      if (!this.order) store.toast(e.message)
    }
  },
  methods: {
    async pay() {
      if (this.paying) return
      this.paying = true
      const r = await store.payOrder(this.id)
      this.paying = false
      if (!r.ok) {
        store.toast(r.message)
        return
      }
      store.toast('支付成功')
      this.order = await store.refreshOrderDetail(this.id).catch(() => this.order)
    },
    async cancel() {
      const res = await new Promise((resolve) => {
        uni.showModal({
          title: '取消订单',
          content: '取消后订单不可恢复，需要重新下单',
          success: resolve
        })
      })
      if (!res.confirm) return
      const r = await store.cancelOrder(this.id)
      if (!r.ok) {
        store.toast(r.message)
        return
      }
      store.toast('订单已取消')
      this.order = await store.refreshOrderDetail(this.id).catch(() => this.order)
    },
    async confirm() {
      const r = await store.confirmOrder(this.id)
      if (!r.ok) {
        store.toast(r.message)
        return
      }
      store.toast('已确认完成')
      this.order = await store.refreshOrderDetail(this.id).catch(() => this.order)
    },
    afterSale() {
      uni.showModal({
        title: '申请售后',
        editable: true,
        placeholderText: '请填写售后原因',
        success: async (res) => {
          if (!res.confirm) return
          const reason = (res.content || '').trim()
          if (!reason) {
            store.toast('请填写售后原因')
            return
          }
          const r = await store.applyAfterSale(this.id, reason)
          if (!r.ok) {
            store.toast(r.message)
            return
          }
          store.toast('售后申请已提交')
          this.order = await store.refreshOrderDetail(this.id).catch(() => this.order)
        }
      })
    },
    back() {
      uni.switchTab({ url: '/pages/order/index' })
    }
  }
}
</script>

<style scoped>
.head {
  margin-top: 12rpx;
  padding: 24rpx;
}
.head-l {
  display: flex;
  align-items: center;
  gap: 12rpx;
}
.head-t {
  font-size: 24rpx;
  color: var(--c-slate);
}
.order-no {
  display: block;
  margin-top: 12rpx;
  font-size: 22rpx;
  color: var(--c-slate);
}
.item {
  margin-top: 24rpx;
  padding: 24rpx;
  display: flex;
  gap: 20rpx;
  align-items: flex-start;
}
.thumb {
  width: 140rpx;
  height: 140rpx;
  border-radius: 12rpx;
  overflow: hidden;
  background: var(--c-mist);
  flex-shrink: 0;
}
.thumb-img { width: 100%; height: 100%; }
.thumb-ph { width: 100%; height: 100%; border-radius: 0; }
.meta { flex: 1; min-width: 0; }
.t {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--c-ink);
}
.s {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: var(--c-slate);
}
.tags { margin-top: 10rpx; }
.kind {
  font-size: 22rpx;
  color: var(--c-slate);
}
.amt { flex-shrink: 0; }
.info {
  margin-top: 24rpx;
  padding: 8rpx 24rpx;
}
.mt20 { margin-top: 20rpx; }
.acts { margin-top: 32rpx; }
.bottom-pad { height: 60rpx; }
</style>
