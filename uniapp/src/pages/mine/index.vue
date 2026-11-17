<template>
  <view class="page">
    <hys-navbar title="个人中心" />

    <view class="wrap">
      <!-- 会员卡 -->
      <view class="member card">
        <view class="avatar">
          <text class="avatar-t">{{ initial }}</text>
        </view>
        <view class="m-info">
          <text class="m-name">{{ user.name }}</text>
          <view class="m-tier">
            <hys-vip-badge :state="badgeState" />
          </view>
        </view>
        <text class="m-region">{{ user.region ? user.region.join(' ') : '' }}</text>
      </view>

      <!-- 余额 -->
      <hys-balance-card
        class="mt16"
        :balance="user.balance"
        @recharge="go('/pages/wallet/recharge?tab=recharge')"
        @transfer="go('/pages/wallet/recharge?tab=transfer')"
        @withdraw="go('/pages/wallet/withdraw')"
      />

      <!-- 01 资金与收益 -->
      <view class="sec">
        <view class="sec-l">
          <text class="sec-no">01</text>
          <text class="sec-title">资金与收益</text>
        </view>
      </view>
      <view class="group card">
        <view class="row" @tap="go('/pages/commission/index')">
          <view class="row-main">
            <text class="row-t">佣金明细</text>
            <text class="row-d">直推 / 间推两级 → 本期合计 {{ money(commissionTotal) }}</text>
          </view>
          <hys-icon name="chevron-right" :size="30" tone="slate" />
        </view>
        <view class="row" @tap="go('/pages/wallet/income')">
          <view class="row-main">
            <text class="row-t">收入明细</text>
            <text class="row-d">余额流水 → 充值 / 互转 / 提现 / 佣金</text>
          </view>
          <hys-icon name="chevron-right" :size="30" tone="slate" />
        </view>
      </view>

      <!-- 02 分享 -->
      <view class="sec">
        <view class="sec-l">
          <text class="sec-no">02</text>
          <text class="sec-title">分享</text>
        </view>
      </view>
      <view class="group card">
        <view class="row" @tap="go('/pages/share/index')">
          <view class="row-main">
            <text class="row-t">我的分享</text>
            <text class="row-d">直推名单与统计 → 团队 {{ stats.team }} / 个人 {{ stats.direct }}</text>
          </view>
          <hys-icon name="chevron-right" :size="30" tone="slate" />
        </view>
        <view class="row" @tap="go('/pages/poster/index')">
          <view class="row-main">
            <text class="row-t">生成二维码海报</text>
            <text class="row-d">多模板保存 → 扫码即带推荐关系</text>
          </view>
          <hys-icon name="chevron-right" :size="30" tone="slate" />
        </view>
      </view>

      <!-- 03 账号 -->
      <view class="sec">
        <view class="sec-l">
          <text class="sec-no">03</text>
          <text class="sec-title">账号</text>
        </view>
      </view>
      <view class="group card">
        <view class="row" @tap="go('/pages/auth/password')">
          <view class="row-main"><text class="row-t">修改密码</text></view>
          <hys-icon name="chevron-right" :size="30" tone="slate" />
        </view>
        <view class="row" @tap="go('/pages/auth/forgot')">
          <view class="row-main"><text class="row-t">找回密码</text></view>
          <hys-icon name="chevron-right" :size="30" tone="slate" />
        </view>
        <view class="row" @tap="logout">
          <view class="row-main"><text class="row-t">退出登录</text></view>
          <hys-icon name="chevron-right" :size="30" tone="slate" />
        </view>
      </view>

      <!-- 04 更多服务 · 即将上线 -->
      <view class="sec">
        <view class="sec-l">
          <text class="sec-no">04</text>
          <text class="sec-title">更多服务 · 即将上线</text>
        </view>
      </view>
      <view class="grid">
        <view v-for="f in soon" :key="f.name" class="soon card" @tap="goPlaceholder(f.name)">
          <text class="soon-t">{{ f.name }}</text>
          <view class="soon-badge"><text class="soon-badge-t">即将上线</text></view>
        </view>
      </view>

      <hys-notice class="mt24" type="plain" text="分销层级只有两级：直推 + 间推，两级封顶、合法合规" />
      <view class="bottom-pad" />
    </view>
  </view>
</template>

<script>
import store from '@/store/index.js'
import { money } from '@/utils/commission.js'

export default {
  data() {
    return {
      soon: [{ name: '绿色积分' }, { name: '优惠券' }, { name: '积分兑换' }, { name: 'APP' }]
    }
  },
  async onShow() {
    if (store.state.loggedIn && !store.state.member) {
      try {
        await store.refreshProfile()
      } catch (e) {
        store.toast(e.message)
      }
    }
  },
  computed: {
    user() {
      return store.currentUser.value
    },
    initial() {
      return (this.user.name || '会').slice(0, 1)
    },
    badgeState() {
      return store.vipState.value === 'active' ? 'active' : store.vipState.value === 'soon' ? 'soon' : store.vipState.value === 'expired' ? 'expired' : 'normal'
    },
    stats() {
      return store.teamStats()
    },
    commissionTotal() {
      return store
        .myCommissions()
        .filter((c) => c.status === '已入账')
        .reduce((s, c) => s + c.amount, 0)
    }
  },
  methods: {
    money(cents) {
      return money.fmt(cents)
    },
    go(url) {
      if (url === '/pages/wallet/recharge') return
      uni.navigateTo({ url })
    },
    goPlaceholder(name) {
      uni.navigateTo({ url: '/pages/placeholder/index?name=' + encodeURIComponent(name) })
    },
    logout() {
      uni.showModal({
        title: '退出登录',
        content: '退出后需要重新登录才能继续下单与查看佣金',
        success: async (res) => {
          if (!res.confirm) return
          // 登出会作废服务端令牌（token_version +1），需等待完成再跳转
          await store.logout()
          uni.reLaunch({ url: '/pages/auth/login' })
        }
      })
    }
  }
}
</script>

<style scoped>
.member {
  margin-top: 12rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
}
.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: var(--c-mist);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.avatar-t {
  font-size: 36rpx;
  color: var(--c-slate);
  font-weight: 600;
}
.m-info {
  flex: 1;
  min-width: 0;
}
.m-name {
  font-size: 34rpx;
  font-weight: 600;
  color: var(--c-ink);
}
.m-tier { margin-top: 10rpx; }
.m-region {
  font-size: 20rpx;
  color: var(--c-slate);
  max-width: 200rpx;
  text-align: right;
  line-height: 1.4;
}
.mt16 { margin-top: 16rpx; }
.sec-l { display: flex; align-items: baseline; }
.group { padding: 0 24rpx; }
.mt24 { margin-top: 24rpx; }
.grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}
.soon {
  width: calc(50% - 8rpx);
  padding: 22rpx 20rpx;
}
.soon-t {
  font-size: 26rpx;
  color: var(--c-ink);
}
.soon-badge {
  display: inline-flex;
  margin-top: 12rpx;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  background: var(--c-disabled-surface);
}
.soon-badge-t {
  font-size: 20rpx;
  color: var(--c-disabled);
}
.bottom-pad { height: 60rpx; }
</style>
