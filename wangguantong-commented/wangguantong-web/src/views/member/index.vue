<template>
  <a-card class="page-card" title="会员管理">
    <!-- 会员查询区，可组合姓名、身份证号和手机号条件。 -->
    <a-form class="toolbar" :model="query" layout="inline">
      <a-form-item label="会员姓名">
        <a-input v-model="query.name" placeholder="请输入会员姓名" allow-clear />
      </a-form-item>
      <a-form-item label="身份证号">
        <a-input v-model="query.idCard" placeholder="请输入身份证号" allow-clear />
      </a-form-item>
      <a-form-item label="手机号">
        <a-input v-model="query.phone" placeholder="请输入手机号" allow-clear />
      </a-form-item>
      <a-space>
        <a-button type="primary" @click="loadList">查询</a-button>
        <a-button @click="resetQuery">重置</a-button>
        <a-button type="primary" @click="openAdd">新增会员</a-button>
      </a-space>
    </a-form>

    <!-- 用户数据表格：状态、时间、操作三列使用自定义插槽。 -->
    <a-table class="no-wrap-table" :columns="columns" :data="tableData" row-key="id" :pagination="false">
      <template #status="{ record }">
        <a-tag :color="record.status === '正常' ? 'green' : 'red'">{{ record.status }}</a-tag>
      </template>
      <template #createTime="{ record }">
        {{ formatDateTime(record.createTime) }}
      </template>
      <template #actions="{ record }">
        <div class="action-buttons">
          <a-button size="small" @click="openEdit(record)">编辑</a-button>
          <a-button size="small" type="primary" @click="openRecharge(record)">充值</a-button>
          <a-popconfirm content="确认删除该会员吗？" @ok="handleDelete(record.id)">
            <a-button size="small" status="danger">删除</a-button>
          </a-popconfirm>
        </div>
      </template>
    </a-table>

    <!-- 新增/编辑用户弹窗。新增必须设置密码，编辑时留空表示保持旧密码。 -->
    <a-modal v-model:visible="formVisible" :title="form.id ? '编辑会员' : '新增会员'" @ok="saveMember">
      <a-form :model="form" layout="vertical">
        <a-form-item label="会员姓名">
          <a-input v-model="form.name" />
        </a-form-item>
        <a-form-item label="身份证号">
          <a-input v-model="form.idCard" />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model="form.phone" />
        </a-form-item>
        <a-form-item :label="form.id ? '重置密码' : '登录密码'">
          <a-input-password v-model="form.password" :placeholder="form.id ? '留空则不修改密码' : '请输入登录密码'" />
        </a-form-item>
        <a-form-item v-if="form.id" label="余额">
          <a-input-number v-model="form.balance" :min="0" />
        </a-form-item>
        <a-form-item v-if="form.id" label="状态">
          <a-select v-model="form.status">
            <a-option value="正常">正常</a-option>
            <a-option value="禁用">禁用</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="用户类型">
          <a-select v-model="form.userType">
            <a-option value="会员">会员</a-option>
            <a-option value="散客">散客</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="会员级别">
          <a-select v-model="form.memberLevel">
            <a-option value="散客">散客（无折扣）</a-option>
            <a-option value="普通会员">普通会员（不打折）</a-option>
            <a-option value="黄金会员">黄金会员（9折）</a-option>
            <a-option value="钻石会员">钻石会员（8折）</a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 快捷充值弹窗，与独立“充值管理”页面调用同一个后端接口。 -->
    <a-modal v-model:visible="rechargeVisible" title="会员充值" @ok="submitRecharge">
      <a-form :model="rechargeForm" layout="vertical">
        <a-form-item label="会员">
          <a-input :model-value="rechargeForm.memberName" disabled />
        </a-form-item>
        <a-form-item label="充值金额">
          <a-input-number v-model="rechargeForm.amount" :min="1" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { addRecharge } from '../../api/recharge'
import { addMember, deleteMember, getMemberList, updateMember } from '../../api/member'
import { formatDateTime } from '../../utils/format'

// 查询条件、表格数据和两个弹窗的显示状态。
const query = reactive({ name: '', idCard: '', phone: '' })
const tableData = ref([])
const formVisible = ref(false)
const rechargeVisible = ref(false)

// 新增/编辑会员共用的表单模型。
const form = reactive({
  id: null,
  name: '',
  idCard: '',
  phone: '',
  password: '',
  balance: 0,
  userType: '会员',
  memberLevel: '普通会员',
  status: '正常'
})

// 充值表单单独保存，避免影响会员编辑表单。
const rechargeForm = reactive({
  memberId: null,
  memberName: '',
  amount: 50
})

// slotName 对应 template 中的 #status、#createTime 和 #actions。
const columns = [
  { title: '会员ID', dataIndex: 'id', width: 90 },
  { title: '会员姓名', dataIndex: 'name' },
  { title: '身份证号', dataIndex: 'idCard', width: 170 },
  { title: '手机号', dataIndex: 'phone' },
  { title: '余额', dataIndex: 'balance' },
  { title: '用户类型', dataIndex: 'userType' },
  { title: '会员级别', dataIndex: 'memberLevel' },
  { title: '状态', slotName: 'status' },
  { title: '注册时间', slotName: 'createTime' },
  { title: '操作', slotName: 'actions', width: 260 }
]

async function loadList() {
  // 查询结果赋给 ref 必须写 .value；模板中使用时 Vue 会自动解包。
  tableData.value = await getMemberList(query)
}

function resetQuery() {
  // 恢复空查询条件并重新加载全部会员。
  query.name = ''
  query.idCard = ''
  query.phone = ''
  loadList()
}

function openAdd() {
  // 先清空可能残留的编辑数据，再打开弹窗。
  Object.assign(form, { id: null, name: '', idCard: '', phone: '', password: '', balance: 0, userType: '会员', memberLevel: '普通会员', status: '正常' })
  formVisible.value = true
}

function openEdit(record) {
  // 不把数据库密码回填到页面；空密码表示本次不重置。
  Object.assign(form, { ...record, password: '' })
  formVisible.value = true
}

async function saveMember() {
  // 新用户必须设置登录密码，登录账号使用身份证号。
  if (!form.id && !form.password) {
    Message.error('请设置登录密码')
    return
  }
  const payload = { ...form }
  if (payload.id && !payload.password) {
    // 编辑时删除空 password 字段，防止把原密码覆盖为空字符串。
    delete payload.password
  }
  if (form.id) {
    await updateMember(payload)
  } else {
    await addMember(payload)
  }
  Message.success('保存成功')
  formVisible.value = false
  loadList()
}

async function handleDelete(id) {
  await deleteMember(id)
  Message.success('删除成功')
  loadList()
}

function openRecharge(record) {
  // 保存当前行的用户编号，后续提交充值时使用。
  Object.assign(rechargeForm, { memberId: record.id, memberName: record.name, amount: 50 })
  rechargeVisible.value = true
}

async function submitRecharge() {
  // 后端完成余额增加、散客转会员和充值明细保存。
  await addRecharge({ memberId: rechargeForm.memberId, amount: rechargeForm.amount })
  Message.success('充值成功')
  rechargeVisible.value = false
  loadList()
}

// 进入页面立即加载会员列表。
onMounted(loadList)
</script>
