-- =====================================================================
-- 环游四海 · 演示数据（脱敏样例，禁止写入真实手机号与密码）
-- 演示口令统一为 123456（BCrypt 10 轮），由 scripts/reset-demo-password 生成后替换占位符
-- 推荐链：平台运营(1) → 苏芮(2) → 林岚(3) → 周野(4) → 陈一(5) / 何雨(6)
--                                              └→ 郑途(7)（苏芮第 2 个直推）
-- 演示登录：13800000004 / 123456（周野，VIP 会员，有余额、有佣金、有订单、有相册）
-- =====================================================================
SET NAMES utf8mb4;

DELETE FROM hys_commission;
DELETE FROM hys_wallet_txn;
DELETE FROM hys_order;
DELETE FROM hys_fund_apply;
DELETE FROM hys_payout_gap;
DELETE FROM hys_route_day;
DELETE FROM hys_album;
DELETE FROM hys_product;
DELETE FROM hys_member;
DELETE FROM hys_config;

INSERT INTO hys_member (member_id, phone, nickname, password, province, city, district, referrer_id, referrer_path, direct_seq, level, vip_expire_at, balance, frozen, green_points, status, create_time, remark) VALUES
(1, '13800000001', '平台运营', '$2a$10$oei9dydSWg0JCtnRIiujW.H5CLEzDQEChf3ihzlshKb6BkxwSA9L6', '广东省', '广州市', '天河区', NULL, ',1,', 0, '1', '2099-12-31 23:59:59', 0, 0, 0, '0', '2026-01-01 09:00:00', '平台根账号（仅用于推荐链根节点，不参与计佣）'),
(2, '13800000002', '苏芮', '$2a$10$oei9dydSWg0JCtnRIiujW.H5CLEzDQEChf3ihzlshKb6BkxwSA9L6', '广东省', '广州市', '越秀区', 1, ',1,2,', 1, '1', '2027-06-30 23:59:59', 120000, 0, 186, '0', '2026-02-11 10:20:00', ''),
(3, '13800000003', '林岚', '$2a$10$oei9dydSWg0JCtnRIiujW.H5CLEzDQEChf3ihzlshKb6BkxwSA9L6', '云南省', '昆明市', '五华区', 2, ',1,2,3,', 1, '1', '2027-03-01 23:59:59', 86000, 0, 92, '0', '2026-03-02 15:05:00', ''),
(4, '13800000004', '周野', '$2a$10$oei9dydSWg0JCtnRIiujW.H5CLEzDQEChf3ihzlshKb6BkxwSA9L6', '云南省', '昆明市', '盘龙区', 3, ',1,2,3,4,', 1, '1', '2027-11-01 23:59:59', 158000, 0, 45, '0', '2026-05-18 11:40:00', '演示主账号'),
(5, '13800000005', '陈一', '$2a$10$oei9dydSWg0JCtnRIiujW.H5CLEzDQEChf3ihzlshKb6BkxwSA9L6', '四川省', '成都市', '武侯区', 4, ',1,2,3,4,5,', 1, '0', NULL, 5000, 0, 3, '0', '2026-09-06 09:12:00', '周野第 1 个直推'),
(6, '13800000006', '何雨', '$2a$10$oei9dydSWg0JCtnRIiujW.H5CLEzDQEChf3ihzlshKb6BkxwSA9L6', '浙江省', '杭州市', '西湖区', 4, ',1,2,3,4,6,', 2, '0', NULL, 0, 0, 0, '0', '2026-10-02 14:26:00', '周野第 2 个直推'),
(7, '13800000007', '郑途', '$2a$10$oei9dydSWg0JCtnRIiujW.H5CLEzDQEChf3ihzlshKb6BkxwSA9L6', '江苏省', '南京市', '鼓楼区', 2, ',1,2,7,', 2, '0', NULL, 39800, 0, 0, '0', '2026-10-20 19:30:00', '苏芮第 2 个直推');

