<template>
  <a-card class="page-card" title="首页数据看板">
    <div class="stat-grid">
      <div class="stat-card" v-for="item in statItems" :key="item.label">
        <div class="stat-label">{{ item.label }}</div>
        <div class="stat-value">{{ item.value }}</div>
      </div>
    </div>

    <a-divider />

    <div class="seat-map-header">
      <h3>电脑座位图</h3>
      <div class="seat-legend">
        <span class="legend-item"><i class="seat-dot free"></i>空闲</span>
        <span class="legend-item"><i class="seat-dot using"></i>使用中</span>
        <span class="legend-item"><i class="seat-dot reserved"></i>预约锁定</span>
        <span class="legend-item"><i class="seat-dot repair"></i>维修中</span>
      </div>
    </div>

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

const statistics = ref({
  memberCount: 0,
  computerCount: 0,
  freeComputerCount: 0,
  usingComputerCount: 0,
  todayRechargeAmount: 0,
  todayOnlineCount: 0
})
const computers = ref([])

const statItems = computed(() => [
  { label: '会员总数', value: statistics.value.memberCount },
  { label: '电脑总数', value: statistics.value.computerCount },
  { label: '空闲电脑', value: statistics.value.freeComputerCount },
  { label: '使用中电脑', value: statistics.value.usingComputerCount },
  { label: '今日充值金额', value: `￥${statistics.value.todayRechargeAmount || 0}` },
  { label: '今日上机次数', value: statistics.value.todayOnlineCount }
])

const areaGroups = computed(() => {
  const map = new Map()
  computers.value.forEach((computer) => {
    const area = computer.area || '未分区'
    if (!map.has(area)) {
      map.set(area, [])
    }
    map.get(area).push(computer)
  })
  return Array.from(map.entries()).map(([name, list]) => ({
    name,
    computers: list.sort((a, b) => String(a.computerNo).localeCompare(String(b.computerNo)))
  }))
})

function seatClass(status) {
  if (status === '空闲') return 'seat-free'
  if (status === '使用中') return 'seat-using'
  if (status === '预约锁定') return 'seat-reserved'
  return 'seat-repair'
}

onMounted(async () => {
  statistics.value = await getStatistics()
  computers.value = await getComputerList({})
})
</script>

<style scoped>
.seat-map-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.seat-map-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
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
  padding: 2px 0 18px;
  border-bottom: 1px solid #f2f3f5;
}

.seat-area:last-child {
  padding-bottom: 0;
  border-bottom: 0;
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
  border-radius: 4px;
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
