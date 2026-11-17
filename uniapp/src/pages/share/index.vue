<template>
  <view class="page">
    <hys-navbar title="我的分享" back />

    <view class="wrap">
      <hys-share-stat :referrer="referrerName" :team-count="stats.team" :direct-count="stats.direct" />

      <text class="def">团队人数＝全部后代；个人人数＝仅直推（口径固定）</text>

      <view class="btn btn-primary poster-btn" @tap="go('/pages/poster/index')">
        <text>生成二维码海报</text>
      </view>

      <view class="sec">
        <view class="sec-l">
          <text class="sec-no">01</text>
          <text class="sec-title">直推名单</text>
        </view>
        <text class="sec-more">{{ list.length }} 人</text>
      </view>

      <hys-state
        v-if="!list.length"
        type="empty"
        title="还没有直推会员，分享海报邀请第一位吧"
        desc="生成个人二维码海报，对方扫码即带推荐关系注册"
        action-text="去生成海报"
        @action="go('/pages/poster/index')"
      />
      <hys-direct-row v-for="m in list" :key="m.id" :member="m" />

      <hys-notice class="mt16" type="info" text="两级封顶、合法合规" />
      <view class="bottom-pad" />
    </view>
  </view>
</template>

<script>
import store from '@/store/index.js'

export default {
  computed: {
    stats() {
      return store.teamStats()
    },
    list() {
      return store.directList()
    },
    referrerName() {
      return store.state.share.referrerName || store.currentUser.value.referrerName || '—'
    },
    inviteCode() {
      return store.state.share.inviteCode || store.currentUser.value.inviteCode || ''
    },
  },
  methods: {
    go(url) {
      uni.navigateTo({ url })
    }
  }
}
</script>

<style scoped>
.def {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: var(--c-slate);
}
.poster-btn {
  margin-top: 24rpx;
}
.sec-l { display: flex; align-items: baseline; }
.mt16 { margin-top: 16rpx; }
.bottom-pad { height: 60rpx; }
</style>
