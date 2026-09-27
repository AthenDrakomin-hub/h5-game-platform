<template>
  <div class="promo-management">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>活动管理</span>
          <el-button type="primary" @click="addPromo">新增活动</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="活动标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160" align="center">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="160" align="center">
          <template #default="{ row }">{{ formatTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">查看</el-button>
            <el-button type="warning" link size="small" @click="editPromo(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="deletePromo(row)">删除</el-button>
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

    <!-- 活动详情弹窗 -->
    <el-dialog v-model="detailVisible" title="活动详情" width="600px">
      <el-descriptions :column="1" border v-if="currentPromo">
        <el-descriptions-item label="活动标题">{{ currentPromo.title || '--' }}</el-descriptions-item>
        <el-descriptions-item label="活动分类">{{ currentPromo.categoryName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="活动状态">{{ currentPromo.status === 1 ? '进行中' : '已结束' }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatTime(currentPromo.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatTime(currentPromo.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="活动内容">
          <div class="promo-content" v-html="currentPromo.content || currentPromo.description || '--'"></div>
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
const currentPromo = ref<any>(null);

const formatTime = (ts: any) => {
  if (!ts) return "--";
  if (typeof ts === "string") return ts;
  return new Date(ts).toLocaleString("zh-CN", { hour12: false });
};

const loadData = async () => {
  loading.value = true;
  try {
    const res: any = await adminApi.getPromoList({
      page: currentPage.value,
      pageSize: pageSize.value
    });
    if (res.code === 200) {
      const list = res.data?.list || res.data?.records || [];
      tableData.value = list;
      total.value = res.data?.total || list.length;
    }
  } catch (e) {
    ElMessage.error("加载活动列表失败");
  } finally {
    loading.value = false;
  }
};

const viewDetail = (row: any) => {
  currentPromo.value = row;
  detailVisible.value = true;
};

const addPromo = () => {
  ElMessage.info("新增活动需后端 admin 接口支持");
};

const editPromo = () => {
  ElMessage.info("编辑活动需后端 admin 接口支持");
};

const deletePromo = (row: any) => {
  ElMessageBox.confirm("确定删除该活动吗？", "提示", {
    type: "warning"
  }).then(() => {
    ElMessage.info("删除活动需后端 admin 接口支持");
  }).catch(() => {});
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.promo-management {
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
.promo-content {
  max-height: 300px;
  overflow-y: auto;
  line-height: 1.6;
}
</style>
