#!/bin/bash
# H5 Game 一键更新部署脚本
# 用法: bash deploy.sh [--skip-backend] [--skip-frontend] [--skip-admin]
set -e

PROJECT_DIR="/opt/h5-game"
FRONTEND_DIR="$PROJECT_DIR/frontend"
ADMIN_DIR="$PROJECT_DIR/admin"
BACKEND_DIR="$PROJECT_DIR/backend"
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
for arg in "$@"; do
  case $arg in
    --skip-backend) SKIP_BACKEND=true ;;
    --skip-frontend) SKIP_FRONTEND=true ;;
    --skip-admin) SKIP_ADMIN=true ;;
  esac
done

echo "=========================================="
echo "  H5 Game 一键更新部署"
echo "  时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "=========================================="

# 0. 拉取最新代码
echo ""
echo "[0/6] 拉取最新代码..."
cd "$PROJECT_DIR"
git fetch origin
git reset --hard origin/main
echo "  当前版本: $(git log --oneline -1)"
echo "  最近3条提交:"
git log --oneline -3

# 创建必要目录
mkdir -p "$WEB_ROOT/admin" "$LOG_DIR"

# 1. 后端构建+重启
if [ "$SKIP_BACKEND" = false ]; then
  echo ""
  echo "[1/6] 后端构建..."
  cd "$BACKEND_DIR"
  mvn clean package -DskipTests -q
  echo "  编译完成: $(ls -lh $BACKEND_JAR | awk '{print $5}')"

  # 备份旧jar
  if [ -f "$BACKEND_RUN_JAR" ]; then
    cp "$BACKEND_RUN_JAR" "$BACKEND_RUN_JAR.bak.$(date +%Y%m%d%H%M%S)"
    echo "  旧版本已备份"
  fi

  # 替换jar并重启
  cp "$BACKEND_JAR" "$BACKEND_RUN_JAR"
  echo "  重启后端服务..."
  systemctl restart h5-backend
  sleep 3

  # 健康检查
  if curl -sf http://127.0.0.1:8888/api/swagger-ui.html > /dev/null 2>&1; then
    echo "  ✅ 后端服务启动成功"
  else
    echo "  ⚠️  后端服务可能未完全启动，请检查: systemctl status h5-backend"
  fi
else
  echo ""
  echo "[1/6] 跳过后端构建"
fi

# 2. 用户端前端构建
if [ "$SKIP_FRONTEND" = false ]; then
  echo ""
  echo "[2/6] 用户端前端构建..."
  cd "$FRONTEND_DIR"
  npm install --silent 2>/dev/null
  npm run build
  echo "  构建完成: $(ls $DIST_DIR/assets/ 2>/dev/null | wc -l) 个资源文件"
  rsync -av --delete "$DIST_DIR/" "$WEB_ROOT/" > /dev/null
  echo "  已部署到 $WEB_ROOT"
else
  echo ""
  echo "[2/6] 跳过用户端构建"
fi

# 3. 管理端前端构建
if [ "$SKIP_ADMIN" = false ]; then
  echo ""
  echo "[3/6] 管理端前端构建..."
  cd "$ADMIN_DIR"
  pnpm install --silent 2>/dev/null || npm install --silent
  pnpm build 2>/dev/null || npm run build
  echo "  构建完成: $(ls $ADMIN_DIST/assets/ 2>/dev/null | wc -l) 个资源文件"
  rsync -av --delete "$ADMIN_DIST/" "$WEB_ROOT/admin/" > /dev/null
  echo "  已部署到 $WEB_ROOT/admin"
else
  echo ""
  echo "[3/6] 跳过管理端构建"
fi

# 4. 重载Nginx
echo ""
echo "[4/6] 重载Nginx..."
nginx -t && systemctl reload nginx
echo "  ✅ Nginx重载成功"

# 5. 清理旧备份(保留最近5个)
echo ""
echo "[5/6] 清理旧备份..."
ls -t "$BACKEND_DIR"/h5-backend-1.0.0.jar.bak.* 2>/dev/null | tail -n +6 | xargs -r rm -f
echo "  已清理，保留最近5个备份"

# 6. 部署完成
echo ""
echo "[6/6] 部署完成"
echo ""
echo "=========================================="
echo "  部署成功"
echo "=========================================="
echo "  用户端: https://h5.goodspage.cn"
echo "  管理端: https://h5.goodspage.cn/admin/"
echo "  后端:   http://127.0.0.1:8888/api/"
echo "  API文档: http://127.0.0.1:8888/api/swagger-ui.html"
echo ""
echo "  管理员: admin / yefeng"
echo "  服务状态: systemctl status h5-backend"
echo "  查看日志: tail -f $LOG_DIR/backend.log"
echo "=========================================="
