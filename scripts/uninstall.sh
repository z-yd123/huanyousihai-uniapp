#!/usr/bin/env bash
# =====================================================================
# 环游四海 · 集成卸载脚本
# 作用：把 install.sh 挂载的内容完整移除，让 ruoyi-springboot / ruoyi-vue3 回到干净状态。
# 用法：./scripts/uninstall.sh [RUOYI_MODERN_ROOT]
# =====================================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUOYI_ROOT="${1:-$(cd "$ROOT_DIR/../.." && pwd)}"
BACKEND="$RUOYI_ROOT/ruoyi-springboot"
ADMIN="$RUOYI_ROOT/ruoyi-vue3"

if [ ! -f "$BACKEND/pom.xml" ]; then
  echo "[ERROR] 未找到 $BACKEND/pom.xml，请把第一个参数指向 ruoyi-modern 仓库根目录" >&2
  exit 1
fi

PY=python3
command -v python3 >/dev/null 2>&1 || PY=python

echo "-- 1/3 还原父 pom 与 ruoyi-admin 依赖"
"$PY" "$ROOT_DIR/scripts/patch_pom.py" "$BACKEND" remove

echo "-- 2/3 删除后端模块"
rm -rf "$BACKEND/ruoyi-hys"

echo "-- 3/3 删除管理端页面与接口"
rm -rf "$ADMIN/src/api/hys" "$ADMIN/src/views/hys"

echo "== 已卸载（数据库表保留，如需清库请手动执行 sql/drop_hys.sql）=="
