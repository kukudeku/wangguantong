<template>
  <a-card class="page-card" title="首页数据看板">
    <!-- v-for 根据 statItems 数组生成六张统计卡片。 -->
    <div class="stat-grid">
      <a-card class="stat-card" v-for="item in statItems" :key="item.label">
        <div class="stat-label">{{ item.label }}</div>
        <div class="stat-value">{{ item.value }}</div>
      </a-card>
    </div>

    <a-divider />

    <!-- 座位图标题与状态颜色图例。 -->
    <div class="seat-map-header">
      <h3>电脑座位图</h3>
      <div class="seat-legend">
        <span class="legend-item"><i class="seat-dot free"></i>空闲</span>
        <span class="legend-item"><i class="seat-dot using"></i>使用中</span>
        <span class="legend-item"><i class="seat-dot reserved"></i>预约锁定</span>
        <span class="legend-item"><i class="seat-dot repair"></i>维修中</span>
      </div>
    </div>

    <!-- 先按区域循环，再在区域内部循环每台电脑。 -->
    <div v-if="areaGroups.length" class="seat-area-list">
      <section v-for="area in areaGroups" :key="area.name" class="seat-area">
        <div class="seat-area-title">
          <span>{{ area.name }}</span>
          <small>{{ area.computers.length }} 台</small>
        </div>
        <div class="seat-grid">
          <div
            v-for="computer in area.computers"
            :key="computer.id"
            class="seat-card"
            :class="seatClass(computer.status)"
          >
            <div class="seat-no">{{ computer.computerNo }}</div>
            <div class="seat-status">{{ computer.status }}</div>
            <div class="seat-meta">￥{{ computer.pricePerHour }}/小时</div>
          </div>
        </div>
      </section>
    </div>
    <a-empty v-else description="暂无电脑数据" />
  </a-card>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getStatistics } from '../../api/dashboard'
import { getComputerList } from '../../api/computer'

// 后端统计接口返回前先准备默认值，避免请求期间模板显示 undefined。
const statistics = ref({
  memberCount: 0,
  computerCount: 0,
  freeComputerCount: 0,
  usingComputerCount: 0,
  todayRechargeAmount: 0,
  todayOnlineCount: 0
})
const computers = ref([])

// computed 会在 statistics 更新后自动重算卡片数组。
const statItems = computed(() => [
  { label: '会员总数', value: statistics.value.memberCount },
  { label: '电脑总数', value: statistics.value.computerCount },
  { label: '空闲电脑', value: statistics.value.freeComputerCount },
  { label: '使用中电脑', value: statistics.value.usingComputerCount },
  { label: '今日充值金额', value: `￥${statistics.value.todayRechargeAmount || 0}` },
  { label: '今日上机次数', value: statistics.value.todayOnlineCount }
])

const areaGroups = computed(() => {
  // Map 的 key 是区域名称，value 是该区域的电脑数组。
  const map = new Map()
  computers.value.forEach((computer) => {
    const area = computer.area || '未分区'
    if (!map.has(area)) {
      map.set(area, [])
    }
    map.get(area).push(computer)
  })
  // 把 Map 转换成模板容易循环的数组，并按电脑编号排序。
  return Array.from(map.entries()).map(([name, list]) => ({
    name,
    computers: list.sort((a, b) => String(a.computerNo).localeCompare(String(b.computerNo)))
  }))
})

function seatClass(status) {
  // 返回对应 CSS 类，决定机位卡片的边框、背景和文字颜色。
  if (status === '空闲') return 'seat-free'
  if (status === '使用中') return 'seat-using'
  if (status === '预约锁定') return 'seat-reserved'
  return 'seat-repair'
}

onMounted(async () => {
  // 页面加载时并行概念上需要两组数据：顶部统计和完整电脑列表。
  statistics.value = await getStatistics()
  computers.value = await getComputerList({})
})
</script>

<style scoped>
/* 以下样式只服务于首页座位图，按“标题 -> 图例 -> 区域 -> 机位卡片”分组。 */
.seat-map-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.seat-map-header h3 {
  margin: 0;
  font-size: 18px;
}

.seat-legend {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  color: #4e5969;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.seat-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.seat-dot.free {
  background: #00b42a;
}

.seat-dot.using {
  background: #165dff;
}

.seat-dot.reserved {
  background: #722ed1;
}

.seat-dot.repair {
  background: #ff7d00;
}

.seat-area-list {
  display: grid;
  gap: 18px;
}

.seat-area {
  padding: 16px;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  background: #fbfcff;
}

.seat-area-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 600;
}

.seat-area-title small {
  color: #86909c;
  font-weight: 400;
}

.seat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(112px, 1fr));
  gap: 12px;
}

.seat-card {
  min-height: 84px;
  padding: 12px;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
}

.seat-no {
  font-size: 18px;
  font-weight: 700;
}

.seat-status {
  font-size: 14px;
  font-weight: 600;
}

.seat-meta {
  color: #86909c;
  font-size: 12px;
}

.seat-free {
  border-color: #7be188;
  background: #f0fff3;
}

.seat-free .seat-status {
  color: #00b42a;
}

.seat-using {
  border-color: #94bfff;
  background: #f2f7ff;
}

.seat-using .seat-status {
  color: #165dff;
}

.seat-reserved {
  border-color: #c396ed;
  background: #fbf5ff;
}

.seat-reserved .seat-status {
  color: #722ed1;
}

.seat-repair {
  border-color: #ffcf8b;
  background: #fff7e8;
}

.seat-repair .seat-status {
  color: #ff7d00;
}
</style>
