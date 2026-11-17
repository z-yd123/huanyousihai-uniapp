/* ============================================================================
   环游四海 V1.0 · 小程序端请求层

   与后端 ruoyi-hys 的 /app/** 接口对接：
   - 会员令牌统一放在 Authorization 头（后端 HysAuthInterceptor 校验，无状态，不走 Shiro/Spring Session）
   - 后端用 HttpStatusResponseAdvice 把业务异常翻译成真实 HTTP 状态码，所以这里既要看状态码也要看业务 code
   - 401 视为令牌失效（改密 / 登出 / 被停用都会让 token_version 变化），清空本地令牌并回登录页
   ========================================================================== */

/* H5 走 vite 代理规避跨域；小程序 / App 端只能直连后端地址，按需改 BASE_REMOTE 即可 */
// #ifdef H5
const BASE_URL = '/dev-api'
// #endif
// #ifndef H5
const BASE_URL = 'http://localhost:8080'
// #endif

const TOKEN_KEY = 'hys_token'

export function getToken() {
  try {
    return uni.getStorageSync(TOKEN_KEY) || ''
  } catch (e) {
    return ''
  }
}

export function setToken(token) {
  try {
    if (token) uni.setStorageSync(TOKEN_KEY, token)
    else uni.removeStorageSync(TOKEN_KEY)
  } catch (e) {
    /* 忽略存储异常：极端情况下仅影响免登录体验，不影响当次会话 */
  }
}

let redirecting = false

/** 令牌失效：清令牌 + 回登录页（做去重，避免并发请求连续弹登录页） */
function toLogin() {
  setToken('')
  if (redirecting) return
  redirecting = true
  setTimeout(() => {
    redirecting = false
  }, 1200)
  uni.showToast({ title: '登录状态已失效，请重新登录', icon: 'none' })
  setTimeout(() => {
    uni.reLaunch({ url: '/pages/auth/login' })
  }, 600)
}

function buildQuery(params) {
  if (!params) return ''
  const parts = []
  Object.keys(params).forEach((key) => {
    const value = params[key]
    if (value === null || value === undefined || value === '') return
    parts.push(encodeURIComponent(key) + '=' + encodeURIComponent(value))
  })
  return parts.length ? '?' + parts.join('&') : ''
}

/**
 * 统一请求。
 * @returns {Promise<object>} 后端响应体（AjaxResult / TableDataInfo）
 */
export function request(options) {
  const { url, method = 'GET', data = null, params = null, auth = true, loading = false } = options
  const header = { 'Content-Type': 'application/json;charset=utf-8' }
  if (auth) {
    const token = getToken()
    if (token) header.Authorization = 'Bearer ' + token
  }
  if (loading) uni.showLoading({ title: '加载中', mask: true })

  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url + buildQuery(params),
      method,
      data: data || undefined,
      header,
      timeout: 15000,
      success: (res) => {
        const body = res.data || {}
        if (res.statusCode === 401 || body.code === 401) {
          toLogin()
          reject(new Error('登录状态已失效，请重新登录'))
          return
        }
        // 业务层 code 为 200 才算成功；其余一律抛出可读错误
        if (res.statusCode >= 200 && res.statusCode < 300 && (body.code === 200 || body.code === undefined)) {
          resolve(body)
          return
        }
        const message = body.msg || body.message || '请求失败，请稍后重试'
        const error = new Error(message)
        error.statusCode = res.statusCode
        error.body = body
        reject(error)
      },
      fail: (err) => {
        const error = new Error('网络异常，请检查网络后重试')
        error.raw = err
        reject(error)
      },
      complete: () => {
        if (loading) uni.hideLoading()
      }
    })
  })
}

export const http = {
  get: (url, params, options) => request({ url, method: 'GET', params, ...options }),
  post: (url, data, options) => request({ url, method: 'POST', data, ...options }),
  put: (url, data, options) => request({ url, method: 'PUT', data, ...options }),
  del: (url, options) => request({ url, method: 'DELETE', ...options })
}

export default http