-- 商品：3 条线路 + 2 件特产 + 1 个 398 会员权益
INSERT INTO hys_product (product_id, product_type, route_type, title, subtitle, cover, images, video_url, detail, normal_price, vip_price, stock, sales, days, nights, depart_city, gather_time, gather_place, includes, excludes, status, sort, create_time) VALUES
(1, 'ROUTE', 'GROUP', '西双版纳 6 天 5 晚 · 雨林秘境', '走进北纬 21° 的绿野，与野象谷的清晨一起醒来', '/static/img/route-xishuangbanna.jpg', '["/static/img/xsbn-1.jpg","/static/img/xsbn-2.jpg","/static/img/xsbn-3.jpg"]', '', '<p>全程 5 晚精选酒店，含 3 次特色餐：傣味手抓饭、雨林簸箕宴、澜沧江江鱼。</p><p>16 人小团，昆明集合、景洪接站，全程空调旅游车，不进购物店。</p>', 468000, 398000, 30, 12, 6, 5, '昆明', '07:00', '昆明长水国际机场 T1 到达层 3 号门', '往返大交通、5 晚酒店、行程内门票、7 正 5 早、旅游意外险', '个人消费、单房差、景区内二次消费', '0', 1, '2026-09-01 10:00:00'),
(2, 'ROUTE', 'SELF_DRIVE', '大理 · 苍山洱海 4 天 3 晚 慢行', '不赶路，只赶日落；车在环海西路，人在风里', '/static/img/route-dali.jpg', '["/static/img/dali-1.jpg","/static/img/dali-2.jpg"]', '', '<p>自驾打包线路：含 3 晚海景客栈、环海西路补给点、双廊日落餐厅预留位。</p><p>不含车辆与油费，平台提供路书与沿途停靠点坐标说明（本期不做导航）。</p>', 268000, 228000, 50, 8, 4, 3, '大理', '09:30', '大理古城南门游客中心', '3 晚住宿、6 正 3 早、喜洲扎染体验、路书与应急联络', '车辆租赁、油费过路费、个人消费', '0', 2, '2026-09-05 10:00:00'),
(3, 'ROUTE', 'GROUP', '稻城亚丁 5 天 4 晚 · 雪山朝圣', '牛奶海与五色海之间，把呼吸交给海拔 4600 米', '/static/img/route-daocheng.jpg', '["/static/img/dc-1.jpg","/static/img/dc-2.jpg"]', '', '<p>成都集散，全程供氧车，配备高原应急包与随队医护联络人。</p><p>含亚丁景区门票与观光车，洛绒牛场徒步由领队带队。</p>', 528000, 468000, 20, 6, 5, 4, '成都', '06:30', '成都东站西广场旅游集散中心', '全程用车、4 晚酒店、门票与观光车、8 正 4 早、氧气与应急包', '往返成都大交通、骑马费用、个人消费', '0', 3, '2026-09-10 10:00:00'),
(4, 'SPECIAL', NULL, '云南野生菌礼盒 · 当季鲜采', '松茸、牛肝菌、鸡枞三味合一，48 小时冷链直达', '/static/img/product-mushroom.jpg', '["/static/img/mushroom-1.jpg"]', '', '<p>产地直采，-18℃ 锁鲜，附赠菌汤包与做法卡。</p>', 19800, 16800, 200, 46, NULL, NULL, '', '', '', '2.5kg 礼盒、冷链配送', '偏远地区补运费', '0', 1, '2026-09-12 10:00:00'),
(5, 'SPECIAL', NULL, '大理手工扎染围巾 · 蓝白', '一块板蓝根染出的云，草木染非遗工坊出品', '/static/img/product-tiedye.jpg', '["/static/img/tiedye-1.jpg"]', '', '<p>纯棉手工扎染，每条纹理唯一，附工坊溯源卡。</p>', 26800, 22800, 120, 31, NULL, NULL, '', '', '', '围巾 1 条、礼盒包装', '无', '0', 2, '2026-09-12 10:00:00'),
(6, 'VIP_RIGHTS', NULL, 'AI 智能相册网站 · 会员权益', '价值 3980 元的专属旅行相册站，升级 VIP 仅需 398 元', '/static/img/vip-album.jpg', '["/static/img/vip-1.jpg"]', '', '<p>专属域名、AI 自动成片、运营代传素材，一辈子的旅行回忆。</p><p>购买后立即升级 VIP 会员，线路与商城享 VIP 价，次年 99 元/年续费。</p>', 39800, 39800, 9999, 128, NULL, NULL, '', '', '', '独立相册站 1 年、运营代传、AI 成片', '域名续费、次年服务费', '0', 1, '2026-09-01 09:00:00');

