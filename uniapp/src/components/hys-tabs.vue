<template>
  <view class="tabs" :class="'tabs-' + size">
    <view
      v-for="(it, i) in norm"
      :key="it.value"
      class="tab"
      :class="{ on: it.value === modelValue }"
      @tap="pick(it.value, i)"
    >
      <text class="tab-t">{{ it.label }}</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'hys-tabs',
  props: {
    modelValue: { type: [String, Number], default: '' },
    items: { type: Array, default: () => [] },
    size: { type: String, default: 'md' }
  },
  computed: {
    norm() {
      return this.items.map((it) =>
        typeof it === 'object' ? { label: it.label, value: it.value != null ? it.value : it.label } : { label: it, value: it }
      )
    }
  },
  methods: {
    pick(v, i) {
      if (v === this.modelValue) return
      this.$emit('update:modelValue', v)
      this.$emit('change', v, i)
    }
  }
}
</script>

<style scoped>
.tabs {
  display: flex;
  gap: 12rpx;
  width: 100%;
}
.tab {
  flex: 1;
  height: 76rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1rpx solid var(--c-mist);
  border-radius: 999rpx;
  background: var(--c-paper);
  transition: all 0.15s ease;
}
.tab-t {
  font-size: 26rpx;
  font-weight: 500;
  color: var(--c-slate);
  white-space: nowrap;
}
.tab.on {
  background: var(--c-pine);
  border-color: var(--c-pine);
}
.tab.on .tab-t {
  color: var(--c-on-pine);
  font-weight: 600;
}
.tabs-sm .tab { height: 64rpx; }
.tabs-sm .tab-t { font-size: 24rpx; }
</style>
