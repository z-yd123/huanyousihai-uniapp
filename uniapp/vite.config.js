import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

/**
 * H5 调试走 /dev-api 反向代理，避免浏览器跨域；
 * 小程序端没有代理，需把 src/api/request.js 里的 BASE_URL 改成后端真实地址。
 */
export default defineConfig({
  plugins: [uni()],
  server: {
    port: 5173,
    proxy: {
      '/dev-api': {
        target: process.env.HYS_API || 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/dev-api/, '')
      }
    }
  }
})