INSERT INTO hys_route_day (product_id, day_no, title, content, images, gather_time, gather_place) VALUES
(1, 1, '昆明集合 · 飞抵景洪', '14:00 昆明长水机场集合，统一乘车前往景洪，沿途讲解傣乡风俗。18:30 入住告庄酒店，晚上自由逛星光夜市。', '["/static/img/xsbn-d1.jpg"]', '14:00', '昆明长水国际机场 T1 到达层 3 号门'),
(1, 2, '中科院热带植物园', '07:00 起床，07:30 早餐，08:00 出发。上午游览西区奇花异卉园，下午东区热带雨林徒步 3 公里。', '["/static/img/xsbn-d2.jpg"]', '08:00', '酒店大堂'),
(1, 3, '野象谷 · 雨林牧象', '06:30 起床，07:00 出发前往野象谷，走高空栈道观测亚洲象活动痕迹，下午体验雨林牧象。', '["/static/img/xsbn-d3.jpg"]', '07:00', '酒店大堂'),
(1, 4, '勐景来 · 中缅第一寨', '上午走访傣族村寨，体验慢轮制陶与古法造纸；下午返回景洪，傣味手抓饭晚宴。', '["/static/img/xsbn-d4.jpg"]', '08:30', '酒店大堂'),
(1, 5, '曼听公园 · 澜沧江夜游', '上午曼听公园与总佛寺，下午自由活动，19:00 澜沧江夜游船，含江鱼晚餐。', '["/static/img/xsbn-d5.jpg"]', '09:00', '酒店大堂'),
(1, 6, '送站返程', '睡到自然醒，10:00 退房，统一送景洪嘎洒机场，行程结束。', '[]', '10:00', '酒店大堂'),
(2, 1, '大理古城 · 环海西路', '09:30 大理古城南门集合，领取路书与补给包。下午自驾环海西路，才村码头看日落，晚上古城人民路自由晚餐。', '["/static/img/dali-d1.jpg"]', '09:30', '大理古城南门游客中心'),
(2, 2, '喜洲 · 双廊', '上午喜洲古镇与扎染工坊体验，下午沿环海东路至双廊，日落餐厅预留位用餐。', '["/static/img/dali-d2.jpg"]', '10:00', '酒店停车场'),
(2, 3, '苍山 · 寂照庵', '上午苍山感通索道，下午寂照庵多肉庭院喝茶，晚上回古城。', '["/static/img/dali-d3.jpg"]', '09:00', '酒店停车场'),
(2, 4, '沙溪方向自由返程', '上午自由安排，12:00 前退房，路书附带返程补给点与加油建议。', '[]', '12:00', '酒店大堂'),
(3, 1, '成都集合 · 康定', '06:30 成都东站集合出发，翻越二郎山，傍晚抵达康定，适应海拔。', '["/static/img/dc-d1.jpg"]', '06:30', '成都东站西广场旅游集散中心'),
(3, 2, '折多山 · 新都桥', '上午翻折多山垭口，下午新都桥摄影天堂沿线停靠，晚宿雅江。', '["/static/img/dc-d2.jpg"]', '07:00', '酒店大堂'),
(3, 3, '理塘 · 稻城', '沿 318 国道西行，途经卡子拉山与理塘毛垭大草原，晚宿稻城。', '["/static/img/dc-d3.jpg"]', '07:00', '酒店大堂'),
(3, 4, '亚丁景区 · 牛奶海', '07:00 进景区，洛绒牛场徒步至牛奶海、五色海，全程领队带队，17:30 出景区。', '["/static/img/dc-d4.jpg"]', '07:00', '景区游客中心'),
(3, 5, '返程成都', '早餐后返程，途整理照片，20:00 前后抵达成都东站，行程结束。', '[]', '08:00', '酒店大堂');

