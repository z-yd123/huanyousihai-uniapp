/* 主题取值：小程序跟随系统 / H5 支持 ?theme= 覆盖（仅预览用） */

const PALETTE = {
  light: {
    ink: '#1C2B26',
    pine: '#1F5C4B',
    'pine-deep': '#144234',
    terracotta: '#BE522E',
    sand: '#F6F2E9',
    paper: '#FFFDF8',
    mist: '#E4E0D5',
    slate: '#55615A',
    bronze: '#816338',
    'vip-surface': '#F3E9D2',
    'on-pine': '#FFFFFF',
    'on-terracotta': '#FFFFFF',
    success: '#2E6B4F',
    warning: '#956312',
    danger: '#B23B2E',
    info: '#2C5A7A',
    disabled: '#A9B0AA',
    'disabled-surface': '#ECEAE2'
  },
  dark: {
    ink: '#E8EDE7',
    pine: '#7FCBA8',
    'pine-deep': '#5FAE8C',
    terracotta: '#E88A62',
    sand: '#121A17',
    paper: '#1B2621',
    mist: '#33423A',
    slate: '#A3B0A6',
    bronze: '#D9B578',
    'vip-surface': '#3A3020',
    'on-pine': '#10261E',
    'on-terracotta': '#2A1409',
    success: '#75C79B',
    warning: '#E3B44A',
    danger: '#E88376',
    info: '#7FB2D6',
    disabled: '#66736B',
    'disabled-surface': '#232F29'
  }
}

export function isDark() {
  try {
    // #ifdef H5
    const attr = document.documentElement.getAttribute('data-theme')
    if (attr === 'dark') return true
    if (attr === 'light') return false
    return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
    // #endif
    // #ifndef H5
    const info = uni.getSystemInfoSync()
    return info.theme === 'dark'
    // #endif
  } catch (e) {
    return false
  }
}

export function color(name = 'ink') {
  const p = isDark() ? PALETTE.dark : PALETTE.light
  if (name && name.charAt(0) === '#') return name
  return p[name] || p.ink
}

/* ---------------------------------------------------------------------------
   tabBar 是原生组件，不吃 CSS 变量，必须用运行时 API 同步主题。
   图标为单色 PNG，夜间稿另备一套浅色图（*-d.png / *-don.png）。
   ------------------------------------------------------------------------- */
const TAB_NAMES = ['home', 'store', 'receipt', 'user']

export function applyTabBarTheme() {
  let base = '/static/tabbar/'
  // #ifndef H5
  base = 'static/tabbar/'
  // #endif
  const dark = isDark()
  /* 非 tabBar 页面调用会失败（原生组件不存在），这里静默兜底，不能让它变成未捕获异常 */
  const safe = (fn, opts) => {
    try {
      const p = fn({ ...opts, fail: () => {} })
      if (p && typeof p.catch === 'function') p.catch(() => {})
    } catch (e) {}
  }
  safe(uni.setTabBarStyle, {
    color: dark ? PALETTE.dark.slate : PALETTE.light.slate,
    selectedColor: dark ? PALETTE.dark.pine : PALETTE.light.pine,
    backgroundColor: dark ? PALETTE.dark.paper : PALETTE.light.paper,
    borderStyle: dark ? 'black' : 'white'
  })
  TAB_NAMES.forEach((n, i) => {
    safe(uni.setTabBarItem, {
      index: i,
      iconPath: base + n + (dark ? '-d' : '') + '.png',
      selectedIconPath: base + n + (dark ? '-don' : '-on') + '.png'
    })
  })
}

export default { PALETTE, isDark, color, applyTabBarTheme }
