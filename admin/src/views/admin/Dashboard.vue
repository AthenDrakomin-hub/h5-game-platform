<template>
  <div class="dashboard-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :xs="12" :sm="12" :md="6" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" :style="{ background: card.color }">
            <el-icon :size="24"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :md="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>近期交易趋势</span>
              <el-tag size="small" type="info">最近 7 天</el-tag>
            </div>
          </template>
          <div class="chart-placeholder">
            <el-empty description="图表组件待接入 ECharts" :image-size="80" />
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>游戏分布</span>
            </div>
          </template>
          <div class="chart-placeholder">
            <el-empty description="饼图待接入" :image-size="80" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近活动 & 系统状态 -->
    <el-row :gutter="16" class="bottom-row">
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近消息</span>
              <el-button text type="primary" @click="$router.push('/admin/messages')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentMessages" size="small" style="width: 100%">
            <el-table-column prop="title" label="标题" min-width="120" show-overflow-tooltip />
            <el-table-column prop="createTime" label="时间" width="160">
              <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>系统状态</span>
            </div>
          </template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="API 状态">
              <el-tag :type="apiStatus ? 'success' : 'danger'" size="small">
                {{ apiStatus ? '正常' : '异常' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="站点名称">{{ siteConfig.siteName || '--' }}</el-descriptions-item>
            <el-descriptions-item label="当前用户">{{ userInfo.username || '--' }}</el-descriptions-item>
            <el-descriptions-item label="用户余额">{{ userInfo.balance || '--' }}</el-descriptions-item>
            <el-descriptions-item label="VIP 等级">{{ userInfo.vipLevel || '--' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { User, Money, Box, Bell } from "@element-plus/icons-vue";
import * as adminApi from "@/api/admin";

const statCards = ref([
  { label: "注册用户", value: "--", icon: User, color: "#409eff" },
  { label: "今日交易", value: "--", icon: Money, color: "#67c23a" },
  { label: "活跃游戏", value: "--", icon: Box, color: "#e6a23c" },
  { label: "未读消息", value: "--", icon: Bell, color: "#f56c6c" }
]);

const recentMessages = ref<any[]>([]);
const siteConfig = ref<any>({});
const userInfo = ref<any>({});
const apiStatus = ref(false);

const formatTime = (ts: number) => {
  if (!ts) return "--";
  return new Date(ts).toLocaleString("zh-CN", { hour12: false });
};

onMounted(async () => {
  try {
    // 加载站点配置
    const configRes: any = await adminApi.getSiteConfigList();
    if (configRes.code === 200) {
      siteConfig.value = configRes.data || {};
      apiStatus.value = true;
    }

    // 加载用户信息
    const userRes: any = await adminApi.getDashboardStats();
    if (userRes.code === 200) {
      userInfo.value = userRes.data || {};
    }

    // 加载消息列表
    const msgRes: any = await adminApi.getMessageList({ page: 1, pageSize: 5 });
    if (msgRes.code === 200) {
      recentMessages.value = msgRes.data?.list || msgRes.data?.records || [];
    }

    // 加载游戏数量
    const gamesRes: any = await adminApi.getLotteryList();
    if (gamesRes.code === 200) {
      const count = Array.isArray(gamesRes.data) ? gamesRes.data.length : gamesRes.data?.total || 0;
      statCards.value[2].value = String(count);
    }
  } catch (e) {
    console.error("Dashboard load error:", e);
  }
});
</script>

<style scoped>
.dashboard-container {
  padding: 16px;
}
.stat-cards {
  margin-bottom: 16px;
}
.stat-card {
  margin-bottom: 16px;
}
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
.chart-row,
.bottom-row {
  margin-bottom: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}
.chart-placeholder {
  height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
