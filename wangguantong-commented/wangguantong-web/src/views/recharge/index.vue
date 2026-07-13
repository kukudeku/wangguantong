<template>
  <a-card class="page-card" title="充值管理">
    <!-- 上方表单负责选择会员和充值金额。 -->
    <a-form class="toolbar" :model="form" layout="inline">
      <a-form-item label="会员">
        <a-select v-model="form.memberId" placeholder="请选择会员" style="width: 220px">
          <a-option v-for="member in members" :key="member.id" :value="member.id">
            {{ member.name }}（余额：{{ member.balance }}）
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item label="充值金额">
        <a-input-number v-model="form.amount" :min="1" />
      </a-form-item>
      <a-button type="primary" @click="submitRecharge">确认充值</a-button>
    </a-form>

    <!-- 下方表格显示所有充值明细；createTime 插槽负责格式化时间。 -->
    <a-table class="no-wrap-table" :columns="columns" :data="records" row-key="id" :pagination="false">
      <template #createTime="{ record }">
        {{ formatDateTime(record.createTime) }}
      </template>
    </a-table>
  </a-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { getMemberList } from '../../api/member'
import { addRecharge, getRechargeList } from '../../api/recharge'
import { formatDateTime } from '../../utils/format'

const members = ref([])
const records = ref([])
// reactive 用于表单对象，修改字段后输入控件会立即同步。
const form = reactive({
  memberId: null,
  amount: 50
})

// columns 描述表格每一列；slotName 表示该列交由 template 中的同名插槽渲染。
const columns = [
  { title: '记录ID', dataIndex: 'id', width: 90 },
  { title: '会员ID', dataIndex: 'memberId' },
  { title: '会员姓名', dataIndex: 'memberName' },
  { title: '充值金额', dataIndex: 'amount' },
  { title: '充值时间', slotName: 'createTime' }
]

async function loadData() {
  // 两个请求分别取得下拉选项和充值记录。
  members.value = await getMemberList({})
  records.value = await getRechargeList()
}

async function submitRecharge() {
  // 未选择会员时禁止提交。
  if (!form.memberId) {
    Message.error('请选择会员')
    return
  }
  // 后端负责增加余额并写入充值记录。
  await addRecharge({ memberId: form.memberId, amount: form.amount })
  Message.success('充值成功')
  form.memberId = null
  form.amount = 50
  // 成功后恢复默认表单并刷新下拉余额和明细表格。
  loadData()
}

// 组件第一次显示到页面后自动加载数据。
onMounted(loadData)
</script>
