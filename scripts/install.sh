#!/usr/bin/env bash
# =====================================================================
# 环游四海 · RuoYi 集成安装脚本（Linux / macOS / Git Bash）
# 作用：把本目录的独立成果「无侵入」地挂载到 ruoyi-springboot 与 ruoyi-vue3，
#      不修改框架既有业务代码，仅新增模块/文件 + 在父 pom 与 admin pom 挂一个模块。
# 用法：
#   ./scripts/install.sh [RUOYI_MODERN_ROOT]
#   默认 RUOYI_MODERN_ROOT=../..（即 ruoyi-modern 仓库根，含 ruoyi-springboot/ 与 ruoyi-vue3/）
# 卸载：
#   ./scripts/uninstall.sh [RUOYI_MODERN_ROOT]
# =====================================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUOYI_ROOT="${1:-$(cd "$ROOT_DIR/../.." && pwd)}"
BACKEND="$RUOYI_ROOT/ruoyi-springboot"
ADMIN="$RUOYI_ROOT/ruoyi-vue3"

echo "== 环游四海集成安装 =="
echo "成果目录: $ROOT_DIR"
echo "RuoYi 根目录: $RUOYI_ROOT"

if [ ! -f "$BACKEND/pom.xml" ]; then
  echo "[ERROR] 未找到 $BACKEND/pom.xml，请把第一个参数指向 ruoyi-modern 仓库根目录" >&2
  exit 1
fi

PY=python3
command -v python3 >/dev/null 2>&1 || PY=python

echo "-- 1/5 复制后端模块 ruoyi-hys"
rm -rf "$BACKEND/ruoyi-hys"
cp -r "$ROOT_DIR/backend/ruoyi-hys" "$BACKEND/ruoyi-hys"

echo "-- 2/5 挂载父 pom 与 ruoyi-admin 依赖"
"$PY" "$ROOT_DIR/scripts/patch_pom.py" "$BACKEND" add

echo "-- 3/5 复制管理端页面与接口"
mkdir -p "$ADMIN/src/api/hys" "$ADMIN/src/views/hys"
cp -r "$ROOT_DIR/admin/src/api/hys/." "$ADMIN/src/api/hys/"
cp -r "$ROOT_DIR/admin/src/views/hys/." "$ADMIN/src/views/hys/"

echo "-- 4/5 校验前端依赖（仅提示，不自动安装）"
if [ ! -d "$ADMIN/node_modules" ]; then
  echo "   [提示] $ADMIN/node_modules 不存在，请先在 ruoyi-vue3 执行 npm install --registry=https://registry.npmmirror.com"
fi

echo "-- 5/5 数据库脚本（需手动执行）"
cat <<EOF
   请按顺序执行：
     mysql -uroot -p ry-vue < $ROOT_DIR/sql/01_hys_schema.sql
     mysql -uroot -p ry-vue < $ROOT_DIR/sql/02_hys_menu.sql
     mysql -uroot -p ry-vue < $ROOT_DIR/sql/03_hys_demo_data.sql

   完成后：
     后端  cd $BACKEND && mvn -DskipTests clean package
     前端  cd $ADMIN && npm run dev
     小程序 cd $ROOT_DIR/uniapp && npm install && npm run dev:h5
EOF
echo "== 安装完成 =="
