<template>
  <view class="amt num" :class="cls">
    <text v-if="symbol" class="sym">{{ symbol }}</text>
    <text class="int">{{ intPart }}</text>
    <text class="dec">.{{ decPart }}</text>
    <text v-if="unit" class="unit">元</text>
  </view>
</template>

<script>
export default {
  name: 'hys-amount',
  props: {
    value: { type: [Number, String], default: 0 },
    cents: { type: Boolean, default: false }, // true = 入参为「分」
    size: { type: String, default: 'm' },
    symbol: { type: String, default: '¥' },
    unit: { type: Boolean, default: false },
    group: { type: Boolean, default: false }
  },
  computed: {
    yuan() {
      let v = Number(this.value || 0)
      if (!isFinite(v)) v = 0
      return this.cents ? v / 100 : v
    },
    parts() {
      return this.yuan.toFixed(2).split('.')
    },
    intPart() {
      return this.group ? this.parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',') : this.parts[0]
    },
    decPart() {
      return this.parts[1]
    },
    cls() {
      return 'amt-' + this.size
    }
  }
}
</script>

<style scoped>
.amt {
  display: inline-flex;
  align-items: baseline;
  font-weight: 700;
  letter-spacing: 0.5rpx;
  font-variant-numeric: tabular-nums;
}
.amt-l { font-size: 56rpx; }
.amt-m { font-size: 36rpx; }
.amt-s { font-size: 30rpx; font-weight: 600; }
.sym { font-size: 0.62em; margin-right: 4rpx; font-weight: 600; }
.dec { font-size: 0.72em; }
.unit { font-size: 0.5em; margin-left: 6rpx; font-weight: 500; }
</style>
