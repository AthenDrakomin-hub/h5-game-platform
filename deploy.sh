#!/bin/bash
# H5 Game 部署脚本
set -e

PROJECT_DIR="/opt/h5-game"
FRONTEND_DIR="$PROJECT_DIR/frontend"
ADMIN_DIR="$PROJECT_DIR/admin"
BACKEND_JAR="$PROJECT_DIR/backend/target/h5-backend-1.0.0.jar"
DIST_DIR="$FRONTEND_DIR/dist"
ADMIN_DIST="$ADMIN_DIR/dist"

echo "=== H5 Game 部署开始 ==="

# 1. 后端部署
echo "[1/5] 后端构建..."
cd "$PROJECT_DIR/backend"
mvn clean package -DskipTests -q
echo "  后端编译完成: $(ls -lh $BACKEND_JAR | awk '{print $5}')"

# 2. 用户端前端构建
echo "[2/5] 用户端前端构建..."
cd "$FRONTEND_DIR"
npm ci --silent 2>/dev/null || npm install --silent
npm run build
echo "  用户端构建完成: $(ls $DIST_DIR/assets/ 2>/dev/null | wc -l) 个资源文件"

# 3. 管理端前端构建
echo "[3/5] 管理端前端构建..."
cd "$ADMIN_DIR"
pnpm install --silent 2>/dev/null || npm install --silent
pnpm build 2>/dev/null || npm run build
echo "  管理端构建完成: $(ls $ADMIN_DIST/assets/ 2>/dev/null | wc -l) 个资源文件"

# 4. 拷贝文件
echo "[4/5] 部署文件..."
cp "$BACKEND_JAR" "$PROJECT_DIR/backend/h5-backend-1.0.0.jar"
# 拷贝前端到Nginx目录
rsync -av --delete "$DIST_DIR/" "$PROJECT_DIR/web-root/"
rsync -av --delete "$ADMIN_DIST/" "$PROJECT_DIR/web-root/admin/"
echo "  文件部署完成"

# 5. 重载Nginx
echo "[5/5] 重载Nginx..."
nginx -t && systemctl reload nginx
echo "  Nginx重载成功"

echo ""
echo "=== 部署完成 ==="
echo "用户端: https://h5.goodspage.cn"
echo "管理端: https://h5.goodspage.cn/admin/"
echo "后端:   http://127.0.0.1:8888/api/"
echo "API文档: http://127.0.0.1:8888/api/swagger-ui.html"
echo ""
echo "管理员账号: admin / yefeng"
echo "状态: systemctl status h5-backend"
