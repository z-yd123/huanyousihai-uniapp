<template>
  <view class="rp">
    <picker class="rp-cell" mode="selector" :range="provinceList" :value="pIndex" @change="onProvince">
      <view class="rp-box" :class="{ filled: !!value[0] }">
        <text class="rp-t">{{ value[0] || '选择省' }}</text>
        <hys-icon name="chevron-down" :size="26" tone="slate" />
      </view>
    </picker>
    <picker
      class="rp-cell"
      mode="selector"
      :range="cityList"
      :value="cIndex"
      :disabled="!value[0]"
      @change="onCity"
    >
      <view class="rp-box" :class="{ filled: !!value[1] }">
        <text class="rp-t">{{ value[1] || '选择市' }}</text>
        <hys-icon name="chevron-down" :size="26" tone="slate" />
      </view>
    </picker>
    <picker
      class="rp-cell"
      mode="selector"
      :range="districtList"
      :value="dIndex"
      :disabled="!value[1]"
      @change="onDistrict"
    >
      <view class="rp-box" :class="{ filled: !!value[2] }">
        <text class="rp-t">{{ value[2] || '选择区' }}</text>
        <hys-icon name="chevron-down" :size="26" tone="slate" />
      </view>
    </picker>
  </view>
</template>

<script>
export default {
  name: 'hys-region-picker',
  props: {
    value: { type: Array, default: () => [] },
    regions: { type: Object, default: () => ({}) }
  },
  emits: ['update:value', 'change'],
  computed: {
    provinceList() {
      return Object.keys(this.regions)
    },
    cityList() {
      const p = this.regions[this.value[0]]
      return p ? Object.keys(p) : []
    },
    districtList() {
      const p = this.regions[this.value[0]]
      const c = p ? p[this.value[1]] : null
      return c || []
    },
    pIndex() {
      return Math.max(0, this.provinceList.indexOf(this.value[0]))
    },
    cIndex() {
      return Math.max(0, this.cityList.indexOf(this.value[1]))
    },
    dIndex() {
      return Math.max(0, this.districtList.indexOf(this.value[2]))
    }
  },
  methods: {
    emit(v) {
      this.$emit('update:value', v)
      this.$emit('change', v)
    },
    onProvince(e) {
      const p = this.provinceList[e.detail.value]
      const cities = Object.keys(this.regions[p] || {})
      const c = cities[0] || ''
      const d = (this.regions[p] && this.regions[p][c] && this.regions[p][c][0]) || ''
      this.emit([p, c, d])
    },
    onCity(e) {
      const c = this.cityList[e.detail.value]
      const d = (this.regions[this.value[0]][c] || [])[0] || ''
      this.emit([this.value[0], c, d])
    },
    onDistrict(e) {
      const d = this.districtList[e.detail.value]
      this.emit([this.value[0], this.value[1], d])
    }
  }
}
</script>

<style scoped>
.rp {
  display: flex;
  gap: 12rpx;
}
.rp-cell {
  flex: 1;
  min-width: 0;
}
.rp-box {
  height: 84rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16rpx;
  border: 1rpx solid var(--c-mist);
  border-radius: 12rpx;
  background: var(--c-paper);
}
.rp-t {
  font-size: 26rpx;
  color: var(--c-disabled);
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.rp-box.filled .rp-t {
  color: var(--c-ink);
}
</style>
