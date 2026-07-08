<template>
  <a-card class="page-card" title="充值管理">
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

    <a-table :columns="columns" :data="records" row-key="id" :pagination="false" />
  </a-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { getMemberList } from '../../api/member'
import { addRecharge, getRechargeList } from '../../api/recharge'

const members = ref([])
const records = ref([])
const form = reactive({
  memberId: null,
  amount: 50
})

const columns = [
  { title: '记录ID', dataIndex: 'id', width: 90 },
  { title: '会员ID', dataIndex: 'memberId' },
  { title: '会员姓名', dataIndex: 'memberName' },
  { title: '充值金额', dataIndex: 'amount' },
  { title: '充值时间', dataIndex: 'createTime' }
]

async function loadData() {
  members.value = await getMemberList({})
  records.value = await getRechargeList()
}

async function submitRecharge() {
  if (!form.memberId) {
    Message.error('请选择会员')
    return
  }
  await addRecharge({ memberId: form.memberId, amount: form.amount })
  Message.success('充值成功')
  form.memberId = null
  form.amount = 50
  loadData()
}

onMounted(loadData)
</script>
