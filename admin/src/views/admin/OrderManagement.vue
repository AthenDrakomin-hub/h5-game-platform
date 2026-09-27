<template>
  <div class="order-management">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
          <div class="header-actions">
            <el-select v-model="filterType" placeholder="订单类型" clearable style="width: 120px">
              <el-option label="全部" value="" />
              <el-option label="充值" value="recharge" />
              <el-option label="提现" value="withdraw" />
              <el-option label="投注" value="bet" />
            </el-select>
            <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 120px">
              <el-option label="全部" value="" />
              <el-option label="成功" value="success" />
              <el-option label="处理中" value="pending" />
              <el-option label="失败" value="failed" />
            </el-select>
            <el-button type="primary" @click="loadData">查询</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)" size="small">{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template #default="{ row }">
            <span :class="row.type === 'withdraw' ? 'text-red' : 'text-green'">
              {{ row.type === 'withdraw' ? '-' : '+' }}{{ row.amount || '--' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="method" label="支付方式" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" align="center">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
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

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="500px">
      <el-descriptions :column="1" border v-if="currentOrder">
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo || '--' }}</el-descriptions-item>
        <el-descriptions-item label="订单类型">{{ getTypeLabel(currentOrder.type) }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ currentOrder.amount || '--' }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ currentOrder.method || '--' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusLabel(currentOrder.status) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(currentOrder.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentOrder.remark || '--' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import * as adminApi from "@/api/admin";

const loading = ref(false);
const filterType = ref("");
const filterStatus = ref("");
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const tableData = ref<any[]>([]);
const detailVisible = ref(false);
const currentOrder = ref<any>(null);

const formatTime = (ts: any) => {
  if (!ts) return "--";
  if (typeof ts === "string") return ts;
  return new Date(ts).toLocaleString("zh-CN", { hour12: false });
};

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = { recharge: "充值", withdraw: "提现", bet: "投注", transfer: "转账" };
  return map[type] || type || "--";
};

const getTypeTag = (type: string) => {
  const map: Record<string, string> = { recharge: "success", withdraw: "warning", bet: "primary", transfer: "info" };
  return map[type] || "info";
};

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = { success: "成功", pending: "处理中", failed: "失败", cancelled: "已取消" };
  return map[status] || status || "--";
};

const getStatusTag = (status: string) => {
  const map: Record<string, string> = { success: "success", pending: "warning", failed: "danger", cancelled: "info" };
  return map[status] || "info";
};

const loadData = async () => {
  loading.value = true;
  try {
    const res: any = await adminApi.getOrderList({
      page: currentPage.value,
      pageSize: pageSize.value,
      type: filterType.value,
      status: filterStatus.value
    });
    if (res.code === 200) {
      const list = res.data?.list || res.data?.records || [];
      tableData.value = list.map((item: any) => ({
        orderNo: item.orderNo || item.transactionId || item.id,
        type: item.type || item.category,
        amount: item.amount,
        method: item.method || item.paymentMethod,
        status: item.status,
        createTime: item.createTime || item.createdAt,
        remark: item.remark
      }));
      total.value = res.data?.total || list.length;
    }
  } catch (e) {
    ElMessage.error("加载订单数据失败");
  } finally {
    loading.value = false;
  }
};

const viewDetail = (row: any) => {
  currentOrder.value = row;
  detailVisible.value = true;
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.order-management {
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
.text-red { color: #f56c6c; font-weight: 600; }
.text-green { color: #67c23a; font-weight: 600; }
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
