-- =====================================================================
-- 环游四海 · 管理端菜单与权限（RuoYi sys_menu）
-- 说明：
--   1) menu_id 使用 3000+ 段，避开若依内置菜单（1~2000）与生成器占用段；
--   2) 组件路径 hys/xxx/index 对应 ruoyi-vue3/src/views/hys/xxx/index.vue；
--   3) perms 与后端 @PreAuthorize("@ss.hasPermi('...')") 一一对应；
--   4) admin 超级管理员默认拥有全部菜单；其他角色请在「角色管理」里勾选「环游四海」目录。
-- 幂等：重复执行只会更新名称/权限，不会重复插入。
-- =====================================================================
SET NAMES utf8mb4;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3000, '环游四海', 0, 5, 'hys', NULL, '', '', 1, 0, 'M', '0', '0', '', 'guide', 'admin', NOW(), '环游四海运营目录')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), order_num = VALUES(order_num), icon = VALUES(icon);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3001, '运营看板', 3000, 1, 'dashboard', 'hys/dashboard/index', '', '', 1, 0, 'C', '0', '0', 'hys:dashboard:view', 'dashboard', 'admin', NOW(), '会员/订单/佣金/资金总览'),
(3002, '线路与商品', 3000, 2, 'product', 'hys/product/index', '', '', 1, 0, 'C', '0', '0', 'hys:product:list', 'shopping', 'admin', NOW(), '线路、特产、398 权益商品'),
(3003, '会员管理', 3000, 3, 'member', 'hys/member/index', '', '', 1, 0, 'C', '0', '0', 'hys:member:list', 'peoples', 'admin', NOW(), '会员、推荐关系、补绑'),
(3004, '订单中心', 3000, 4, 'order', 'hys/order/index', '', '', 1, 0, 'C', '0', '0', 'hys:order:list', 'list', 'admin', NOW(), '商品与线路订单统一处理'),
(3005, '佣金明细', 3000, 5, 'commission', 'hys/commission/index', '', '', 1, 0, 'C', '0', '0', 'hys:commission:list', 'money', 'admin', NOW(), '仅直推/间推两级'),
(3006, '资金审核', 3000, 6, 'fund', 'hys/fund/index', '', '', 1, 0, 'C', '0', '0', 'hys:fund:list', 'validCode', 'admin', NOW(), '充值登记与提现审核'),
(3007, '异常对账', 3000, 7, 'payoutgap', 'hys/payoutgap/index', '', '', 1, 0, 'C', '0', '0', 'hys:gap:list', 'chart', 'admin', NOW(), 'referrer 缺失等异常，正常应为空'),
(3008, '相册管理', 3000, 8, 'album', 'hys/album/index', '', '', 1, 0, 'C', '0', '0', 'hys:album:list', 'picture', 'admin', NOW(), 'AI 智能相册素材代传'),
(3009, '业务参数', 3000, 9, 'config', 'hys/config/index', '', '', 1, 0, 'C', '0', '0', 'hys:config:list', 'edit', 'admin', NOW(), '拨比/费率/上限等可配置项')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), parent_id = VALUES(parent_id), component = VALUES(component), perms = VALUES(perms);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3010, '商品查询', 3002, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'hys:product:query', '#', 'admin', NOW(), ''),
(3011, '商品新增', 3002, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'hys:product:add', '#', 'admin', NOW(), ''),
(3012, '商品修改', 3002, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'hys:product:edit', '#', 'admin', NOW(), ''),
(3013, '商品删除', 3002, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'hys:product:remove', '#', 'admin', NOW(), ''),
(3014, '会员修改', 3003, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'hys:member:edit', '#', 'admin', NOW(), ''),
(3015, '补绑推荐人', 3003, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'hys:member:rebind', '#', 'admin', NOW(), '仅异常账号可用，补绑后重算佣金'),
(3016, '订单发货', 3004, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'hys:order:ship', '#', 'admin', NOW(), ''),
(3017, '订单退款', 3004, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'hys:order:refund', '#', 'admin', NOW(), '退款同步回冲佣金'),
(3018, '资金审核', 3006, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'hys:fund:audit', '#', 'admin', NOW(), ''),
(3019, '异常处理', 3007, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'hys:gap:handle', '#', 'admin', NOW(), ''),
(3020, '相册维护', 3008, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'hys:album:edit', '#', 'admin', NOW(), ''),
(3021, '参数修改', 3009, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'hys:config:edit', '#', 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), parent_id = VALUES(parent_id), perms = VALUES(perms);
