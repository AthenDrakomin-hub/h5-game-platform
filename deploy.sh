#!/bin/bash
# H5 Game 部署脚本
set -e

PROJECT_DIR="/opt/h5-game"
FRONTEND_DIR="$PROJECT_DIR/frontend"
BACKEND_JAR="$PROJECT_DIR/backend/target/h5-backend-1.0.0.jar"
DIST_DIR="$FRONTEND_DIR/dist"
NGINX_CONF="/etc/nginx/sites-available/goodspage.cn"

echo "=== H5 Game 部署开始 ==="

# 1. 后端部署
echo "[1/4] 后端构建..."
cd "$PROJECT_DIR/backend"
mvn clean package -DskipTests -q
echo "  后端编译完成: $(ls -lh $BACKEND_JAR | awk '{print $5}')"

# 2. 前端构建
echo "[2/4] 前端构建..."
cd "$FRONTEND_DIR"
npm ci --silent 2>/dev/null || npm install --silent
npm run build
echo "  前端构建完成: $(ls $DIST_DIR/assets/ 2>/dev/null | wc -l) 个资源文件"

# 3. 拷贝文件
echo "[3/4] 部署文件..."
cp "$BACKEND_JAR" "$PROJECT_DIR/backend/h5-backend-1.0.0.jar"
echo "  后端JAR已确认"

# 4. 重载Nginx
echo "[4/4] 重载Nginx..."
nginx -t && systemctl reload nginx
echo "  Nginx重载成功"

echo ""
echo "=== 部署完成 ==="
echo "前端: https://goodspage.cn"
echo "后端: http://127.0.0.1:18888/api/"
echo "状态: systemctl status h5-backend"
