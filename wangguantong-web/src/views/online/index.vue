<template>
  <a-card class="page-card" title="上机管理">
    <a-form class="toolbar" :model="startForm" layout="inline">
      <a-form-item label="会员">
        <a-select v-model="startForm.memberId" placeholder="请选择会员" style="width: 220px">
          <a-option v-for="member in members" :key="member.id" :value="member.id">
            {{ member.name }}（{{ member.memberLevel }}，余额：{{ member.balance }}）
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item label="空闲电脑">
        <a-select v-model="startForm.computerId" placeholder="请选择空闲电脑" style="width: 220px">
          <a-option v-for="computer in freeComputers" :key="computer.id" :value="computer.id">
            {{ computer.computerNo }}（{{ computer.area }}，￥{{ computer.pricePerHour }}/小时）
          </a-option>
        </a-select>
      </a-form-item>
      <a-button type="primary" @click="handleStart">上机</a-button>
    </a-form>

    <a-tabs v-model:active-key="activeTab" class="online-tabs" @change="handleTabChange">
      <a-tab-pane key="current" title="当前上机" />
      <a-tab-pane key="history" title="上机历史" />
    </a-tabs>

    <a-form class="toolbar" :model="query" layout="inline">
      <a-form-item label="会员姓名">
        <a-input v-model="query.memberName" allow-clear />
      </a-form-item>
      <a-form-item label="电脑编号">
        <a-input v-model="query.computerNo" allow-clear />
      </a-form-item>
      <a-space>
        <a-button type="primary" @click="loadRecords">查询</a-button>
        <a-button @click="resetQuery">重置</a-button>
      </a-space>
    </a-form>

    <a-table class="no-wrap-table" :columns="columns" :data="records" row-key="id" :pagination="false" :scroll="{ x: 1760 }">
      <template #startTime="{ record }">
        {{ formatDateTime(record.startTime) }}
      </template>
      <template #endTime="{ record }">
        {{ formatDateTime(record.endTime) }}
      </template>
      <template #computerHistory="{ record }">
        <div v-if="hasChangedComputer(record)" class="change-record">
          <a-tag color="orange">已换机</a-tag>
          <span>{{ formatComputerHistory(record.computerHistory) }}</span>
        </div>
        <span v-else>未换机</span>
      </template>
      <template #status="{ record }">
        <a-tag :color="record.status === '进行中' ? 'blue' : 'green'">{{ record.status }}</a-tag>
      </template>
      <template #runningTime="{ record }">
        <span v-if="record.status === '进行中'">{{ formatRunningTime(record.startTime) }}</span>
        <span v-else>-</span>
      </template>
      <template #currentAmount="{ record }">
        <span>{{ record.currentAmount || record.totalAmount || 0 }}</span>
      </template>
      <template #balanceWarning="{ record }">
        <span v-if="record.warningMessage" class="warning-text">余额不足</span>
        <span v-else>正常</span>
      </template>
      <template #actions="{ record }">
        <div v-if="record.status === '进行中'" class="action-buttons">
          <a-button size="small" @click="openChangeComputer(record)">换机</a-button>
          <a-popconfirm content="确认下机结算吗？" @ok="handleEnd(record.id)">
            <a-button size="small" type="primary">下机结算</a-button>
          </a-popconfirm>
        </div>
        <span v-else>已完成</span>
      </template>
    </a-table>

    <a-modal v-model:visible="changeVisible" title="更换电脑" :on-before-ok="submitChangeComputer">
      <a-form :model="changeForm" layout="vertical">
        <a-form-item label="当前电脑"><a-input :model-value="selectedRecord?.computerNo" disabled /></a-form-item>
        <a-form-item label="目标电脑" required>
          <a-select v-model="changeForm.targetComputerId" placeholder="请选择空闲电脑">
            <a-option v-for="computer in freeComputers" :key="computer.id" :value="computer.id">
              {{ computer.computerNo }}（{{ computer.area }}，￥{{ computer.pricePerHour }}/小时，{{ changeFeeText(computer) }}）
            </a-option>
          </a-select>
        </a-form-item>
        <a-alert>{{ selectedChangeFeeText }}</a-alert>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { getComputerList } from '../../api/computer'
import { getMemberList } from '../../api/member'
import { changeOnlineComputer, endOnline, getOnlineList, startOnline } from '../../api/online'
import { formatDateTime } from '../../utils/format'

const members = ref([])
const freeComputers = ref([])
const computers = ref([])
const records = ref([])
const changeVisible = ref(false)
const selectedRecord = ref(null)
const activeTab = ref('current')
const now = ref(Date.now())
let timer = null
let refreshTimer = null

const startForm = reactive({
  memberId: null,
  computerId: null
})

const changeForm = reactive({ targetComputerId: null })

const query = reactive({
  memberName: '',
  computerNo: ''
})

