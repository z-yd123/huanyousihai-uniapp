<template>
  <image class="hys-icon" :src="uri" :style="{ width: sizeRpx, height: sizeRpx }" mode="aspectFit" />
</template>

<script>
import { iconDataUri } from '@/utils/icons.js'
import { color as themeColor } from '@/utils/theme.js'

export default {
  name: 'hys-icon',
  props: {
    name: { type: String, default: 'dot' },
    size: { type: [Number, String], default: 40 },
    tone: { type: String, default: 'ink' },
    hex: { type: String, default: '' }
  },
  computed: {
    sizeRpx() {
      return typeof this.size === 'number' ? this.size + 'rpx' : this.size
    },
    uri() {
      const c = this.hex || themeColor(this.tone)
      return iconDataUri(this.name, c)
    }
  }
}
</script>

<style scoped>
.hys-icon {
  display: block;
  flex-shrink: 0;
}
</style>
