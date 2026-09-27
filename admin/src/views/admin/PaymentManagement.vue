<template>
  <div class="payment-management">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>支付方式管理</span>
          <div class="header-actions">
            <el-select v-model="filterType" placeholder="类型" clearable style="width: 120px">
              <el-option label="全部" value="" />
              <el-option label="USDT" value="usdt" />
              <el-option label="银行卡" value="bank" />
              <el-option label="易支付" value="easypay" />
              <el-option label="其他" value="other" />
            </el-select>
            <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 100px">
              <el-option label="全部" value="" />
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button type="success" @click="openCreate">新增支付方式</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)" size="small">{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="minAmount" label="最小金额" width="100" align="right" />
        <el-table-column prop="maxAmount" label="最大金额" width="100" align="right" />
        <el-table-column prop="feeRate" label="手续费率" width="100" align="center">
          <template #default="{ row }">{{ row.feeRate ? (row.feeRate * 100).toFixed(2) + '%' : '--' }}</template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button :type="row.status === 1 ? 'warning' : 'success'" link size="small" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑支付方式' : '新增支付方式'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如：USDT-TRC20" />
        </el-form-item>
        <el-form-item label="编码" prop="code">
          <el-input v-model="form.code" placeholder="如：usdt_trc20" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="USDT" value="usdt" />
            <el-option label="银行卡" value="bank" />
            <el-option label="易支付" value="easypay" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="图标URL">
          <el-input v-model="form.icon" placeholder="支付方式图标地址" />
        </el-form-item>
        <el-form-item label="收款账户">
          <el-input v-model="form.payAccount" placeholder="USDT地址/银行卡号/商户号" />
        </el-form-item>
        <el-form-item label="最小金额">
          <el-input-number v-model="form.minAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="最大金额">
          <el-input-number v-model="form.maxAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="手续费率">
          <el-input-number v-model="form.feeRate" :min="0" :max="1" :step="0.001" :precision="4" style="width: 100%" />
          <span style="color: #999; font-size: 12px">0.01 = 1%</span>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="API配置">
          <el-input
            v-model="form.apiConfig"
            type="textarea"
            :rows="4"
            placeholder='易支付配置JSON：{"gatewayUrl":"https://pay.xxx.com","pid":"商户ID","key":"商户密钥","type":"alipay"}'
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/axios'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const filterType = ref('')
const filterStatus = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  code: '',
  type: 'usdt',
  icon: '',
  payAccount: '',
  minAmount: 100,
  maxAmount: 50000,
  feeRate: 0,
  sort: 0,
  status: 1,
  apiConfig: '',
  remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const getTypeLabel = (type) => {
  const map = { usdt: 'USDT', bank: '银行卡', easypay: '易支付', other: '其他' }
  return map[type] || type
}

const getTypeTag = (type) => {
  const map = { usdt: 'success', bank: 'primary', easypay: 'warning', other: 'info' }
  return map[type] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, pageSize: pageSize.value }
    if (filterType.value) params.type = filterType.value
    if (filterStatus.value !== '') params.status = filterStatus.value
    const res = await request({ url: '/admin/payment/list', method: 'get', params })
    tableData.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null, name: '', code: '', type: 'usdt', icon: '', payAccount: '',
    minAmount: 100, maxAmount: 50000, feeRate: 0, sort: 0, status: 1, apiConfig: '', remark: ''
  })
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const url = isEdit.value ? '/admin/payment/update' : '/admin/payment/create'
    await request({ url, method: 'post', data: form })
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row) => {
  try {
    await request({ url: `/admin/payment/toggle/${row.id}`, method: 'post' })
    ElMessage.success('状态已切换')
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除支付方式「${row.name}」？`, '确认删除', {
    type: 'warning'
  }).then(async () => {
    try {
      await request({ url: `/admin/payment/delete/${row.id}`, method: 'post' })
      ElMessage.success('删除成功')
      loadData()
    } catch (e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => loadData())
</script>

<style scoped>
.payment-management { padding: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 8px; align-items: center; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