const columns = [
  { title: '记录ID', dataIndex: 'id', width: 90 },
  { title: '会员姓名', dataIndex: 'memberName', width: 100 },
  { title: '电脑编号', dataIndex: 'computerNo', width: 100 },
  { title: '换机记录', slotName: 'computerHistory', width: 250 },
  { title: '开始时间', slotName: 'startTime', width: 180 },
  { title: '下机时间', slotName: 'endTime', width: 180 },
  { title: '实时上机时长', slotName: 'runningTime', width: 160 },
  { title: '计费小时', dataIndex: 'chargeHours', width: 90 },
  { title: '会员级别', dataIndex: 'memberLevel', width: 100 },
  { title: '折扣', dataIndex: 'discountRate', width: 80 },
  { title: '当前消费', slotName: 'currentAmount', width: 100 },
  { title: '会员余额', dataIndex: 'memberBalance', width: 100 },
  { title: '余额提醒', slotName: 'balanceWarning', width: 110 },
  { title: '状态', slotName: 'status', width: 100 },
  { title: '操作', slotName: 'actions', width: 190 }
]

const selectedTargetComputer = computed(() => computers.value.find((item) => item.id === changeForm.targetComputerId))
const selectedCurrentComputer = computed(() => computers.value.find((item) => item.id === selectedRecord.value?.computerId))
const selectedChangeFeeText = computed(() => {
  if (!selectedTargetComputer.value) return '目标机价格更高时只补小时差价，同价或更低时免费换机且不退差价。'
  const extraAmount = getChangeExtraAmount(selectedTargetComputer.value)
  return extraAmount > 0
    ? `本次换机需补差价 ￥${formatMoney(extraAmount)}，确认后从会员余额扣除。`
    : '本次免费换机，不额外扣费；已产生的费用不退还。'
})

async function loadOptions() {
  members.value = await getMemberList({})
  computers.value = await getComputerList({})
  freeComputers.value = computers.value.filter((item) => item.status === '空闲')
}

async function loadRecords() {
  records.value = await getOnlineList({
    memberName: query.memberName,
    computerNo: query.computerNo,
    status: activeTab.value === 'current' ? '进行中' : '已完成'
  })
}

function formatRunningTime(startTime) {
  if (!startTime) return '-'
  const start = new Date(startTime.replace(' ', 'T')).getTime()
  const seconds = Math.max(0, Math.floor((now.value - start) / 1000))
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const remainSeconds = seconds % 60
  return `${hours}小时${minutes}分${remainSeconds}秒`
}

function getDiscountRate(memberLevel) {
  if (memberLevel === '钻石会员') return 0.8
  if (memberLevel === '黄金会员') return 0.9
  return 1
}

function getChangeExtraAmount(targetComputer) {
  const currentPrice = Number(selectedCurrentComputer.value?.pricePerHour || 0)
  const targetPrice = Number(targetComputer?.pricePerHour || 0)
  return Math.max(0, (targetPrice - currentPrice) * getDiscountRate(selectedRecord.value?.memberLevel))
}

function formatMoney(value) {
  const number = Number(value || 0)
  return Number.isInteger(number) ? String(number) : number.toFixed(2)
}

function changeFeeText(targetComputer) {
  const extraAmount = getChangeExtraAmount(targetComputer)
  return extraAmount > 0 ? `补 ￥${formatMoney(extraAmount)}` : '免费换机'
}

function hasChangedComputer(record) {
  return String(record?.computerHistory || '').includes('->')
}

function formatComputerHistory(history) {
  return String(history || '-').replaceAll('->', '→')
}

async function loadAll() {
  await loadOptions()
  await loadRecords()
}

function resetQuery() {
  query.memberName = ''
  query.computerNo = ''
  loadRecords()
}

function handleTabChange() {
  loadRecords()
}

async function handleStart() {
  if (!startForm.memberId || !startForm.computerId) {
    Message.error('请选择会员和空闲电脑')
    return
  }
  await startOnline(startForm)
  Message.success('上机成功')
  startForm.memberId = null
  startForm.computerId = null
  loadAll()
}

async function handleEnd(recordId) {
  await endOnline(recordId)
  Message.success('下机结算成功')
  loadAll()
}

async function openChangeComputer(record) {
  selectedRecord.value = record
  changeForm.targetComputerId = null
  computers.value = await getComputerList({})
  freeComputers.value = computers.value.filter((item) => item.status === '空闲')
  changeVisible.value = true
}

async function submitChangeComputer() {
  if (!changeForm.targetComputerId) {
    Message.error('请选择目标电脑')
    return false
  }
  const result = await changeOnlineComputer({
    recordId: selectedRecord.value.id,
    targetComputerId: changeForm.targetComputerId
  })
  Message.success(result?.message || '换机成功')
  await loadAll()
  return true
}

onMounted(() => {
  loadAll()
  timer = window.setInterval(() => {
    now.value = Date.now()
  }, 1000)
  refreshTimer = window.setInterval(() => {
    loadAll()
  }, 30000)
})

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
  if (refreshTimer) window.clearInterval(refreshTimer)
})
</script>

<style scoped>
.change-record {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}
</style>
