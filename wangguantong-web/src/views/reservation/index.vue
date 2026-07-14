<template>
  <a-card class="page-card" title="预约锁定电脑">
    <a-form class="toolbar" :model="form" layout="inline">
      <a-form-item label="会员">
        <a-select v-model="form.memberId" placeholder="请选择会员" style="width: 220px">
          <a-option v-for="member in members" :key="member.id" :value="member.id">
            {{ member.name }}（{{ member.userType }}，{{ member.memberLevel }}）
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item label="空闲电脑">
        <a-select v-model="form.computerId" placeholder="请选择空闲电脑" style="width: 220px">
          <a-option v-for="computer in freeComputers" :key="computer.id" :value="computer.id">
            {{ computer.computerNo }}（{{ computer.area }}）
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item label="预约时间">
        <a-date-picker v-model="form.reserveTime" show-time format="YYYY-MM-DD HH:mm:ss" style="width: 220px" />
        <div class="form-tip">只能预约当前时间起 1 小时内，超时未上机自动取消。</div>
      </a-form-item>
      <a-button type="primary" @click="submitReservation">预约锁定</a-button>
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
          <a-option value="已预约">已预约</a-option>
          <a-option value="已取消">已取消</a-option>
          <a-option value="已上机">已上机</a-option>
        </a-select>
      </a-form-item>
      <a-space>
        <a-button type="primary" @click="loadReservations">查询</a-button>
        <a-button @click="resetQuery">重置</a-button>
      </a-space>
    </a-form>

    <a-table class="no-wrap-table" :columns="columns" :data="records" row-key="id" :pagination="false" :scroll="{ x: 1080 }">
      <template #reserveTime="{ record }">
        {{ formatDateTime(record.reserveTime) }}
      </template>
      <template #status="{ record }">
        <a-tag :color="statusColor(record.status)">{{ record.status }}</a-tag>
      </template>
      <template #createTime="{ record }">
        {{ formatDateTime(record.createTime) }}
      </template>
      <template #actions="{ record }">
        <div class="action-buttons" v-if="record.status === '已预约'">
          <a-popconfirm content="确认取消预约吗？" @ok="handleCancel(record.id)">
            <a-button size="small" status="danger">取消预约</a-button>
          </a-popconfirm>
          <a-popconfirm content="确认让该预约开始上机吗？" @ok="handleStart(record.id)">
            <a-button size="small" type="primary">预约上机</a-button>
          </a-popconfirm>
        </div>
        <span v-else>{{ record.status }}</span>
      </template>
    </a-table>
  </a-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { getFreeComputers } from '../../api/computer'
import { getMemberList } from '../../api/member'
import { addReservation, cancelReservation, getReservationList, startReservation } from '../../api/reservation'
import { formatDateTime } from '../../utils/format'

const members = ref([])
const freeComputers = ref([])
const records = ref([])
const form = reactive({
  memberId: null,
  computerId: null,
  reserveTime: ''
})
const query = reactive({
  memberName: '',
  computerNo: '',
  status: ''
})

const columns = [
  { title: '预约ID', dataIndex: 'id', width: 90 },
  { title: '会员姓名', dataIndex: 'memberName', width: 120 },
  { title: '电脑编号', dataIndex: 'computerNo', width: 120 },
  { title: '预约时间', slotName: 'reserveTime', width: 190 },
  { title: '状态', slotName: 'status', width: 100 },
  { title: '创建时间', slotName: 'createTime', width: 190 },
  { title: '操作', slotName: 'actions', width: 190 }
]

function statusColor(status) {
  if (status === '已预约') return 'purple'
  if (status === '已上机') return 'green'
  return 'gray'
}

async function loadOptions() {
  members.value = await getMemberList({})
  freeComputers.value = await getFreeComputers()
}

async function loadReservations() {
  records.value = await getReservationList(query)
}

async function loadAll() {
  await loadOptions()
  await loadReservations()
}

function resetQuery() {
  query.memberName = ''
  query.computerNo = ''
  query.status = ''
  loadReservations()
}

async function submitReservation() {
  if (!form.memberId || !form.computerId) {
    Message.error('请选择会员和空闲电脑')
    return
  }
  if (!validateReservationTime(form.reserveTime)) return
  await addReservation(form)
  Message.success('预约锁定成功')
  form.memberId = null
  form.computerId = null
  form.reserveTime = ''
  loadAll()
}

function validateReservationTime(value) {
  if (!value) {
    Message.error('请选择预约时间')
    return false
  }
  const reserveTime = new Date(String(value).replace(' ', 'T')).getTime()
  const nowTime = Date.now()
  if (Number.isNaN(reserveTime)) {
    Message.error('预约时间格式不正确')
    return false
  }
  if (reserveTime < nowTime) {
    Message.error('预约时间不能早于当前时间')
    return false
  }
  if (reserveTime > nowTime + 60 * 60 * 1000) {
    Message.error('只能预约最近 1 小时')
    return false
  }
  return true
}

async function handleCancel(id) {
  await cancelReservation(id)
  Message.success('取消成功')
  loadAll()
}

async function handleStart(id) {
  await startReservation(id)
  Message.success('预约上机成功')
  loadAll()
}

onMounted(loadAll)
</script>

<style scoped>
.form-tip {
  margin-top: 6px;
  color: #86909c;
  font-size: 12px;
}
</style>
