<template>
  <div class="game-management">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>游戏管理</span>
          <el-radio-group v-model="gameCategory" @change="loadData">
            <el-radio-button label="lottery">彩票游戏</el-radio-button>
            <el-radio-button label="casino">娱乐城</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 彩票游戏 -->
      <template v-if="gameCategory === 'lottery'">
        <el-table :data="lotteryGames" v-loading="loading" stripe style="width: 100%">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="id" label="ID" width="80" align="center" />
          <el-table-column prop="name" label="游戏名称" min-width="140" />
          <el-table-column prop="code" label="游戏代码" width="120" />
          <el-table-column prop="categoryCode" label="分类" width="120" />
          <el-table-column label="当前期号" width="140" align="center">
            <template #default="{ row }">{{ drawInfo[row.code]?.currentPeriod || '--' }}</template>
          </el-table-column>
          <el-table-column label="上期开奖" min-width="140" align="center">
            <template #default="{ row }">
              <div class="draw-numbers">
                <span v-for="(n, i) in getNumbers(row.code)" :key="i" class="ball">{{ n }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="封盘倒计时" width="120" align="center">
            <template #default="{ row }">
              <el-tag v-if="drawInfo[row.code]?.closeCountdown" type="warning" size="small">
                {{ drawInfo[row.code].closeCountdown }}s
              </el-tag>
              <span v-else>--</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template #default>
              <el-button type="primary" link size="small">配置</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <!-- 娱乐城游戏 -->
      <template v-else>
        <el-table :data="casinoGames" v-loading="loading" stripe style="width: 100%">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="id" label="ID" width="80" align="center" />
          <el-table-column prop="name" label="游戏名称" min-width="140" />
          <el-table-column prop="gameCode" label="游戏代码" width="140" />
          <el-table-column prop="venueName" label="所属平台" width="120" />
          <el-table-column prop="providerName" label="供应商" width="120" />
          <el-table-column label="操作" width="120" align="center">
            <template #default>
              <el-button type="primary" link size="small">配置</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="casinoPage"
            v-model:page-size="casinoPageSize"
            :total="casinoTotal"
            layout="total, prev, pager, next"
            @current-change="loadCasinoGames"
          />
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import * as adminApi from "@/api/admin";

const loading = ref(false);
const gameCategory = ref("lottery");
const lotteryGames = ref<any[]>([]);
const drawInfo = ref<Record<string, any>>({});
const casinoGames = ref<any[]>([]);
const casinoPage = ref(1);
const casinoPageSize = ref(20);
const casinoTotal = ref(0);

const getNumbers = (code: string) => {
  const nums = drawInfo.value[code]?.lastNumbers;
  if (!nums) return [];
  return typeof nums === "string" ? nums.split(",") : nums;
};

const loadLotteryGames = async () => {
  loading.value = true;
  try {
    const res: any = await adminApi.getLotteryList();
    if (res.code === 200) {
      lotteryGames.value = res.data || [];
      // 加载开奖数据
      const codes = lotteryGames.value.slice(0, 6).map((g: any) => g.code).join(",");
      if (codes) {
        const drawRes: any = await adminApi.getDrawList(codes);
        if (drawRes.code === 200) {
          drawInfo.value = drawRes.data || {};
        }
      }
    }
  } catch (e) {
    ElMessage.error("加载彩票游戏失败");
  } finally {
    loading.value = false;
  }
};

const loadCasinoGames = async () => {
  loading.value = true;
  try {
    const res: any = await adminApi.getCasinoGameList({
      page: casinoPage.value,
      pageSize: casinoPageSize.value
    });
    if (res.code === 200) {
      const list = res.data?.list || res.data?.records || [];
      casinoGames.value = list.map((item: any) => ({
        ...item,
        venueName: item.venue?.name || item.venueName,
        providerName: item.provider?.name || item.providerName
      }));
      casinoTotal.value = res.data?.total || list.length;
    }
  } catch (e) {
    ElMessage.error("加载娱乐城游戏失败");
  } finally {
    loading.value = false;
  }
};

const loadData = () => {
  if (gameCategory.value === "lottery") {
    loadLotteryGames();
  } else {
    loadCasinoGames();
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.game-management {
  padding: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}
.draw-numbers {
  display: flex;
  gap: 4px;
  justify-content: center;
}
.ball {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #e6a23c;
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
