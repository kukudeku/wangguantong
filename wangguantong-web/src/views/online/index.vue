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

    <a-form class="toolbar" :model="query" layout="inline">
      <a-form-item label="会员姓名">
        <a-input v-model="query.memberName" allow-clear />
      </a-form-item>
      <a-form-item label="电脑编号">
        <a-input v-model="query.computerNo" allow-clear />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model="query.status" allow-clear style="width: 140px">
          <a-option value="进行中">进行中</a-option>
          <a-option value="已完成">已完成</a-option>
        </a-select>
      </a-form-item>
      <a-space>
        <a-button type="primary" @click="loadRecords">查询</a-button>
        <a-button @click="resetQuery">重置</a-button>
      </a-space>
    </a-form>

    <a-table class="no-wrap-table" :columns="columns" :data="records" row-key="id" :pagination="false" :scroll="{ x: 1680 }">
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
        <span v-if="record.warningMessage" class="warning-text">{{ record.warningMessage }}</span>
        <span v-else>正常</span>
      </template>
      <template #actions="{ record }">
        <a-popconfirm v-if="record.status === '进行中'" content="确认下机结算吗？" @ok="handleEnd(record.id)">
          <a-button size="small" type="primary">下机结算</a-button>
        </a-popconfirm>
        <span v-else>已完成</span>
      </template>
    </a-table>
  </a-card>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { getFreeComputers } from '../../api/computer'
import { getMemberList } from '../../api/member'
import { endOnline, getOnlineList, startOnline } from '../../api/online'

const members = ref([])
const freeComputers = ref([])
const records = ref([])
const now = ref(Date.now())
let timer = null
let refreshTimer = null

const startForm = reactive({
  memberId: null,
  computerId: null
})

const query = reactive({
  memberName: '',
  computerNo: '',
  status: ''
})

const columns = [
  { title: '记录ID', dataIndex: 'id', width: 90 },
  { title: '会员姓名', dataIndex: 'memberName', width: 100 },
  { title: '电脑编号', dataIndex: 'computerNo', width: 100 },
  { title: '开始时间', dataIndex: 'startTime', width: 180 },
  { title: '下机时间', dataIndex: 'endTime', width: 180 },
  { title: '实时上机时长', slotName: 'runningTime', width: 160 },
  { title: '计费小时', dataIndex: 'chargeHours', width: 90 },
  { title: '会员级别', dataIndex: 'memberLevel', width: 100 },
  { title: '折扣', dataIndex: 'discountRate', width: 80 },
  { title: '当前消费', slotName: 'currentAmount', width: 100 },
  { title: '会员余额', dataIndex: 'memberBalance', width: 100 },
  { title: '余额提醒', slotName: 'balanceWarning', width: 180 },
  { title: '状态', slotName: 'status', width: 100 },
  { title: '操作', slotName: 'actions', width: 120 }
]

async function loadOptions() {
  members.value = await getMemberList({})
  freeComputers.value = await getFreeComputers()
}

async function loadRecords() {
  records.value = await getOnlineList(query)
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

async function loadAll() {
  await loadOptions()
  await loadRecords()
}

function resetQuery() {
  query.memberName = ''
  query.computerNo = ''
  query.status = ''
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

onMounted(() => {
  loadAll()
  timer = window.setInterval(() => {
    now.value = Date.now()
  }, 1000)
  refreshTimer = window.setInterval(() => {
    loadRecords()
  }, 30000)
})

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
  if (refreshTimer) window.clearInterval(refreshTimer)
})
</script>
