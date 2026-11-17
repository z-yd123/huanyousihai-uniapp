<template>
  <view class="drow card">
    <view class="head">
      <text class="name">{{ member.name }}</text>
      <view class="tier">
        <text class="tier-t">{{ member.level === 'VIP' ? 'VIP 会员' : '普通会员' }}</text>
      </view>
    </view>
    <view class="lines">
      <view class="ln">
        <text class="ln-k">推荐人</text>
        <text class="ln-v">{{ member.referrerName || '—' }}</text>
      </view>
      <view class="ln">
        <text class="ln-k">手机号</text>
        <text class="ln-v num">{{ masked }}</text>
      </view>
      <view class="ln">
        <text class="ln-k">团队人数</text>
        <text class="ln-v num">{{ member.team }}</text>
      </view>
      <view class="ln">
        <text class="ln-k">个人人数</text>
        <text class="ln-v num">{{ member.direct }}</text>
      </view>
      <view class="ln">
        <text class="ln-k">注册时间</text>
        <text class="ln-v num">{{ member.createdAt }}</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'hys-direct-row',
  props: {
    member: { type: Object, default: () => ({}) }
  },
  computed: {
    masked() {
      const p = this.member.phone || ''
      if (p.length < 11) return p || '—'
      return p.slice(0, 3) + '****' + p.slice(-4)
    }
  }
}
</script>

<style scoped>
.drow {
  padding: 22rpx 24rpx 10rpx;
  margin-bottom: 20rpx;
}
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid var(--c-mist);
}
.name {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--c-ink);
}
.tier {
  padding: 4rpx 16rpx;
  border: 1rpx solid var(--c-vip-bronze);
  border-radius: 8rpx;
  background: var(--c-vip-surface);
}
.tier-t {
  font-size: 20rpx;
  color: var(--c-vip-bronze);
}
.lines { padding-bottom: 10rpx; }
.ln {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12rpx 0;
}
.ln-k {
  font-size: 24rpx;
  color: var(--c-slate);
}
.ln-v {
  font-size: 24rpx;
  color: var(--c-ink);
}
</style>
