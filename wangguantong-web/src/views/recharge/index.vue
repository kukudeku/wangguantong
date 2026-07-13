<template>
  <a-card class="page-card" :title="activeTab === 'voucher' ? '团购券管理' : '充值管理'">
    <a-tabs v-model:active-key="activeTab" @change="changeTab">
      <a-tab-pane key="recharge" title="余额充值">
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

        <a-table class="no-wrap-table" :columns="columns" :data="records" row-key="id" :pagination="false" :scroll="{ x: 880 }">
          <template #createTime="{ record }">{{ formatDateTime(record.createTime) }}</template>
          <template #referenceNo="{ record }">{{ record.referenceNo || '-' }}</template>
        </a-table>
      </a-tab-pane>

      <a-tab-pane key="voucher" title="团购券管理">
        <a-alert class="voucher-alert" type="info">
          用户端只能核销后台已录入且状态为“未使用”的团购券，核销成功后券码立即失效。
        </a-alert>
        <a-form class="toolbar" :model="voucherQuery" layout="inline">
          <a-form-item label="券码">
            <a-input v-model="voucherQuery.voucherCode" allow-clear placeholder="输入券码查询" />
          </a-form-item>
          <a-form-item label="状态">
            <a-select v-model="voucherQuery.status" allow-clear placeholder="全部状态" style="width: 140px">
              <a-option value="未使用">未使用</a-option>
              <a-option value="已使用">已使用</a-option>
              <a-option value="已禁用">已禁用</a-option>
            </a-select>
          </a-form-item>
          <a-space>
            <a-button type="primary" @click="loadVouchers">查询</a-button>
            <a-button @click="resetVoucherQuery">重置</a-button>
            <a-button type="primary" @click="openVoucherModal">新增团购券</a-button>
          </a-space>
        </a-form>

        <a-table class="no-wrap-table" :columns="voucherColumns" :data="vouchers" row-key="id" :pagination="false" :scroll="{ x: 1260 }">
          <template #status="{ record }">
            <a-tag :color="voucherStatusColor(record.status)">{{ record.status }}</a-tag>
          </template>
          <template #usedMember="{ record }">{{ record.usedMemberName || '-' }}</template>
          <template #usedTime="{ record }">{{ formatDateTime(record.usedTime) }}</template>
          <template #expireTime="{ record }">{{ formatDateTime(record.expireTime) }}</template>
          <template #createTime="{ record }">{{ formatDateTime(record.createTime) }}</template>
          <template #actions="{ record }">
            <div class="action-buttons">
              <a-popconfirm
                v-if="record.status === '未使用'"
                content="确认禁用该团购券吗？"
                @ok="changeVoucherStatus(record, '已禁用')"
              >
                <a-button size="small" status="danger">禁用</a-button>
              </a-popconfirm>
              <a-popconfirm
                v-else-if="record.status === '已禁用'"
                content="确认重新启用该团购券吗？"
                @ok="changeVoucherStatus(record, '未使用')"
              >
                <a-button size="small" type="primary">启用</a-button>
              </a-popconfirm>
              <span v-else>不可操作</span>
            </div>
          </template>
        </a-table>
      </a-tab-pane>
    </a-tabs>

    <a-modal v-model:visible="voucherVisible" title="新增团购券" :on-before-ok="submitVoucher">
      <a-form :model="voucherForm" layout="vertical">
        <a-form-item label="团购券码" required>
          <a-input v-model="voucherForm.voucherCode" placeholder="例如 MT-20260713-001" />
        </a-form-item>
        <a-form-item label="充值金额" required>
          <a-input-number v-model="voucherForm.amount" :min="1" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="有效期">
          <a-date-picker v-model="voucherForm.expireTime" show-time style="width: 100%" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model="voucherForm.remark" :max-length="200" show-word-limit />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { getMemberList } from '../../api/member'
import { addRecharge, getRechargeList } from '../../api/recharge'
import { addVoucher, getVoucherList, updateVoucherStatus } from '../../api/voucher'
import { formatDateTime } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const activeTab = ref(route.path === '/admin/voucher' ? 'voucher' : 'recharge')
const members = ref([])
const records = ref([])
const vouchers = ref([])
const voucherVisible = ref(false)
const form = reactive({ memberId: null, amount: 50 })
const voucherQuery = reactive({ voucherCode: '', status: '' })
const voucherForm = reactive({ voucherCode: '', amount: 50, expireTime: '', remark: '' })

const columns = [
  { title: '记录ID', dataIndex: 'id', width: 90 },
  { title: '会员姓名', dataIndex: 'memberName', width: 120 },
  { title: '充值金额', dataIndex: 'amount', width: 120 },
  { title: '充值方式', dataIndex: 'rechargeType', width: 140 },
  { title: '关联券码', slotName: 'referenceNo', width: 200 },
  { title: '充值时间', slotName: 'createTime', width: 190 }
]

const voucherColumns = [
  { title: '券码', dataIndex: 'voucherCode', width: 210 },
  { title: '金额', dataIndex: 'amount', width: 100 },
  { title: '状态', slotName: 'status', width: 110 },
  { title: '核销会员', slotName: 'usedMember', width: 120 },
  { title: '核销时间', slotName: 'usedTime', width: 180 },
  { title: '有效期', slotName: 'expireTime', width: 180 },
  { title: '备注', dataIndex: 'remark', width: 180 },
  { title: '创建时间', slotName: 'createTime', width: 180 },
  { title: '操作', slotName: 'actions', width: 110 }
]

function voucherStatusColor(status) {
  if (status === '未使用') return 'blue'
  if (status === '已使用') return 'green'
  if (status === '已过期') return 'gray'
  return 'red'
}

async function loadData() {
  members.value = await getMemberList({})
  records.value = await getRechargeList()
  await loadVouchers()
}

async function loadVouchers() {
  vouchers.value = await getVoucherList(voucherQuery)
}

function resetVoucherQuery() {
  voucherQuery.voucherCode = ''
  voucherQuery.status = ''
  loadVouchers()
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

function openVoucherModal() {
  Object.assign(voucherForm, { voucherCode: '', amount: 50, expireTime: '', remark: '' })
  voucherVisible.value = true
}

async function submitVoucher() {
  if (!voucherForm.voucherCode.trim()) {
    Message.error('请输入团购券码')
    return false
  }
  await addVoucher(voucherForm)
  Message.success('团购券创建成功')
  voucherVisible.value = false
  loadVouchers()
  return true
}

async function changeVoucherStatus(record, status) {
  await updateVoucherStatus({ id: String(record.id), status })
  Message.success(status === '已禁用' ? '团购券已禁用' : '团购券已启用')
  loadVouchers()
}

function changeTab(tab) {
  const path = tab === 'voucher' ? '/admin/voucher' : '/admin/recharge'
  if (route.path !== path) router.push(path)
}

onMounted(loadData)

watch(() => route.path, (path) => {
  activeTab.value = path === '/admin/voucher' ? 'voucher' : 'recharge'
})
</script>

<style scoped>
.voucher-alert {
  margin-bottom: 16px;
}
</style>
