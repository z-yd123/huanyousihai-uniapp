-- =====================================================================
-- 环游四海 · 清理脚本（仅在需要回滚演示数据时手动执行）
-- 注意：会删除全部业务表与菜单，请在测试库执行。
-- =====================================================================
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS hys_wallet_txn;
DROP TABLE IF EXISTS hys_commission;
DROP TABLE IF EXISTS hys_payout_gap;
DROP TABLE IF EXISTS hys_fund_apply;
DROP TABLE IF EXISTS hys_order;
DROP TABLE IF EXISTS hys_route_day;
DROP TABLE IF EXISTS hys_album;
DROP TABLE IF EXISTS hys_product;
DROP TABLE IF EXISTS hys_member;
DROP TABLE IF EXISTS hys_config;

DELETE FROM sys_role_menu WHERE menu_id BETWEEN 3000 AND 3021;
DELETE FROM sys_menu WHERE menu_id BETWEEN 3000 AND 3021;

SET FOREIGN_KEY_CHECKS = 1;
