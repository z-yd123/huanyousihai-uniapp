<template>
  <view class="page">
    <hys-navbar title="充值 / 互转" back />

    <view class="wrap">
      <hys-tabs v-model="tab" :items="['充值', '互转']" @change="onTab" />

      <view class="form mt24">
        <hys-field label="金额（元）" :error="amountError">
          <input
            class="field num"
            type="digit"
            v-model="amount"
            placeholder="请输入金额"
            placeholder-class="field-ph"
            :class="{ 'field-error': !!amountError }"
          />
        </hys-field>

        <hys-notice class="mb24" :type="amountError ? 'danger' : 'info'" :text="limitText" />

        <hys-field v-if="tab === '互转'" label="对手方（互转时显示）" :error="targetError" help="选择会员 / 输入账号（手机号）">
          <input
            class="field"
            v-model="target"
            placeholder="选择会员 / 输入账号"
            placeholder-class="field-ph"
            :class="{ 'field-error': !!targetError }"
          />
          <view class="quick">
            <text class="quick-k">常用</text>
            <view v-for="m in candidates" :key="m.id" class="quick-c" @tap="target = m.phone">
              <text class="quick-t">{{ m.name }}</text>
            </view>
          </view>
        </hys-field>

        <view class="confirm card">
          <text class="cf-t">确认转账信息</text>
          <view class="kv">
            <text class="kv-k">金额</text>
            <hys-amount :value="Number(amount || 0)" size="s" class="kv-v" />
          </view>
          <view class="kv">
            <text class="kv-k">对手方</text>
            <text class="kv-v">{{ tab === '互转' ? targetName : '平台 / 分公司代充' }}</text>
          </view>
          <view class="kv">
            <text class="kv-k">手续费</text>
            <text class="kv-v">{{ tab === '互转' ? '—' : '—' }}</text>
          </view>
        </view>

        <view class="btn btn-primary mt24" :class="{ 'btn-disabled': !canSubmit }" @tap="submit">
          <text>{{ submitting ? '处理中…' : '确认' }}</text>
        </view>

        <hys-notice class="mt20" type="info" text="建议用互转或升级 VIP 消费，避免手续费与个人所得税" />
        <view class="bottom-pad" />
      </view>
    </view>
  </view>
</template>

<script>
import store from '@/store/index.js'
import { money } from '@/utils/commission.js'

export default {
  data() {
    return {
      tab: '充值',
      amount: '',
      target: '',
      submitting: false,
      amountError: '',
      targetError: '',
      touched: false
    }
  },
  computed: {
    user() {
      return store.currentUser.value
    },
    candidates() {
      return store.state.users.filter((u) => u.id !== this.user.id && !u.hidden).slice(0, 3)
    },
    targetName() {
      if (!this.target) return '—'
      const u = store.userByPhone(this.target)
      return u ? `${u.name} ${store.maskPhone(u.phone)}` : '—'
    },
    limitText() {
      return this.tab === '充值'
        ? '单笔不超过 5000 元，超额需拆分'
        : `可转余额 ¥${(this.user.balance / 100).toFixed(2)}，转出即时到账`
    },
    amountHelp() {
      return this.tab === '充值' ? '单笔不超过 5000 元，超额需拆分' : ''
    },
    canSubmit() {
      const n = Number(this.amount || 0)
      if (!(n > 0)) return false
      if (this.amountError || this.targetError) return false
      if (this.tab === '互转' && !this.target) return false
      return true
    }
  },
  onLoad(q) {
    if (q.tab === 'transfer') this.tab = '互转'
    if (q.tab === 'recharge') this.tab = '充值'
  },
  watch: {
    amount(v) {
      this.validateAmount(v)
    }
  },
  methods: {
    onTab(v) {
      this.amountError = ''
      this.targetError = ''
    },
    validateAmount(v) {
      const n = Number(v || 0)
      this.amountError = ''
      if (!v) return
      if (!(n > 0)) {
        this.amountError = '请输入正确的金额'
        return
      }
      if (this.tab === '充值' && n > 5000) {
        this.amountError = '单笔充值不超过 5000 元，请拆分为多次充值'
        return
      }
      if (this.tab === '互转' && money.toCents(n) > this.user.balance) {
        this.amountError = '余额不足，可通过充值后完成互转'
      }
    },
    async submit() {
      if (this.submitting) return
      this.submitting = true
      const r =
        this.tab === '充值' ? await store.recharge(this.amount) : await store.transfer(this.target, this.amount)
      this.submitting = false
      if (!r.ok) {
        this.amountError = r.message || '操作失败'
        store.toast(r.message || '操作失败')
        return
      }
      const amt = money.fmt(money.toCents(this.amount))
      const isRecharge = this.tab === '充值'
      uni.showModal({
        title: isRecharge ? '充值已登记' : '互转成功',
        content: isRecharge
          ? `${amt} 充值登记已提交，运营核对到账后自动入余额`
          : `${amt}　对手方 ${this.targetName}\n已实时到账，双方余额同步更新`,
        confirmText: '看流水',
        cancelText: '完成',
        success: (res) => {
          if (res.confirm) uni.navigateTo({ url: '/pages/wallet/income' })
          else uni.navigateBack()
        }
      })
    }
  }
}
</script>

<style scoped>
.mt24 { margin-top: 24rpx; }
.mb24 { margin-bottom: 24rpx; }
.form { padding-bottom: 60rpx; }
.quick {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 14rpx;
  flex-wrap: wrap;
}
.quick-k {
  font-size: 22rpx;
  color: var(--c-slate);
}
.quick-c {
  padding: 6rpx 16rpx;
  border: 1rpx solid var(--c-mist);
  border-radius: 999rpx;
}
.quick-t {
  font-size: 22rpx;
  color: var(--c-ink);
}
.confirm {
  padding: 24rpx;
}
.cf-t {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--c-ink);
  padding-bottom: 8rpx;
  border-bottom: 1rpx solid var(--c-mist);
  display: block;
  margin-bottom: 8rpx;
}
.mt20 { margin-top: 20rpx; }
.bottom-pad { height: 40rpx; }
</style>
