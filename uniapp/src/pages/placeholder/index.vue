<template>
  <view class="page">
    <hys-navbar title="功能名称" back />

    <view class="center">
      <hys-placeholder-card :feature="feature" @back="back" />
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return { feature: '绿色积分' }
  },
  onLoad(q) {
    if (q.name) this.feature = decodeURIComponent(q.name)
    if (q.title) uni.setNavigationBarTitle({ title: q.title })
  },
  methods: {
    back() {
      uni.switchTab({ url: '/pages/mine/index' })
    }
  }
}
</script>

<style scoped>
.center {
  padding-top: 20vh;
}
</style>