-- 订单：覆盖待付款 / 待出行 / 已完成 / 售后
INSERT INTO hys_order (order_id, order_no, member_id, product_id, product_type, product_title, product_cover, unit_price, quantity, amount, pay_amount, status, pay_type, pay_time, travel_date, contact_name, contact_phone, remark, ship_time, finish_time, refund_time, refund_reason, after_sale, create_time) VALUES
(1001, 'HYS202609150001', 4, 6, 'VIP_RIGHTS', 'AI 智能相册网站 · 会员权益', '/static/img/vip-album.jpg', 39800, 1, 39800, 39800, 2, 'BALANCE', '2026-09-15 20:12:00', NULL, '周野', '138****0004', '升级 VIP', '2026-09-15 20:12:30', '2026-09-16 10:00:00', NULL, '', 0, '2026-09-15 20:10:00'),
(1002, 'HYS202610080002', 4, 1, 'ROUTE', '西双版纳 6 天 5 晚 · 雨林秘境', '/static/img/route-xishuangbanna.jpg', 398000, 1, 398000, 398000, 1, 'BALANCE', '2026-10-08 09:31:00', '2026-12-06', '周野', '138****0004', '需要大床房', '2026-10-20 16:00:00', NULL, NULL, '', 0, '2026-10-08 09:30:00'),
(1003, 'HYS202611120003', 4, 4, 'SPECIAL', '云南野生菌礼盒 · 当季鲜采', '/static/img/product-mushroom.jpg', 16800, 1, 16800, NULL, 0, NULL, NULL, NULL, '周野', '138****0004', '', NULL, NULL, NULL, '', 0, '2026-11-12 21:05:00'),
(1004, 'HYS202610200004', 4, 5, 'SPECIAL', '大理手工扎染围巾 · 蓝白', '/static/img/product-tiedye.jpg', 22800, 1, 22800, 22800, 4, 'BALANCE', '2026-10-20 12:00:00', NULL, '周野', '138****0004', '颜色与图片不符', '2026-10-21 09:00:00', NULL, '2026-10-24 15:20:00', '颜色与图片不符，申请退货', 2, '2026-10-20 11:58:00'),
(1005, 'HYS202610010005', 5, 6, 'VIP_RIGHTS', 'AI 智能相册网站 · 会员权益', '/static/img/vip-album.jpg', 39800, 1, 39800, 39800, 2, 'BALANCE', '2026-10-01 10:00:00', NULL, '陈一', '138****0005', '', '2026-10-01 10:00:20', '2026-10-02 09:00:00', NULL, '', 0, '2026-10-01 09:58:00');

-- 佣金：两笔 398 订单，各拨满 200 元；第 1 个直推拆 100+100
INSERT INTO hys_commission (member_id, order_id, order_no, level, amount, status, remark, settle_time, create_time) VALUES
(3, 1001, 'HYS202609150001', 'DIRECT', 10000, 1, '周野是林岚第 1 个直推，直推奖 100 元', '2026-09-15 20:12:05', '2026-09-15 20:12:05'),
(2, 1001, 'HYS202609150001', 'INDIRECT', 10000, 1, '间推奖 100 元，拨给推荐的推荐（苏芮）', '2026-09-15 20:12:05', '2026-09-15 20:12:05'),
(4, 1005, 'HYS202610010005', 'DIRECT', 10000, 1, '陈一是周野第 1 个直推，直推奖 100 元', '2026-10-01 10:00:05', '2026-10-01 10:00:05'),
(3, 1005, 'HYS202610010005', 'INDIRECT', 10000, 1, '间推奖 100 元，拨给推荐的推荐（林岚）', '2026-10-01 10:00:05', '2026-10-01 10:00:05');

-- 钱包流水（周野）：充值 → 消费 → 佣金 → 提现
INSERT INTO hys_wallet_txn (member_id, txn_type, amount, fee, balance_after, counterparty_id, biz_key, remark, create_time) VALUES
(4, 'RECHARGE', 200000, 0, 200000, NULL, 'RECHARGE:9001', '线下打款登记入账 2000.00 元', '2026-09-10 10:00:00'),
(4, 'ORDER_PAY', -39800, 0, 160200, NULL, 'ORDER:1001', '购买会员权益，升级 VIP', '2026-09-15 20:12:00'),
(4, 'COMMISSION', 10000, 0, 170200, 5, 'COMM:1005:4:DIRECT', '直推奖 · 陈一升级 VIP', '2026-10-01 10:00:05'),
(4, 'WITHDRAW', -30000, 1800, 148400, NULL, 'WITHDRAW:8801', '提现 300.00 元，手续费 18.00 元', '2026-10-12 16:20:00'),
(4, 'REFUND', 22800, 0, 171200, NULL, 'REFUND:1004', '订单 1004 退款到余额', '2026-10-24 15:20:00');

