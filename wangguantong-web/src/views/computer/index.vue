<template>
  <a-card class="page-card" :title="activeTab === 'repair' ? '报修管理' : '电脑机位管理'">
    <a-tabs v-model:active-key="activeTab" @change="changeTab">
      <a-tab-pane key="computer" title="电脑列表">
        <a-form class="toolbar" :model="query" layout="inline">
          <a-form-item label="区域">
            <a-input v-model="query.area" placeholder="例如 电竞一区" allow-clear />
          </a-form-item>
          <a-form-item label="状态">
            <a-select v-model="query.status" placeholder="请选择状态" allow-clear style="width: 140px">
              <a-option value="空闲">空闲</a-option>
              <a-option value="使用中">使用中</a-option>
              <a-option value="预约锁定">预约锁定</a-option>
              <a-option value="维修中">维修中</a-option>
            </a-select>
          </a-form-item>
          <a-space>
            <a-button type="primary" @click="loadList">查询</a-button>
            <a-button @click="resetQuery">重置</a-button>
            <a-button type="primary" @click="openAdd">新增电脑</a-button>
          </a-space>
        </a-form>

        <a-table class="no-wrap-table" :columns="columns" :data="tableData" row-key="id" :pagination="false" :scroll="{ x: 920 }">
          <template #status="{ record }">
            <a-tag :color="statusColor(record.status)">{{ record.status }}</a-tag>
          </template>
          <template #actions="{ record }">
            <div class="action-buttons">
              <a-button size="small" @click="openEdit(record)">编辑</a-button>
              <a-button size="small" status="warning" @click="openRepairReport(record)">报修</a-button>
              <a-popconfirm content="确认删除该电脑吗？" @ok="handleDelete(record.id)">
                <a-button size="small" status="danger">删除</a-button>
              </a-popconfirm>
            </div>
          </template>
        </a-table>
      </a-tab-pane>

      <a-tab-pane key="repair" title="报修管理">
        <a-form class="toolbar" :model="repairQuery" layout="inline">
          <a-form-item label="电脑编号">
            <a-input v-model="repairQuery.computerNo" allow-clear />
          </a-form-item>
          <a-form-item label="处理状态">
            <a-select v-model="repairQuery.status" allow-clear placeholder="全部状态" style="width: 140px">
              <a-option value="待处理">待处理</a-option>
              <a-option value="处理中">处理中</a-option>
              <a-option value="已完成">已完成</a-option>
              <a-option value="已取消">已取消</a-option>
            </a-select>
          </a-form-item>
          <a-space>
            <a-button type="primary" @click="loadRepairs">查询</a-button>
            <a-button @click="resetRepairQuery">重置</a-button>
          </a-space>
        </a-form>

        <a-table class="no-wrap-table" :columns="repairColumns" :data="repairRecords" row-key="id" :pagination="false" :scroll="{ x: 1420 }">
          <template #status="{ record }">
            <a-tag :color="repairStatusColor(record.status)">{{ record.status }}</a-tag>
          </template>
          <template #createTime="{ record }">{{ formatDateTime(record.createTime) }}</template>
          <template #processTime="{ record }">{{ formatDateTime(record.processTime) }}</template>
          <template #finishTime="{ record }">{{ formatDateTime(record.finishTime) }}</template>
          <template #actions="{ record }">
            <div class="action-buttons">
              <a-button v-if="record.status === '待处理'" size="small" type="primary" @click="openProcess(record, '处理中')">开始维修</a-button>
              <a-button v-if="record.status === '处理中'" size="small" type="primary" @click="openProcess(record, '已完成')">完成维修</a-button>
              <a-button v-if="record.status === '待处理' || record.status === '处理中'" size="small" status="danger" @click="openProcess(record, '已取消')">取消报修</a-button>
              <span v-if="record.status === '已完成' || record.status === '已取消'">已结束</span>
            </div>
          </template>
        </a-table>
      </a-tab-pane>
    </a-tabs>

    <a-modal v-model:visible="formVisible" :title="form.id ? '编辑电脑' : '新增电脑'" @ok="saveComputer">
      <a-form :model="form" layout="vertical">
        <a-form-item label="电脑编号"><a-input v-model="form.computerNo" placeholder="例如 A001" /></a-form-item>
        <a-form-item label="所在区域"><a-input v-model="form.area" placeholder="例如 电竞一区" /></a-form-item>
        <a-form-item label="每小时单价"><a-input-number v-model="form.pricePerHour" :min="1" /></a-form-item>
        <a-form-item label="状态">
          <a-select v-model="form.status">
            <a-option value="空闲">空闲</a-option>
            <a-option value="使用中">使用中</a-option>
            <a-option value="预约锁定">预约锁定</a-option>
            <a-option value="维修中">维修中</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注"><a-input v-model="form.remark" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:visible="reportVisible" title="提交机器报修" :on-before-ok="submitRepairReport">
      <a-form :model="reportForm" layout="vertical">
        <a-form-item label="电脑编号"><a-input :model-value="selectedComputer?.computerNo" disabled /></a-form-item>
        <a-form-item label="故障说明" required>
          <a-textarea v-model="reportForm.problemDescription" :max-length="500" show-word-limit placeholder="请描述故障现象" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:visible="processVisible" :title="processTitle" :on-before-ok="submitProcessRepair">
      <a-form :model="processForm" layout="vertical">
        <a-form-item label="电脑编号"><a-input :model-value="selectedRepair?.computerNo" disabled /></a-form-item>
        <a-form-item label="故障说明"><a-textarea :model-value="selectedRepair?.problemDescription" disabled /></a-form-item>
        <a-form-item label="处理说明">
          <a-textarea v-model="processForm.processRemark" :max-length="500" show-word-limit placeholder="填写维修或取消说明" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { addComputer, deleteComputer, getComputerList, updateComputer } from '../../api/computer'
