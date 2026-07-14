<template>
  <a-card class="page-card" title="服务工单">
    <div class="service-summary">
      <div><span>待处理</span><strong>{{ countByStatus('待处理') }}</strong></div>
      <div><span>处理中</span><strong>{{ countByStatus('处理中') }}</strong></div>
      <div><span>已完成</span><strong>{{ countByStatus('已完成') }}</strong></div>
      <div><span>全部工单</span><strong>{{ allRecords.length }}</strong></div>
    </div>

    <a-form class="toolbar" :model="query" layout="inline">
      <a-form-item label="服务类型">
        <a-select v-model="query.serviceType" allow-clear placeholder="全部类型" style="width: 140px">
          <a-option value="呼叫网管">呼叫网管</a-option>
          <a-option value="故障报修">故障报修</a-option>
        </a-select>
      </a-form-item>
      <a-form-item label="电脑编号">
        <a-input v-model="query.computerNo" allow-clear placeholder="例如 A001" />
      </a-form-item>
      <a-form-item label="处理状态">
        <a-select v-model="query.status" allow-clear placeholder="全部状态" style="width: 140px">
          <a-option value="待处理">待处理</a-option>
          <a-option value="处理中">处理中</a-option>
          <a-option value="已完成">已完成</a-option>
          <a-option value="已取消">已取消</a-option>
        </a-select>
      </a-form-item>
      <a-space>
        <a-button type="primary" @click="loadRecords">查询</a-button>
        <a-button @click="resetQuery">重置</a-button>
      </a-space>
    </a-form>

    <a-table
      class="no-wrap-table"
      :columns="columns"
      :data="records"
      row-key="id"
      :pagination="false"
      :scroll="{ x: 1500 }"
    >
      <template #serviceType="{ record }">
        <a-tag :color="record.serviceType === '呼叫网管' ? 'arcoblue' : 'orange'">
          {{ record.serviceType || '故障报修' }}
        </a-tag>
      </template>
      <template #serviceLocation="{ record }">
        {{ record.serviceLocation || record.computerNo || '-' }}
      </template>
      <template #status="{ record }">
        <a-tag :color="statusColor(record.status)">{{ record.status }}</a-tag>
      </template>
      <template #createTime="{ record }">{{ formatDateTime(record.createTime) }}</template>
      <template #finishTime="{ record }">{{ formatDateTime(record.finishTime) }}</template>
      <template #actions="{ record }">
        <div class="action-buttons">
          <a-button v-if="record.status === '待处理'" size="small" type="primary" @click="openProcess(record, '处理中')">受理</a-button>
          <a-button v-if="record.status === '处理中'" size="small" type="primary" @click="openProcess(record, '已完成')">完成</a-button>
          <a-button v-if="record.status === '待处理' || record.status === '处理中'" size="small" status="danger" @click="openProcess(record, '已取消')">取消</a-button>
          <span v-else>已结束</span>
        </div>
      </template>
    </a-table>

    <a-modal v-model:visible="processVisible" :title="processTitle" :on-before-ok="submitProcess">
      <a-form :model="processForm" layout="vertical">
        <a-form-item label="服务类型"><a-input :model-value="selectedRecord?.serviceType || '故障报修'" disabled /></a-form-item>
        <a-form-item label="用户位置"><a-input :model-value="selectedRecord?.serviceLocation || selectedRecord?.computerNo || '-'" disabled /></a-form-item>
        <a-form-item label="服务内容"><a-textarea :model-value="selectedRecord?.problemDescription" disabled /></a-form-item>
        <a-form-item label="处理说明">
          <a-textarea v-model="processForm.processRemark" :max-length="500" show-word-limit placeholder="填写本次处理结果" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { getRepairList, processRepair } from '../../api/repair'
import { formatDateTime } from '../../utils/format'

const query = reactive({ serviceType: '', computerNo: '', status: '' })
const records = ref([])
const allRecords = ref([])
const processVisible = ref(false)
const selectedRecord = ref(null)
const processForm = reactive({ status: '', processRemark: '' })

const columns = [
  { title: '工单编号', dataIndex: 'id', width: 100 },
  { title: '服务类型', slotName: 'serviceType', width: 120 },
  { title: '用户', dataIndex: 'memberName', width: 110 },
  { title: '电脑编号', dataIndex: 'computerNo', width: 110 },
  { title: '所在位置', slotName: 'serviceLocation', width: 140 },
  { title: '服务内容', dataIndex: 'problemDescription', width: 280 },
  { title: '状态', slotName: 'status', width: 100 },
  { title: '处理说明', dataIndex: 'processRemark', width: 220 },
  { title: '提交时间', slotName: 'createTime', width: 180 },
  { title: '结束时间', slotName: 'finishTime', width: 180 },
  { title: '操作', slotName: 'actions', width: 180, fixed: 'right' }
]

const processTitle = computed(() => {
  if (processForm.status === '处理中') return '受理服务工单'
  if (processForm.status === '已完成') return '完成服务工单'
  return '取消服务工单'
})

function statusColor(status) {
  if (status === '待处理') return 'orange'
  if (status === '处理中') return 'blue'
  if (status === '已完成') return 'green'
  return 'gray'
}

function countByStatus(status) {
  return allRecords.value.filter((item) => item.status === status).length
}

async function loadRecords() {
  const [list, all] = await Promise.all([
    getRepairList(query),
    getRepairList({})
  ])
  records.value = list
  allRecords.value = all
}

function resetQuery() {
  Object.assign(query, { serviceType: '', computerNo: '', status: '' })
  loadRecords()
}

function openProcess(record, status) {
  selectedRecord.value = record
  processForm.status = status
  processForm.processRemark = record.processRemark || ''
  processVisible.value = true
}

async function submitProcess() {
  await processRepair({
    id: selectedRecord.value.id,
    status: processForm.status,
    processRemark: processForm.processRemark
  })
  Message.success('工单状态已更新')
  processVisible.value = false
  await loadRecords()
  return true
}

onMounted(loadRecords)
</script>

<style scoped>
.service-summary {
  margin-bottom: 18px;
  border: 1px solid #e5e6eb;
  border-radius: 4px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.service-summary > div {
  min-height: 76px;
  padding: 15px 20px;
  border-right: 1px solid #e5e6eb;
}

.service-summary > div:last-child {
  border-right: 0;
}

.service-summary span,
.service-summary strong {
  display: block;
}

.service-summary span {
  color: #86909c;
  font-size: 13px;
}

.service-summary strong {
  margin-top: 5px;
  color: #1d2129;
  font-size: 24px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  white-space: nowrap;
}

@media (max-width: 720px) {
  .service-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .service-summary > div:nth-child(2) {
    border-right: 0;
  }

  .service-summary > div:nth-child(-n + 2) {
    border-bottom: 1px solid #e5e6eb;
  }
}
</style>
