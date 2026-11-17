<template>
  <view class="page">
    <hys-navbar title="注册" />

    <view class="wrap">
      <!-- 邀请来源（扫码 / 推荐人码带入，已锁定） -->
      <view class="invite">
        <text class="invite-t">来自 {{ inviteName }} 的邀请</text>
        <view class="invite-lock">
          <hys-icon name="lock" :size="26" tone="bronze" />
          <text class="invite-lock-t">已锁定</text>
        </view>
      </view>

      <hys-field index="1" label="推荐人码" required :error="errors.referrerCode" help="推荐关系为佣金前置，无邀请码无法注册">
        <input
          class="field num"
          v-model="form.referrerCode"
          placeholder="请输入推荐人码"
          placeholder-class="field-ph"
          :class="{ 'field-error': !!errors.referrerCode }"
          maxlength="11"
        />
      </hys-field>

      <hys-field index="2" label="名称" required :error="errors.name">
        <input
          class="field"
          v-model="form.name"
          placeholder="真名或网名均可"
          placeholder-class="field-ph"
          :class="{ 'field-error': !!errors.name }"
        />
      </hys-field>

      <hys-field index="3" label="账号 / 手机号" required :error="errors.phone">
        <input
          class="field num"
          v-model="form.phone"
          type="number"
          placeholder="请输入手机号"
          placeholder-class="field-ph"
          :class="{ 'field-error': !!errors.phone }"
          maxlength="11"
        />
      </hys-field>

      <hys-field index="4" label="区域（省 / 市 / 区）" required :error="errors.region" help="需选到区级，不需填写详细地址">
        <hys-region-picker v-model:value="form.region" :regions="regions" />
      </hys-field>

      <hys-field index="5" label="密码" required :error="errors.password" help="6 位以上，用于登录">
        <view class="pwd">
          <input
            class="field pwd-input"
            :password="!showPwd"
            v-model="form.password"
            placeholder="请设置密码"
            placeholder-class="field-ph"
            :class="{ 'field-error': !!errors.password }"
          />
          <text class="pwd-eye" @tap="showPwd = !showPwd">{{ showPwd ? '隐藏' : '显示' }}</text>
        </view>
      </hys-field>

      <view v-if="formError" class="err-box">
        <text class="err-t">{{ formError }}</text>
      </view>

      <view class="btn btn-primary mt24" @tap="submit">
        <text>注册</text>
      </view>
      <view class="to-login" @tap="goLogin">
        <text class="to-login-t">已有账号，去登录</text>
      </view>

      <hys-notice class="mt20" type="info" text="推荐关系为必填项" />
      <text class="foot">推荐关系为佣金前置；扫码注册成功直达首页</text>
      <view class="bottom-pad" />
    </view>
  </view>
</template>

<script>
import store from '@/store/index.js'

export default {
  data() {
    return {
      showPwd: false,
      loading: false,
      formError: '',
      form: { referrerCode: '', name: '', phone: '', region: [], password: '' },
      errors: {}
    }
  },
  computed: {
    regions() {
      return store.state.regions
    },
    inviteName() {
      const u = store.userByPhone(this.form.referrerCode)
      if (u) return u.name
      return store.state.inviteFrom || '推荐人'
    }
  },
  onLoad(q) {
    if (q.ref) this.form.referrerCode = q.ref
    if (q.name) store.state.inviteFrom = q.name
  },
  methods: {
    async submit() {
      if (this.loading) return
      this.loading = true
      const r = await store.register(this.form)
      this.loading = false
      if (!r.ok) {
        this.errors = r.errors || {}
        this.formError = this.errors.referrerCode || Object.values(this.errors)[0] || r.message || '请完善信息'
        store.toast(this.formError)
        return
      }
      this.errors = {}
      this.formError = ''
      uni.showToast({ title: '注册成功，邀请关系已建立', icon: 'none' })
      setTimeout(() => {
        uni.switchTab({ url: '/pages/index/index' })
      }, 700)
    },
    goLogin() {
      uni.navigateTo({ url: '/pages/auth/login' })
    }
  }
}
</script>

<style scoped>
.invite {
  margin: 12rpx 0 28rpx;
  padding: 20rpx 24rpx;
  border-radius: 16rpx;
  background: var(--c-vip-surface);
  border: 1rpx solid rgba(129, 99, 56, 0.22);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.invite-t {
  font-size: 26rpx;
  color: var(--c-vip-bronze);
  font-weight: 500;
}
.invite-lock {
  display: flex;
  align-items: center;
  gap: 6rpx;
}
.invite-lock-t {
  font-size: 22rpx;
  color: var(--c-vip-bronze);
}
.pwd {
  position: relative;
}
.pwd-input { padding-right: 120rpx; }
.pwd-eye {
  position: absolute;
  right: 24rpx;
  top: 0;
  height: 92rpx;
  line-height: 92rpx;
  font-size: 22rpx;
  color: var(--c-slate);
}
.err-box {
  padding: 16rpx 20rpx;
  border-radius: 12rpx;
  background: rgba(178, 59, 46, 0.08);
  border: 1rpx solid rgba(178, 59, 46, 0.35);
}
.err-t {
  font-size: 22rpx;
  color: var(--c-danger);
  line-height: 1.5;
}
.mt24 { margin-top: 24rpx; }
.mt20 { margin-top: 20rpx; }
.to-login {
  padding: 28rpx 0;
  display: flex;
  justify-content: center;
}
.to-login-t {
  font-size: 24rpx;
  color: var(--c-slate);
}
.foot {
  display: block;
  margin-top: 14rpx;
  font-size: 22rpx;
  color: var(--c-slate);
}
.bottom-pad { height: 60rpx; }
</style>
