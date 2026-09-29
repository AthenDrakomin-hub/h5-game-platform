#!/bin/bash
# H5 Game 一键更新部署脚本
# 用法: bash deploy.sh [--skip-backend] [--skip-frontend] [--skip-admin] [--skip-bot]
set -e

PROJECT_DIR="/opt/h5-game"
FRONTEND_DIR="$PROJECT_DIR/frontend"
ADMIN_DIR="$PROJECT_DIR/admin"
BACKEND_DIR="$PROJECT_DIR/backend"
BOT_DIR="$PROJECT_DIR/bot"
BACKEND_JAR="$BACKEND_DIR/target/h5-backend-1.0.0.jar"
BACKEND_RUN_JAR="$BACKEND_DIR/h5-backend-1.0.0.jar"
DIST_DIR="$FRONTEND_DIR/dist"
ADMIN_DIST="$ADMIN_DIR/dist"
WEB_ROOT="$PROJECT_DIR/web-root"
LOG_DIR="$PROJECT_DIR/logs"

# 参数解析
SKIP_BACKEND=false
SKIP_FRONTEND=false
SKIP_ADMIN=false
SKIP_BOT=false
for arg in "$@"; do
  case $arg in
    --skip-backend) SKIP_BACKEND=true ;;
    --skip-frontend) SKIP_FRONTEND=true ;;
    --skip-admin) SKIP_ADMIN=true ;;
    --skip-bot) SKIP_BOT=true ;;
  esac
done

echo "=========================================="
echo "  H5 Game 一键更新部署"
echo "  时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "=========================================="

# 0. 拉取最新代码
echo ""
echo "[0/7] 拉取最新代码..."
cd "$PROJECT_DIR"
git fetch origin
git reset --hard origin/main
echo "  当前版本: $(git log --oneline -1)"
git log --oneline -3

mkdir -p "$WEB_ROOT/admin" "$LOG_DIR"

# 1. 后端构建 + 重启 + 健康检查 + 失败回滚
if [ "$SKIP_BACKEND" = false ]; then
  echo ""
  echo "[1/7] 后端构建..."
  cd "$BACKEND_DIR"
  mvn clean package -DskipTests -q
  echo "  编译完成: $(ls -lh $BACKEND_JAR | awk '{print $5}')"

  PREV_BAK=""
  if [ -f "$BACKEND_RUN_JAR" ]; then
    PREV_BAK="$BACKEND_RUN_JAR.bak.$(date +%Y%m%d%H%M%S)"
    cp "$BACKEND_RUN_JAR" "$PREV_BAK"
    echo "  旧版本已备份: $PREV_BAK"
  fi

  cp "$BACKEND_JAR" "$BACKEND_RUN_JAR"
  echo "  重启 h5-backend..."
  systemctl restart h5-backend

  # 健康检查（最多等 40s）
  HEALTH_URL="http://127.0.0.1:8888/api/wap/home/config"
  OK=false
  for i in $(seq 1 20); do
    if curl -sf "$HEALTH_URL" > /dev/null 2>&1; then
      OK=true; break
    fi
    sleep 2
  done

  if [ "$OK" = true ]; then
    echo "  ✅ 后端健康检查通过"
  else
    echo "  ❌ 后端健康检查失败，自动回滚..."
    if [ -n "$PREV_BAK" ] && [ -f "$PREV_BAK" ]; then
      cp "$PREV_BAK" "$BACKEND_RUN_JAR"
      systemctl restart h5-backend
      echo "  ↩️  已回滚到上一版本，请检查 journalctl -u h5-backend"
    else
      echo "  ⚠️  无历史备份可回滚，请手工处理"
    fi
    exit 1
  fi
else
  echo ""
  echo "[1/7] 跳过后端"
fi

# 2. Bot 构建 + 重启
if [ "$SKIP_BOT" = false ]; then
  echo ""
  echo "[2/7] Bot 构建..."
  cd "$BOT_DIR"
  npm install --silent 2>/dev/null
  npx tsc
  systemctl restart h5-bot
  sleep 2
  if systemctl is-active --quiet h5-bot; then
    echo "  ✅ h5-bot 已重启"
  else
    echo "  ❌ h5-bot 启动失败: journalctl -u h5-bot"
  fi
else
  echo ""
  echo "[2/7] 跳过 bot"
fi

# 3. 用户端前端
if [ "$SKIP_FRONTEND" = false ]; then
  echo ""
  echo "[3/7] 用户端前端构建..."
  cd "$FRONTEND_DIR"
  npm install --silent 2>/dev/null
  npm run build
  echo "  构建完成: $(ls $DIST_DIR/assets/ 2>/dev/null | wc -l) 个资源"
  rsync -av --delete "$DIST_DIR/" "$WEB_ROOT/" > /dev/null
  echo "  已部署到 $WEB_ROOT"
else
  echo ""
  echo "[3/7] 跳过用户端"
fi

# 4. 管理端前端
if [ "$SKIP_ADMIN" = false ]; then
  echo ""
  echo "[4/7] 管理端前端构建..."
  cd "$ADMIN_DIR"
  pnpm install --silent 2>/dev/null || npm install --silent
  pnpm build 2>/dev/null || npm run build
  rsync -av --delete "$ADMIN_DIST/" "$WEB_ROOT/admin/" > /dev/null
  echo "  已部署到 $WEB_ROOT/admin"
else
  echo ""
  echo "[4/7] 跳过管理端"
fi

# 5. Nginx
echo ""
echo "[5/7] 重载 Nginx..."
nginx -t && systemctl reload nginx

# 6. 清理旧备份
echo ""
echo "[6/7] 清理旧 jar 备份（保留最近5个）..."
ls -t "$BACKEND_DIR"/h5-backend-1.0.0.jar.bak.* 2>/dev/null | tail -n +6 | xargs -r rm -f

# 7. 完成
echo ""
echo "[7/7] 部署完成"
echo ""
echo "=========================================="
echo "  用户端:   https://h5.goodspage.cn"
echo "  管理端:   https://h5.goodspage.cn/admin/"
echo "  后端:     http://127.0.0.1:8888/api/"
echo "  Bot:      systemctl status h5-bot"
echo "  后端日志: journalctl -u h5-backend -f"
echo "=========================================="
