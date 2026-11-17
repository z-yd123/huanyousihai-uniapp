<template>
  <view class="bal card">
    <text class="bal-k">账户余额</text>
    <hys-amount :value="balance" cents size="l" class="bal-amt" />
    <view class="bal-acts">
      <view class="bal-act" @tap="$emit('recharge')">
        <hys-icon name="recharge" :size="34" tone="pine" />
        <text class="bal-act-t">充值</text>
      </view>
      <view class="bal-act" @tap="$emit('transfer')">
        <hys-icon name="transfer" :size="34" tone="pine" />
        <text class="bal-act-t">互转</text>
      </view>
      <view class="bal-act" @tap="$emit('withdraw')">
        <hys-icon name="withdraw" :size="34" tone="ink" />
        <text class="bal-act-t">提现</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'hys-balance-card',
  props: {
    balance: { type: Number, default: 0 }
  },
  emits: ['recharge', 'transfer', 'withdraw']
}
</script>

<style scoped>
.bal {
  padding: 28rpx 24rpx 8rpx;
}
.bal-k {
  font-size: 24rpx;
  color: var(--c-slate);
}
.bal-amt {
  display: block;
  margin-top: 8rpx;
  color: var(--c-ink);
}
.bal-acts {
  display: flex;
  margin-top: 20rpx;
  border-top: 1rpx solid var(--c-mist);
}
.bal-act {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 24rpx 0;
}
.bal-act-t {
  font-size: 26rpx;
  color: var(--c-ink);
  font-weight: 500;
}
</style>
