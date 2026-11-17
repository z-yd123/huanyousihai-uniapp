import { createSSRApp } from 'vue'
import App from './App.vue'
import store from './store/index.js'

export function createApp() {
  const app = createSSRApp(App)
  app.config.globalProperties.$store = store
  app.provide('store', store)

  /* 页面显示时按当前路由定向刷新数据。
     放在全局 mixin 里而不是逐页 onShow，是为了让 21 个页面共享同一套刷新策略：
     谁的数据变了只有 store 知道，页面无需重复声明。 */
  app.mixin({
    onShow() {
      const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : []
      const current = pages.length ? pages[pages.length - 1] : null
      if (current && current.route) store.onPageShow(current.route)
    }
  })

  return { app }
}
