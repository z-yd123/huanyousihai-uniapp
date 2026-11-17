-- =====================================================================
-- 环游四海 V1.0 · 业务库表结构（MySQL 8.0 / utf8mb4）
-- 作者：ZYD（students/zyd-hys-fullstack）
-- 约定：
--   1) 金额全链路以「分」为最小单位，字段统一 BIGINT，禁止 DECIMAL/浮点；
--   2) 逻辑删除统一 status/可见字段，不做物理删除（资金类表禁止 DELETE）；
--   3) 幂等键 biz_key 唯一索引，杜绝重复入账（充值/互转/提现/佣金）；
--   4) 与 RuoYi 自身表（sys_*）零耦合，独立建表，前缀 hys_。
-- 执行顺序：01_hys_schema.sql → 02_hys_menu.sql → 03_hys_demo_data.sql
-- =====================================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------------------
-- 会员：账号即手机号；referrer_id 非空是注册硬约束（佣金链路前置条件）
-- referrer_path 形如 ',1,5,9,'，用于团队（全部后代）计数，避免递归查询
-- direct_seq 记录「我是推荐人的第几个直推」，佣金分档依据，注册时一次性算好
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS hys_member;
CREATE TABLE hys_member (
  member_id       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '会员ID',
  phone           VARCHAR(20)  NOT NULL                COMMENT '手机号（登录账号）',
  nickname        VARCHAR(50)  NOT NULL                COMMENT '名称/昵称',
  password        VARCHAR(100) NOT NULL                COMMENT 'BCrypt 密码',
  province        VARCHAR(30)  NOT NULL                COMMENT '省',
  city            VARCHAR(30)  NOT NULL                COMMENT '市',
  district        VARCHAR(30)  NOT NULL                COMMENT '区/县',
  referrer_id     BIGINT       DEFAULT NULL            COMMENT '推荐人ID（直推上级）',
  referrer_path   VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '推荐链路径 ,rootId,...,selfId,',
  direct_seq      INT          NOT NULL DEFAULT 1      COMMENT '在推荐人名下的直推序号',
  level           CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '会员性质 0普通 1VIP',
  vip_expire_at   DATETIME     DEFAULT NULL            COMMENT 'VIP 到期时间',
  balance         BIGINT       NOT NULL DEFAULT 0      COMMENT '可用余额（分）',
  frozen          BIGINT       NOT NULL DEFAULT 0      COMMENT '冻结余额（分，提现审核中）',
  green_points    INT          NOT NULL DEFAULT 0      COMMENT '绿色积分（1元=1积分，1.0 占位）',
  status          CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '状态 0正常 1停用 2异常待补绑',
  token_version   INT          NOT NULL DEFAULT 0      COMMENT '令牌版本，改密/停用后旧令牌立即失效',
  last_login_at   DATETIME     DEFAULT NULL            COMMENT '最近登录时间',
  remark          VARCHAR(255) DEFAULT ''              COMMENT '备注',
  create_time     DATETIME     DEFAULT NULL            COMMENT '注册时间',
  update_time     DATETIME     DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (member_id),
  UNIQUE KEY uk_member_phone (phone),
  KEY idx_member_referrer (referrer_id),
  KEY idx_member_path (referrer_path),
  KEY idx_member_level (level, vip_expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员';

-- ---------------------------------------------------------------------
-- 商品：线路（ROUTE）/ 特产（SPECIAL）/ 会员权益（VIP_RIGHTS，固定 398）
-- 线路的行程分段单独存 hys_route_day，其余线路属性内联，减少联表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS hys_product;
CREATE TABLE hys_product (
  product_id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  product_type    VARCHAR(16)  NOT NULL                COMMENT 'ROUTE/SPECIAL/VIP_RIGHTS',
  route_type      VARCHAR(16)  DEFAULT NULL            COMMENT 'GROUP组团游 SELF_DRIVE自驾游',
  title           VARCHAR(120) NOT NULL                COMMENT '标题',
  subtitle        VARCHAR(200) DEFAULT ''              COMMENT '副标题/卖点',
  cover           VARCHAR(500) DEFAULT ''              COMMENT '封面图',
  images          TEXT                                 COMMENT '图集 JSON 数组',
  video_url       VARCHAR(500) DEFAULT ''              COMMENT '介绍小视频',
  detail          TEXT                                 COMMENT '图文详情',
  normal_price    BIGINT       NOT NULL DEFAULT 0      COMMENT '普通会员价（分）',
  vip_price       BIGINT       NOT NULL DEFAULT 0      COMMENT 'VIP 会员价（分）',
  stock           INT          NOT NULL DEFAULT 0      COMMENT '库存',
  sales           INT          NOT NULL DEFAULT 0      COMMENT '已售',
  days            INT          DEFAULT NULL            COMMENT '行程天数',
  nights          INT          DEFAULT NULL            COMMENT '行程晚数',
  depart_city     VARCHAR(50)  DEFAULT ''              COMMENT '出发/集合城市',
  gather_time     VARCHAR(50)  DEFAULT ''              COMMENT '集合时间',
  gather_place    VARCHAR(200) DEFAULT ''              COMMENT '集合地点（静态标记，非导航入口）',
  includes        VARCHAR(500) DEFAULT ''              COMMENT '费用包含',
  excludes        VARCHAR(500) DEFAULT ''              COMMENT '费用不含',
  status          CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '0上架 1下架',
  sort            INT          NOT NULL DEFAULT 0      COMMENT '排序',
  create_time     DATETIME     DEFAULT NULL,
  update_time     DATETIME     DEFAULT NULL,
  PRIMARY KEY (product_id),
  KEY idx_product_type (product_type, status, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品/线路';

DROP TABLE IF EXISTS hys_route_day;
CREATE TABLE hys_route_day (
  day_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '行程段ID',
  product_id    BIGINT       NOT NULL                COMMENT '所属线路',
  day_no        INT          NOT NULL                COMMENT '第几天',
  title         VARCHAR(120) NOT NULL                COMMENT '当日标题',
  content       TEXT                                 COMMENT '当日行程文案',
  images        TEXT                                 COMMENT '当日配图 JSON 数组',
  gather_time   VARCHAR(50)  DEFAULT ''              COMMENT '当日集合时间',
  gather_place  VARCHAR(200) DEFAULT ''              COMMENT '当日集合地点',
  PRIMARY KEY (day_id),
  UNIQUE KEY uk_route_day (product_id, day_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线路每日行程';

-- ---------------------------------------------------------------------
-- 订单：商品与线路统一入订单中心，状态机不可跳变
-- 0待付款 1待收货/待出行 2已完成 3售后中 4已退款 5已取消
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS hys_order;
CREATE TABLE hys_order (
  order_id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  order_no         VARCHAR(32)  NOT NULL                COMMENT '订单号',
  member_id        BIGINT       NOT NULL                COMMENT '下单会员',
  product_id       BIGINT       NOT NULL                COMMENT '商品ID',
  product_type     VARCHAR(16)  NOT NULL                COMMENT '冗余类型，便于订单中心分栏',
  product_title    VARCHAR(120) NOT NULL                COMMENT '下单快照标题',
  product_cover    VARCHAR(500) DEFAULT ''              COMMENT '下单快照封面',
  unit_price       BIGINT       NOT NULL                COMMENT '成交单价（分，下单时取价快照）',
  quantity         INT          NOT NULL DEFAULT 1      COMMENT '数量',
  amount           BIGINT       NOT NULL                COMMENT '应付金额（分）',
  pay_amount       BIGINT       DEFAULT NULL            COMMENT '实付金额（分）',
  status           TINYINT      NOT NULL DEFAULT 0      COMMENT '订单状态',
  pay_type         VARCHAR(16)  DEFAULT NULL            COMMENT 'BALANCE 余额支付',
  pay_time         DATETIME     DEFAULT NULL,
  travel_date      DATE         DEFAULT NULL            COMMENT '出行日期',
  contact_name     VARCHAR(50)  DEFAULT ''              COMMENT '联系人',
  contact_phone    VARCHAR(20)  DEFAULT ''              COMMENT '联系电话',
  remark           VARCHAR(255) DEFAULT ''              COMMENT '备注',
  ship_time        DATETIME     DEFAULT NULL            COMMENT '发货/成团时间',
  finish_time      DATETIME     DEFAULT NULL,
  refund_time      DATETIME     DEFAULT NULL,
  refund_reason    VARCHAR(255) DEFAULT ''              COMMENT '售后原因',
  after_sale       TINYINT      NOT NULL DEFAULT 0      COMMENT '0无 1申请中 2已退款 3已驳回',
  create_time      DATETIME     DEFAULT NULL,
  update_time      DATETIME     DEFAULT NULL,
  PRIMARY KEY (order_id),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_order_member (member_id, status),
  KEY idx_order_create (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

-- ---------------------------------------------------------------------
-- 佣金：层级只有两级，level 枚举仅 DIRECT/INDIRECT（COMPLIANCE 红线）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS hys_commission;
CREATE TABLE hys_commission (
  commission_id  BIGINT      NOT NULL AUTO_INCREMENT COMMENT '佣金ID',
  member_id      BIGINT      NOT NULL                COMMENT '受益人',
  order_id       BIGINT      NOT NULL                COMMENT '来源订单',
  order_no       VARCHAR(32) NOT NULL                COMMENT '来源订单号',
  level          VARCHAR(10) NOT NULL                COMMENT 'DIRECT直推 INDIRECT间推（仅两级）',
  amount         BIGINT      NOT NULL                COMMENT '金额（分）',
  status         TINYINT     NOT NULL DEFAULT 1      COMMENT '0待入账 1已入账 2已回冲',
  remark         VARCHAR(200) DEFAULT ''             COMMENT '说明（第几个直推等）',
  settle_time    DATETIME    DEFAULT NULL            COMMENT '入账时间',
  create_time    DATETIME    DEFAULT NULL,
  PRIMARY KEY (commission_id),
  UNIQUE KEY uk_commission_once (order_id, member_id, level),
  KEY idx_commission_member (member_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='佣金明细（两级封顶）';

-- ---------------------------------------------------------------------
-- 异常对账：正常业务不产生记录，仅 referrer 为空等脏数据时落库并告警
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS hys_payout_gap;
CREATE TABLE hys_payout_gap (
  gap_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '对账ID',
  order_id      BIGINT       NOT NULL                COMMENT '来源订单',
  order_no      VARCHAR(32)  NOT NULL                COMMENT '来源订单号',
  amount        BIGINT       NOT NULL                COMMENT '未发放金额（分）',
  reason        VARCHAR(40)  NOT NULL                COMMENT 'referrer_missing / indirect_missing',
  detail        VARCHAR(500) DEFAULT ''              COMMENT '异常说明',
  handled       CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '0待处理 1已补绑重算 2已人工核销',
  handle_remark VARCHAR(255) DEFAULT '',
  create_time   DATETIME     DEFAULT NULL,
  handle_time   DATETIME     DEFAULT NULL,
  PRIMARY KEY (gap_id),
  UNIQUE KEY uk_gap_order (order_id, reason),
  KEY idx_gap_handled (handled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='佣金异常对账（应恒为空）';

-- ---------------------------------------------------------------------
-- 钱包流水：biz_key 唯一，保证同一业务动作只入账一次
-- amount 带符号（收入正、支出负），balance_after 便于对账
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS hys_wallet_txn;
CREATE TABLE hys_wallet_txn (
  txn_id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  member_id      BIGINT      NOT NULL                COMMENT '会员',
  txn_type       VARCHAR(24) NOT NULL                COMMENT 'RECHARGE/TRANSFER_IN/TRANSFER_OUT/WITHDRAW/COMMISSION/ORDER_PAY/REFUND/COMMISSION_REVERSE',
  amount         BIGINT      NOT NULL                COMMENT '变动金额（分，带符号）',
  fee            BIGINT      NOT NULL DEFAULT 0      COMMENT '手续费（分）',
  balance_after  BIGINT      NOT NULL                COMMENT '变动后余额（分）',
  counterparty_id BIGINT     DEFAULT NULL            COMMENT '对手方会员',
  biz_key        VARCHAR(80) NOT NULL                COMMENT '幂等键',
  remark         VARCHAR(200) DEFAULT ''             COMMENT '摘要',
  create_time    DATETIME    DEFAULT NULL,
  PRIMARY KEY (txn_id),
  UNIQUE KEY uk_txn_biz (biz_key),
  KEY idx_txn_member (member_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包流水';

-- ---------------------------------------------------------------------
-- 资金申请：充值（线下打款后线上登记）与提现（人工审核）共用一张表
-- fee = 提现手续费（6%，按分向下取整）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS hys_fund_apply;
CREATE TABLE hys_fund_apply (
  apply_id      BIGINT      NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  apply_no      VARCHAR(32) NOT NULL                COMMENT '申请单号',
  member_id     BIGINT      NOT NULL                COMMENT '会员',
  apply_type    VARCHAR(16) NOT NULL                COMMENT 'RECHARGE/WITHDRAW',
  amount        BIGINT      NOT NULL                COMMENT '申请金额（分）',
  fee           BIGINT      NOT NULL DEFAULT 0      COMMENT '手续费（分）',
  actual_amount BIGINT      NOT NULL DEFAULT 0      COMMENT '实际到账/入账（分）',
  status        TINYINT     NOT NULL DEFAULT 0      COMMENT '0待审核 1已通过 2已驳回',
  account_name  VARCHAR(50) DEFAULT ''              COMMENT '收款人',
  account_no    VARCHAR(60) DEFAULT ''              COMMENT '收款账号（脱敏展示）',
  bank_name     VARCHAR(60) DEFAULT ''              COMMENT '开户行',
  remark        VARCHAR(255) DEFAULT ''             COMMENT '会员备注',
  audit_by      VARCHAR(50) DEFAULT ''              COMMENT '审核人',
  audit_time    DATETIME    DEFAULT NULL,
  audit_remark  VARCHAR(255) DEFAULT ''             COMMENT '审核意见',
  create_time   DATETIME    DEFAULT NULL,
  PRIMARY KEY (apply_id),
  UNIQUE KEY uk_apply_no (apply_no),
  KEY idx_apply_member (member_id, apply_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值/提现申请';

-- ---------------------------------------------------------------------
-- AI 智能相册站（398 权益）：运营后台代传素材，会员按参团批次浏览
-- media_json: [{category:HOTEL|FOOD|SCENIC|VIDEO, url, title}]
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS hys_album;
CREATE TABLE hys_album (
  album_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '相册ID',
  member_id    BIGINT       NOT NULL                COMMENT '所属会员',
  product_id   BIGINT       DEFAULT NULL            COMMENT '关联线路（批次）',
  title        VARCHAR(120) NOT NULL                COMMENT '批次标题',
  travel_date  DATE         DEFAULT NULL            COMMENT '出行日期',
  cover        VARCHAR(500) DEFAULT ''              COMMENT '封面',
  media_json   TEXT                                 COMMENT '素材 JSON 数组',
  description  VARCHAR(500) DEFAULT ''              COMMENT '回忆文案',
  status       CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '0可浏览 1隐藏',
  create_time  DATETIME     DEFAULT NULL,
  PRIMARY KEY (album_id),
  KEY idx_album_member (member_id, travel_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 智能相册';

-- ---------------------------------------------------------------------
-- 可配置业务参数（Q-xx 未确认项一律走配置，不硬编码）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS hys_config;
CREATE TABLE hys_config (
  config_id    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  config_key   VARCHAR(60)  NOT NULL                COMMENT '键',
  config_value VARCHAR(200) NOT NULL                COMMENT '值',
  config_name  VARCHAR(100) NOT NULL                COMMENT '名称',
  remark       VARCHAR(255) DEFAULT ''              COMMENT '说明',
  update_time  DATETIME     DEFAULT NULL,
  PRIMARY KEY (config_id),
  UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务参数';

SET FOREIGN_KEY_CHECKS = 1;
