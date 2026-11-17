<template>
  <view class="page">
    <hys-navbar title="收入明细" back />

    <view class="wrap">
      <view class="bal card">
        <text class="bal-k">账户余额</text>
        <hys-amount :value="user.balance" cents size="l" class="bal-amt" />
        <text class="bal-s">余额数字静态更新，无滚动动画</text>
      </view>

      <view class="sec">
        <view class="sec-l">
          <text class="sec-no">01</text>
          <text class="sec-title">流水明细</text>
        </view>
        <text class="sec-more">{{ rows.length }} 笔</text>
      </view>

      <hys-state
        v-if="!rows.length"
        type="empty"
        title="暂无收入明细"
        desc="充值、佣金入账、互转与提现都会在这里留痕"
        action-text="返回余额"
        @action="back"
      />
      <view v-for="w in rows" :key="w.id" class="txn card-flat">
        <view class="txn-head">
          <text class="txn-t">{{ w.type }}</text>
          <hys-amount :value="w.amount" cents size="s" :class="w.amount < 0 ? 'neg' : 'pos'" />
        </view>
        <view class="txn-body">
          <view class="ln">
            <text class="k">对手方</text>
            <text class="v">{{ w.counterparty || '—' }}</text>
          </view>
          <view class="ln">
            <text class="k">手续费</text>
            <text class="v num">{{ w.fee ? '¥' + (w.fee / 100).toFixed(2) : '—' }}</text>
          </view>
          <view class="ln">
            <text class="k">状态</text>
            <text class="v" :class="statusCls(w.status)">{{ w.status }}</text>
          </view>
          <view class="ln">
            <text class="k">时间</text>
            <text class="v num">{{ w.createdAt }}</text>
          </view>
        </view>
      </view>

      <hys-notice class="mt16" type="info" text="提现手续费 6%，按分计算向下取整到分；提现进入人工审核" />
      <view class="bottom-pad" />
    </view>
  </view>
</template>

<script>
import store from '@/store/index.js'

export default {
  computed: {
    user() {
      return store.currentUser.value
    },
    rows() {
      return store.myWallet()
    }
  },
  methods: {
    back() {
      uni.navigateBack()
    },
    statusCls(s) {
      if (s === '审核中') return 'st-warning'
      if (s === '已入账' || s === '已到账') return 'st-success'
      if (s === '已拒绝') return 'st-danger'
      return ''
    }
  }
}
</script>

<style scoped>
.bal {
  margin-top: 12rpx;
  padding: 28rpx 24rpx;
}
.bal-k {
  font-size: 24rpx;
  color: var(--c-slate);
}
.bal-amt {
  display: block;
  margin-top: 6rpx;
  color: var(--c-ink);
}
.bal-s {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: var(--c-slate);
}
.sec-l { display: flex; align-items: baseline; }
.txn {
  padding: 20rpx 24rpx 10rpx;
  margin-bottom: 20rpx;
  background: var(--c-paper);
  border: 1rpx solid var(--c-mist);
  border-radius: 20rpx;
}
.txn-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 14rpx;
  border-bottom: 1rpx solid var(--c-mist);
}
.txn-t {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--c-ink);
}
.pos { color: var(--c-success); }
.neg { color: var(--c-ink); }
.txn-body { padding-bottom: 10rpx; }
.ln {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10rpx 0;
}
.k {
  font-size: 24rpx;
  color: var(--c-slate);
}
.v {
  font-size: 24rpx;
  color: var(--c-ink);
}
.st-warning { color: var(--c-warning); }
.st-success { color: var(--c-success); }
.st-danger { color: var(--c-danger); }
.mt16 { margin-top: 16rpx; }
.bottom-pad { height: 60rpx; }
</style>