-- 资金申请：一笔待审核提现 + 一笔待审核充值
INSERT INTO hys_fund_apply (apply_no, member_id, apply_type, amount, fee, actual_amount, status, account_name, account_no, bank_name, remark, create_time) VALUES
('FA202611150001', 4, 'WITHDRAW', 50000, 3000, 47000, 0, '周野', '6217****8899', '招商银行昆明分行', '提现用于日常开支', '2026-11-15 10:20:00'),
('FA202611160002', 5, 'RECHARGE', 100000, 0, 100000, 0, '', '', '', '已线下转账，请核账', '2026-11-16 09:05:00');

-- 相册：周野两次参团
INSERT INTO hys_album (member_id, product_id, title, travel_date, cover, media_json, description, status, create_time) VALUES
(4, 1, '西双版纳 · 雨林里的六天', '2026-08-06', '/static/img/xsbn-1.jpg', '[{"category":"SCENIC","url":"/static/img/xsbn-1.jpg","title":"望天树空中走廊"},{"category":"SCENIC","url":"/static/img/xsbn-2.jpg","title":"野象谷清晨"},{"category":"HOTEL","url":"/static/img/xsbn-hotel.jpg","title":"告庄江景房"},{"category":"FOOD","url":"/static/img/xsbn-food.jpg","title":"傣味手抓饭"},{"category":"VIDEO","url":"/static/img/xsbn-video.jpg","title":"澜沧江夜游 30 秒"}]', 'AI 已自动成片 1 支，运营补传 12 张，可下载原图。', '0', '2026-08-12 10:00:00'),
(4, 2, '大理 · 四天追了三个日落', '2026-05-18', '/static/img/dali-1.jpg', '[{"category":"SCENIC","url":"/static/img/dali-1.jpg","title":"环海西路日落"},{"category":"SCENIC","url":"/static/img/dali-2.jpg","title":"双廊洱海"},{"category":"FOOD","url":"/static/img/dali-food.jpg","title":"喜洲破酥粑粑"},{"category":"HOTEL","url":"/static/img/dali-hotel.jpg","title":"海景客栈阳台"}]', '自驾批次的照片已整理，含路书打卡点合影。', '0', '2026-05-22 18:00:00');

-- 业务参数（Q-xx 待确认项全部配置化，不硬编码）
INSERT INTO hys_config (config_key, config_value, config_name, remark) VALUES
('vip.price', '39800', 'VIP 升级价（分）', '对外口径：价值 3980 元，升级仅需 398 元'),
('vip.renew_price', '9900', 'VIP 续费价（分/年）', '第二年起 99 元/年'),
('vip.renew_years', '1', 'VIP 单次续费年限', ''),
('vip.expire_affects_commission', 'false', 'VIP 过期是否影响佣金', 'Q-04b 待确认，默认不影响'),
('payout.total', '20000', '单笔订单拨比总额（分）', 'COMPLIANCE：恒为 200 元，不多拨不少拨'),
('payout.direct_first_two', '10000', '第 1/2 个直推的直推奖（分）', ''),
('payout.indirect_first_two', '10000', '第 1/2 个直推的间推奖（分）', 'Q-05：剩余 100 元归间推人，平台不留存'),
('payout.direct_after', '20000', '第 3 个起直推的直推奖（分）', '拨比用满，上级不再另发'),
('payout.max_depth', '2', '分销层级上限', '国家级红线：仅两级，代码内为常量，不可配置放大'),
('payout.withdraw_fee_rate', '0.06', '提现手续费率', '6%，按分向下取整'),
('payout.renew_commission_rate', '0', '续费订单计佣比例', 'Q-03 待定，默认 0（不计佣）'),
('wallet.recharge_limit', '500000', '单笔充值上限（分）', '5000 元，超出需拆分'),
('wallet.transfer_daily_limit', '2000000', '互转单日累计上限（分）', '20000 元'),
('wallet.transfer_daily_count', '20', '互转单日次数上限', ''),
('share.team_level', '2', '团队统计层级', '团队人数=全部后代（当前链路最多两级）'),
('poster.templates', '4', '海报模板数量', ''),
('app.notice', '分销仅两级，合法合规；提现收取 6% 手续费并代扣个税提示', '小程序合规提示文案', '配置注入，前端常驻可见')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), config_name = VALUES(config_name), remark = VALUES(remark);
