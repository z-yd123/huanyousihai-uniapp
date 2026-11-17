# 环游四海 V1.0 · 全栈实现（uni-app + Spring Boot + Vue3 管理端）

> **作者**：zyd
> **仓库**：https://github.com/z-yd123/huanyousihai-uniapp
> **实现范围**：小程序端 21 页、后台服务 9 大模块（`ruoyi-hys` 独立模块）、管理端 9 个页面、佣金引擎与单测、SQL 与演示数据
> **隔离说明**：本项目为独立实现，通过 `scripts/install.sh` 以「新增模块 + 两处 pom 挂载」的方式接入 RuoYi 框架，**不修改框架任何既有 Java / Vue 业务代码**；如需并入班级共享仓库，整体拷入 `students/<你的标识>/` 即可，不会与其他同学的提交冲突。

---

## 一、这是什么

环游四海是一个「旅游线路 + 特产商城 + 两级分销」的业务闭环。本次实现把设计稿、业务规则文档、开发方案三份材料落成可运行的三端系统：

| 端 | 技术栈 | 位置 | 说明 |
| --- | --- | --- | --- |
| 小程序端 | uni-app（Vue3 + Vite） | `uniapp/` | 21 个页面，全部走真实接口，无 mock 数据 |
| 服务端 | Spring Boot 4.1 + MyBatis（RuoYi-Modern） | `backend/ruoyi-hys/` | 独立业务模块，9 大模块 / 63 个接口 |
| 管理端 | Vue3 + Element Plus（ruoyi-vue3） | `admin/` | 9 个页面，与后端权限码一一对应 |

三端闭环：**会员在小程序下单 → 服务端结算佣金并记账 → 运营在管理端审核提现、处理售后、核对异常账本**。

---

## 二、核心业务规则的实现方式

### 1. 两级佣金（`CommissionEngine`，纯函数 + 17 条单测）

| 场景 | 直推人 | 间推人 | 合计 |
| --- | --- | --- | --- |
| 新会员是该推荐人的第 1~2 个直推 | 100 元 | 100 元 | 200 元 |
| 第 3 个直推起 | 200 元 | 0 元 | 200 元 |
| 推荐链缺失（`referrerId` 为空） | — | — | 全额落 `hys_payout_gap` |

- 拨比恒为 200 元（`PAYOUT_TOTAL = 20000` 分），**受益人可以是 2 人也可以是 1 人**，但合计不允许多拨或少拨。
- 层级上限 `MAX_DEPTH = 2` 是代码内常量，不是配置项，管理端也不提供放大入口（`payout.max_depth` 在界面上是只读）。
- 计佣中断不静默：差额写入 `hys_payout_gap`，管理端「异常对账」页可见，必须人工核销。

> 关于 `AGENT.md` 自身的一处矛盾（§6.3 伪代码断言「受益人恒为 2 人」与 §6.1 表格/§8.3 用例「第 3 个直推起直推人独得 200」互相冲突），本实现按**可执行的 §6.1 + §8.3** 落地，断言改为「合计恒 200 + 层级 ≤ 2 + 有间推奖时恰好两条」。详见 `docs/04-业务规则实现说明.md`。

### 2. 金额与资金安全

- 金额全链路以「分」存 `BIGINT`，前端只在展示时除以 100，杜绝浮点误差。
- 提现手续费 6% **按分向下取整**（1000 元 → 手续费 60 元、到账 940 元；33.33 元 → 手续费 1.99 元）。
- 余额变动全部是条件更新：`update ... set balance = balance + #{delta} where member_id = ? and balance >= ?`，影响行数为 0 即视为余额不足，不做「先查后改」。
- 幂等：`hys_wallet_txn.biz_key` 唯一索引 + 互转请求体 `requestId`，网络重试不会重复扣款。
- 订单状态机用条件更新 `where status = #{from}` 实现，影响行数 0 即拒绝非法跳变（不存在「已退款 → 待付款」这种回头路）。

### 3. 权限与数据边界

- 小程序端：`/app/**` 走自定义 `HysAuthInterceptor` + 无状态会员 JWT（含 `token_version`）。改密 / 登出 / 被停用会让旧令牌立即失效。
- 会员 ID **一律从令牌取**，不接受前端传入；越权访问他人订单统一返回「订单不存在」，不泄露订单是否存在。
- 管理端：每个接口都有 `@PreAuthorize("@ss.hasPermi('hys:xxx:yyy')")`，权限码与 `sys_menu` 一一对应（3000~3021）。
- 返回给前端的 VO 里不出现 `password`、`referrer_path`、`token_version`；手机号统一脱敏为 `138****0004`。

---

## 三、验证结果

