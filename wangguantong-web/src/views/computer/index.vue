<template>
  <a-card class="page-card" title="电脑机位管理">
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

    <a-table class="no-wrap-table" :columns="columns" :data="tableData" row-key="id" :pagination="false">
      <template #status="{ record }">
        <a-tag :color="statusColor(record.status)">{{ record.status }}</a-tag>
      </template>
      <template #actions="{ record }">
        <div class="action-buttons">
          <a-button size="small" @click="openEdit(record)">编辑</a-button>
          <a-popconfirm content="确认删除该电脑吗？" @ok="handleDelete(record.id)">
            <a-button size="small" status="danger">删除</a-button>
          </a-popconfirm>
        </div>
      </template>
    </a-table>

    <a-modal v-model:visible="formVisible" :title="form.id ? '编辑电脑' : '新增电脑'" @ok="saveComputer">
      <a-form :model="form" layout="vertical">
        <a-form-item label="电脑编号">
          <a-input v-model="form.computerNo" placeholder="例如 A001" />
        </a-form-item>
        <a-form-item label="所在区域">
          <a-input v-model="form.area" placeholder="例如 电竞一区" />
        </a-form-item>
        <a-form-item label="每小时单价">
          <a-input-number v-model="form.pricePerHour" :min="1" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model="form.status">
            <a-option value="空闲">空闲</a-option>
            <a-option value="使用中">使用中</a-option>
            <a-option value="预约锁定">预约锁定</a-option>
            <a-option value="维修中">维修中</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model="form.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { addComputer, deleteComputer, getComputerList, updateComputer } from '../../api/computer'

const query = reactive({ area: '', status: '' })
const tableData = ref([])
const formVisible = ref(false)
const form = reactive({
  id: null,
  computerNo: '',
  area: '',
  pricePerHour: 6,
  status: '空闲',
  remark: ''
})

const columns = [
  { title: '电脑ID', dataIndex: 'id', width: 90 },
  { title: '电脑编号', dataIndex: 'computerNo' },
  { title: '区域', dataIndex: 'area' },
  { title: '每小时单价', dataIndex: 'pricePerHour' },
  { title: '状态', slotName: 'status' },
  { title: '备注', dataIndex: 'remark' },
  { title: '操作', slotName: 'actions', width: 160 }
]

function statusColor(status) {
  if (status === '空闲') return 'green'
  if (status === '使用中') return 'blue'
  if (status === '预约锁定') return 'purple'
  return 'orange'
}

async function loadList() {
  tableData.value = await getComputerList(query)
}

function resetQuery() {
  query.area = ''
  query.status = ''
  loadList()
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
  if (form.id) {
    await updateComputer(form)
  } else {
    await addComputer(form)
  }
  Message.success('保存成功')
  formVisible.value = false
  loadList()
}

async function handleDelete(id) {
  await deleteComputer(id)
  Message.success('删除成功')
  loadList()
}

onMounted(loadList)
</script>
