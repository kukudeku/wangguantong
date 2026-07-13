<template>
  <a-card class="page-card" title="电脑机位管理">
    <!-- 查询条件和新增入口。 -->
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

    <!-- 电脑列表；状态和操作列使用自定义插槽。 -->
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

    <!-- 新增和编辑复用同一个弹窗，通过 form.id 判断当前模式。 -->
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

// query 保存筛选条件，tableData 保存接口返回的列表。
const query = reactive({ area: '', status: '' })
const tableData = ref([])
// formVisible 控制弹窗开关，form 保存新增/编辑内容。
const formVisible = ref(false)
const form = reactive({
  id: null,
  computerNo: '',
  area: '',
  pricePerHour: 6,
  status: '空闲',
  remark: ''
})

// 表格列配置。操作列固定宽度，避免两个按钮被挤压换行。
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
  // 将业务状态映射为 Arco 标签颜色，方便管理员快速区分机位。
  if (status === '空闲') return 'green'
  if (status === '使用中') return 'blue'
  if (status === '预约锁定') return 'purple'
  return 'orange'
}

async function loadList() {
  // Axios 会把 query 转为 ?area=...&status=... 查询参数。
  tableData.value = await getComputerList(query)
}

function resetQuery() {
  // 清空条件后立即重新查询全部电脑。
  query.area = ''
  query.status = ''
  loadList()
}

function openAdd() {
  // Object.assign 一次性恢复新增电脑的默认值，避免残留上次编辑的数据。
  Object.assign(form, { id: null, computerNo: '', area: '', pricePerHour: 6, status: '空闲', remark: '' })
  formVisible.value = true
}

function openEdit(record) {
  // 把当前行数据复制到表单，随后打开编辑弹窗。
  Object.assign(form, record)
  formVisible.value = true
}

async function saveComputer() {
  // 有 id 代表数据库已有记录，调用修改；没有 id 代表新增。
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
  // 删除按钮外层有 a-popconfirm，只有用户确认后才会执行此函数。
  await deleteComputer(id)
  Message.success('删除成功')
  loadList()
}

// 页面挂载后自动查询电脑列表。
onMounted(loadList)
</script>