| 验证项 | 命令 | 结果 |
| --- | --- | --- |
| 佣金引擎单测 | `mvn -pl ruoyi-hys -am test -Dtest=CommissionEngineTest -Dsurefire.failIfNoSpecifiedTests=false` | **17/17 通过**（拨比 5 条、合规 8 条、手续费 4 条） |
| 后端子模块编译 | `mvn -pl ruoyi-hys -am -DskipTests compile` | **BUILD SUCCESS** |
| 后端全量打包 | `mvn -DskipTests package` | **BUILD SUCCESS**，产出 `ruoyi-admin.jar`（含 7 个模块） |
| 管理端构建 | `npm run build:prod` | 见 `docs/03-联调与验证记录.md` |
| 小程序 H5 构建 | `npm run build:h5` | 见 `docs/03-联调与验证记录.md` |

> 本机没有 MySQL / Redis 环境，因此未做「服务真机启动 + 全链路点按」验证；SQL 脚本、接口契约与前端构建均已单独验证，验证边界在 `docs/03-联调与验证记录.md` 中如实标注。

---

## 四、如何跑起来

### 0. 前置

- JDK 17、Maven 3.9+、Node 18+、MySQL 8.0
- 一个 RuoYi-Modern 仓库（含 `ruoyi-springboot/` 与 `ruoyi-vue3/`）

### 1. 挂载到框架

```bash
# 在 ruoyi-modern 仓库根目录
cd huanyousihai-uniapp
./scripts/install.sh /path/to/ruoyi-modern
```

脚本做三件事（幂等，可重复执行）：复制 `ruoyi-hys` 模块、在父 pom 的 `<modules>` 加一行、在 `ruoyi-admin/pom.xml` 加一个依赖。卸载用 `./scripts/uninstall.sh`。

### 2. 初始化数据库

```bash
mysql -uroot -p ry-vue < sql/01_hys_schema.sql      # 10 张业务表
mysql -uroot -p ry-vue < sql/02_hys_menu.sql        # 菜单与按钮权限
mysql -uroot -p ry-vue < sql/03_hys_demo_data.sql   # 脱敏演示数据
```

`03` 里的演示账号密码统一为 `123456`（BCrypt 已写入，非明文）。

### 3. 启动三端

```bash
# 服务端：8080
cd ruoyi-springboot && mvn -DskipTests clean package && java -jar ruoyi-admin/target/ruoyi-admin.jar

# 管理端：80（admin / admin123）
cd ruoyi-vue3 && npm install --registry=https://registry.npmmirror.com && npm run dev

# 小程序 H5：5173（已配 /dev-api 代理到 8080）
cd uniapp && npm install --registry=https://registry.npmmirror.com && npm run dev:h5
```

### 4. 演示账号

| 手机号 | 姓名 | 角色 | 看点 |
| --- | --- | --- | --- |
| `13800000004` | 周野 | 演示主账号 | 有 2 个直推、1580 元余额、完整佣金与团队数据 |
| `13800000003` | 林岚 | 周野的推荐人 | 可看间推佣金 |
| `13800000002` | 苏芮 | 团队顶层 | 可看团队人数口径 |

推荐链：`平台运营(1) → 苏芮(2) → 林岚(3) → 周野(4) → 陈一(5) / 何雨(6)`，另有 `苏芮 → 郑途(7)`。

---

## 五、目录结构

```
huanyousihai-uniapp/
├── backend/ruoyi-hys/          # 后端业务模块（独立可插拔）
│   ├── src/main/java/com/ruoyi/hys/
│   │   ├── config/             # JWT / 拦截器 / 上下文 / 常量
│   │   ├── controller/app/     # 小程序端 6 个控制器
│   │   ├── controller/admin/   # 管理端 9 个控制器
│   │   ├── domain/             # 实体 + DTO + VO
│   │   ├── engine/             # CommissionEngine（纯函数，可单测）
│   │   ├── mapper/             # MyBatis 接口
│   │   └── service/            # 9 个业务服务
│   └── src/test/               # 佣金引擎单测
├── admin/src/                  # 管理端页面与接口
│   ├── api/hys/index.js
│   └── views/hys/{dashboard,product,member,order,commission,payoutgap,fund,album,config}
├── uniapp/                     # 小程序端（Vue3 + Vite）
│   └── src/{api,store,pages,components,styles,utils}
├── sql/                        # 建表 / 菜单 / 演示数据 / 清理
├── scripts/                    # install.sh / uninstall.sh / patch_pom.py
└── docs/                       # 需求覆盖矩阵 / 接口文档 / 验证记录 / 规则说明
```

---

## 六、已知限制

1. 本机无 MySQL / Redis，未做端到端真机联调，仅完成编译、打包、单测、前端构建四项验证。
2. 图片素材沿用仓库内 `/static/img/` 的占位图，未接对象存储；后台上传为地址填写。
3. 短信验证码、微信登录、真实支付未接入（材料中列为后续版本能力），当前为「余额支付 + 人工审核充值/提现」。
4. 小视频、地图导航按材料要求**不实现**（合规红线：不提供地图与第三级分销图形）。