import { getRepairList, processRepair, reportRepair } from '../../api/repair'
import { formatDateTime } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const activeTab = ref(route.query.tab === 'repair' ? 'repair' : 'computer')
const query = reactive({ area: '', status: '' })
const repairQuery = reactive({ computerNo: '', status: '' })
const tableData = ref([])
const repairRecords = ref([])
const formVisible = ref(false)
const reportVisible = ref(false)
const processVisible = ref(false)
const selectedComputer = ref(null)
const selectedRepair = ref(null)
const form = reactive({ id: null, computerNo: '', area: '', pricePerHour: 6, status: '空闲', remark: '' })
const reportForm = reactive({ problemDescription: '' })
const processForm = reactive({ status: '', processRemark: '' })

const columns = [
  { title: '电脑ID', dataIndex: 'id', width: 90 },
  { title: '电脑编号', dataIndex: 'computerNo', width: 120 },
  { title: '区域', dataIndex: 'area', width: 150 },
  { title: '每小时单价', dataIndex: 'pricePerHour', width: 130 },
  { title: '状态', slotName: 'status', width: 110 },
  { title: '备注', dataIndex: 'remark', width: 220 },
  { title: '操作', slotName: 'actions', width: 220 }
]

const repairColumns = [
  { title: '记录ID', dataIndex: 'id', width: 90 },
  { title: '电脑编号', dataIndex: 'computerNo', width: 110 },
  { title: '报修人', dataIndex: 'memberName', width: 110 },
  { title: '故障说明', dataIndex: 'problemDescription', width: 260 },
  { title: '状态', slotName: 'status', width: 100 },
  { title: '处理说明', dataIndex: 'processRemark', width: 220 },
  { title: '报修时间', slotName: 'createTime', width: 180 },
  { title: '处理时间', slotName: 'processTime', width: 180 },
  { title: '完成时间', slotName: 'finishTime', width: 180 },
  { title: '操作', slotName: 'actions', width: 220 }
]

const processTitle = computed(() => {
  if (processForm.status === '处理中') return '开始维修'
  if (processForm.status === '已完成') return '完成维修'
  return '取消报修'
})

function statusColor(status) {
  if (status === '空闲') return 'green'
  if (status === '使用中') return 'blue'
  if (status === '预约锁定') return 'purple'
  return 'orange'
}

function repairStatusColor(status) {
  if (status === '待处理') return 'orange'
  if (status === '处理中') return 'blue'
  if (status === '已完成') return 'green'
  return 'gray'
}

async function loadList() {
  tableData.value = await getComputerList(query)
}

async function loadRepairs() {
  repairRecords.value = await getRepairList(repairQuery)
}

function resetQuery() {
  query.area = ''
  query.status = ''
  loadList()
}

function resetRepairQuery() {
  repairQuery.computerNo = ''
  repairQuery.status = ''
  loadRepairs()
}

function changeTab(tab) {
  if (route.query.tab !== tab) router.replace({ path: route.path, query: { ...route.query, tab } })
}

function openAdd() {
  Object.assign(form, { id: null, computerNo: '', area: '', pricePerHour: 6, status: '空闲', remark: '' })
  formVisible.value = true
}

function openEdit(record) {
  Object.assign(form, record)
  formVisible.value = true
}

async function saveComputer() {
  if (form.id) await updateComputer(form)
  else await addComputer(form)
  Message.success('保存成功')
  formVisible.value = false
  loadList()
}

async function handleDelete(id) {
  await deleteComputer(id)
  Message.success('删除成功')
  loadList()
}

function openRepairReport(record) {
  selectedComputer.value = record
  reportForm.problemDescription = ''
  reportVisible.value = true
}

async function submitRepairReport() {
  if (!reportForm.problemDescription.trim()) {
    Message.error('请填写故障说明')
    return false
  }
  await reportRepair({ computerId: selectedComputer.value.id, problemDescription: reportForm.problemDescription })
  Message.success('报修记录已提交')
  reportVisible.value = false
  changeTab('repair')
  await loadRepairs()
  return true
}

function openProcess(record, status) {
  selectedRepair.value = record
  processForm.status = status
  processForm.processRemark = record.processRemark || ''
  processVisible.value = true
}

async function submitProcessRepair() {
  await processRepair({ id: selectedRepair.value.id, status: processForm.status, processRemark: processForm.processRemark })
  Message.success('报修状态已更新')
  processVisible.value = false
  await Promise.all([loadRepairs(), loadList()])
  return true
}

onMounted(() => Promise.all([loadList(), loadRepairs()]))

watch(() => route.query.tab, (tab) => {
  activeTab.value = tab === 'repair' ? 'repair' : 'computer'
})
</script>
