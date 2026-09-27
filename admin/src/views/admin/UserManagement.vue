<template>
  <div class="user-management">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索用户名"
              clearable
              style="width: 200px"
              @keyup.enter="loadData"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button type="primary" @click="loadData">查询</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="userId" label="用户ID" width="100" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="balance" label="余额" width="120" align="right">
          <template #default="{ row }">
            <span class="balance-text">{{ row.balance || '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="vipLevel" label="VIP等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.vipLevel" type="warning" size="small">VIP{{ row.vipLevel }}</el-tag>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column prop="isTrial" label="账号类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isTrial ? 'info' : 'success'" size="small">
              {{ row.isTrial ? '试玩' : '正式' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" align="center">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
            <el-button type="warning" link size="small" @click="editUser(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 用户详情弹窗 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="500px">
      <el-descriptions :column="1" border v-if="currentUser">
        <el-descriptions-item label="用户ID">{{ currentUser.userId || '--' }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentUser.username || '--' }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ currentUser.nickname || '--' }}</el-descriptions-item>
        <el-descriptions-item label="余额">{{ currentUser.balance || '--' }}</el-descriptions-item>
        <el-descriptions-item label="VIP等级">{{ currentUser.vipLevel || '--' }}</el-descriptions-item>
        <el-descriptions-item label="账号类型">{{ currentUser.isTrial ? '试玩' : '正式' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatTime(currentUser.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { Search } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import * as adminApi from "@/api/admin";

const loading = ref(false);
const searchKeyword = ref("");
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const tableData = ref<any[]>([]);
const detailVisible = ref(false);
const currentUser = ref<any>(null);

const formatTime = (ts: any) => {
  if (!ts) return "--";
  if (typeof ts === "string") return ts;
  return new Date(ts).toLocaleString("zh-CN", { hour12: false });
};

const loadData = async () => {
  loading.value = true;
  try {
    // 注：真实后端用户列表需 admin 权限接口，当前用用户信息+交易记录替代展示
    const userRes: any = await adminApi.getUserList({ page: 1, pageSize: 10 });
    if (userRes.code === 200 && userRes.data) {
      const user = userRes.data;
      tableData.value = [{
        userId: user.userId || user.id,
        username: user.username,
        nickname: user.nickname,
        balance: user.balance,
        vipLevel: user.vipLevel,
        isTrial: user.isTrial,
        createTime: user.createTime
      }];
      total.value = 1;
    }
  } catch (e) {
    ElMessage.error("加载用户数据失败");
  } finally {
    loading.value = false;
  }
};

const viewDetail = (row: any) => {
  currentUser.value = row;
  detailVisible.value = true;
};

const editUser = (row: any) => {
  ElMessage.info("编辑功能需后端 admin 接口支持");
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.user-management {
  padding: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}
.header-actions {
  display: flex;
  gap: 8px;
}
.balance-text {
  color: #e6a23c;
  font-weight: 600;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
