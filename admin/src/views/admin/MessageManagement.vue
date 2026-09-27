<template>
  <div class="message-management">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>消息管理</span>
          <el-button type="primary" @click="sendMessage">发送消息</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="消息标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.category || '系统' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isRead" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isRead ? 'info' : 'danger'" size="small">
              {{ row.isRead ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发送时间" width="170" align="center">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">查看</el-button>
            <el-button type="danger" link size="small" @click="deleteMessage(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 消息详情弹窗 -->
    <el-dialog v-model="detailVisible" title="消息详情" width="500px">
      <el-descriptions :column="1" border v-if="currentMessage">
        <el-descriptions-item label="消息标题">{{ currentMessage.title || '--' }}</el-descriptions-item>
        <el-descriptions-item label="消息分类">{{ currentMessage.category || '系统' }}</el-descriptions-item>
        <el-descriptions-item label="发送时间">{{ formatTime(currentMessage.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="消息内容">
          <div class="message-content">{{ currentMessage.content || currentMessage.summary || '--' }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import * as adminApi from "@/api/admin";

const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const tableData = ref<any[]>([]);
const detailVisible = ref(false);
const currentMessage = ref<any>(null);

const formatTime = (ts: any) => {
  if (!ts) return "--";
  if (typeof ts === "string") return ts;
  return new Date(ts).toLocaleString("zh-CN", { hour12: false });
};

const loadData = async () => {
  loading.value = true;
  try {
    const res: any = await adminApi.getMessageList({
      page: currentPage.value,
      pageSize: pageSize.value
    });
    if (res.code === 200) {
      const list = res.data?.list || res.data?.records || [];
      tableData.value = list.map((item: any) => ({
        ...item,
        isRead: typeof item.isRead === "number" ? item.isRead === 1 : item.isRead
      }));
      total.value = res.data?.total || list.length;
    }
  } catch (e) {
    ElMessage.error("加载消息列表失败");
  } finally {
    loading.value = false;
  }
};

const viewDetail = async (row: any) => {
  currentMessage.value = row;
  detailVisible.value = true;
  // 标记已读
  if (!row.isRead) {
    try {
      // 管理端消息无需标记已读
      row.isRead = true;
    } catch (e) {
      // 静默
    }
  }
};

const sendMessage = () => {
  ElMessage.info("发送消息需后端 admin 接口支持");
};

const deleteMessage = () => {
  ElMessageBox.confirm("确定删除该消息吗？", "提示", {
    type: "warning"
  }).then(() => {
    ElMessage.info("删除消息需后端 admin 接口支持");
  }).catch(() => {});
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.message-management {
  padding: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.message-content {
  max-height: 200px;
  overflow-y: auto;
  line-height: 1.6;
  white-space: pre-wrap;
}
</style>
