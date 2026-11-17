<template>
  <view class="page">
    <hys-navbar title="生成二维码海报" back />

    <view class="wrap">
      <hys-poster
        :landscape="landscape"
        :name="user.name"
        :invite-code="inviteCode"
        :generating="generating"
      />

      <view class="captions">
        <text class="cap-k">海报上的名称与邀请码</text>
        <text class="cap-v">{{ user.name }} · {{ inviteCode }}</text>
      </view>

      <view v-if="qr" class="qr card">
        <image class="qr-img" :src="qr" mode="aspectFit" />
        <view class="qr-side">
          <text class="qr-t">真实邀请二维码</text>
          <text class="qr-d">由服务端按邀请码实时生成，扫码进入注册页自动带入推荐人</text>
        </view>
      </view>

      <view class="sec">
        <view class="sec-l">
          <text class="sec-no">01</text>
          <text class="sec-title">模板横向选择器</text>
        </view>
      </view>
      <scroll-view scroll-x class="tpl-scroll" :show-scrollbar="false">
        <view class="tpl-row">
          <view
            v-for="(t, i) in templates"
            :key="t.id"
            class="tpl"
            :class="{ on: i === active }"
            @tap="pick(i)"
          >
            <image class="tpl-img" :src="t.img" mode="aspectFill" />
            <text class="tpl-t">{{ t.name }}</text>
          </view>
        </view>
      </scroll-view>

      <view class="info card">
        <view class="kv">
          <text class="kv-k">名称</text>
          <text class="kv-v">{{ user.name }}</text>
        </view>
        <view class="kv">
          <text class="kv-k">邀请码</text>
          <text class="kv-v num">{{ user.phone }}</text>
        </view>
        <view class="kv">
          <text class="kv-k">手机号脱敏</text>
          <text class="kv-v num">{{ masked }}</text>
        </view>
      </view>

      <view class="acts">
        <view class="btn btn-primary act" @tap="save">
          <text>保存到相册</text>
        </view>
        <view class="btn btn-vip act" @tap="share">
          <text>转发</text>
        </view>
      </view>

      <hys-notice class="mt16" type="plain" text="已保存到相册，可继续保存其他模板" />
      <hys-notice class="mt12" type="info" text="扫码注册即带推荐关系；小程序码保持白底深码、清晰区 ≥200rpx" />
      <view class="bottom-pad" />
    </view>
  </view>
</template>

<script>
import store from '@/store/index.js'

export default {
  data() {
    return {
      active: 0,
      generating: false,
      qr: '',
      templates: [
        { id: 't1', name: '模板一', img: '/static/img/poster-landscape.jpg' },
        { id: 't2', name: '模板二', img: '/static/img/route-dali.jpg' },
        { id: 't3', name: '模板三', img: '/static/img/route-erhai.jpg' },
        { id: 't4', name: '模板四', img: '/static/img/route-daocheng.jpg' }
      ]
    }
  },
  computed: {
    user() {
      return store.currentUser.value
    },
    masked() {
      return store.maskPhone(this.user.phone)
    },
    inviteCode() {
      return this.user.inviteCode || this.user.phone || ''
    },
    landscape() {
      return this.templates[this.active].img
    }
  },
  async onShow() {
    try {
      this.qr = await store.refreshInviteQr(240)
    } catch (e) {
      this.qr = ''
    }
  },
  methods: {
    pick(i) {
      this.active = i
      this.generating = true
      setTimeout(() => {
        this.generating = false
      }, 520)
    },
    save() {
      // #ifdef H5
      uni.showToast({ title: '已保存到相册，可继续保存其他模板', icon: 'none' })
      return
      // #endif
      // #ifndef H5
      uni.saveImageToPhotosAlbum({
        filePath: this.landscape,
        success: () => store.toast('已保存到相册，可继续保存其他模板'),
        fail: () => store.toast('请开启相册权限后保存')
      })
      // #endif
    },
    share() {
      uni.showActionSheet({
        itemList: ['转发给微信好友', '生成分享链接'],
        success: () => store.toast('已调起转发')
      })
    }
  }
}
</script>

<style scoped>
.qr {
  margin-top: 16rpx;
  padding: 20rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
}
.qr-img {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  background: #fff;
  flex-shrink: 0;
}
.qr-side {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.qr-t {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--c-ink);
}
.qr-d {
  font-size: 22rpx;
  line-height: 1.5;
  color: var(--c-slate);
}
.captions {
  padding: 16rpx 4rpx 0;
}
.cap-k {
  display: block;
  font-size: 22rpx;
  color: var(--c-slate);
}
.cap-v {
  display: block;
  margin-top: 4rpx;
  font-size: 24rpx;
  color: var(--c-ink);
}
.sec-l { display: flex; align-items: baseline; }
.tpl-scroll { width: 100%; white-space: nowrap; }
.tpl-row { display: inline-flex; gap: 16rpx; }
.tpl {
  width: 200rpx;
  flex-shrink: 0;
}
.tpl-img {
  width: 200rpx;
  height: 280rpx;
  border-radius: 12rpx;
  border: 3rpx solid transparent;
}
.tpl.on .tpl-img {
  border-color: var(--c-pine);
}
.tpl-t {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: var(--c-slate);
  text-align: center;
}
.tpl.on .tpl-t { color: var(--c-pine); font-weight: 600; }
.info {
  margin-top: 24rpx;
  padding: 8rpx 24rpx;
}
.acts {
  display: flex;
  gap: 16rpx;
  margin-top: 24rpx;
}
.act { flex: 1; }
.mt16 { margin-top: 16rpx; }
.mt12 { margin-top: 12rpx; }
.bottom-pad { height: 60rpx; }
</style>
