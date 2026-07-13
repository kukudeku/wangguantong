<template>
  <a-card title="推广计划" class="page-card">
    <div class="promotion-stats">
      <div>
        <span>成功邀请</span>
        <strong>{{ overview.successCount || 0 }} 人</strong>
      </div>
      <div>
        <span>累计发放奖励</span>
        <strong>￥{{ money(overview.totalReward) }}</strong>
      </div>
      <div>
        <span>活动状态</span>
        <strong><a-tag :color="ruleForm.status === '启用' ? 'green' : 'gray'">{{ ruleForm.status }}</a-tag></strong>
      </div>
    </div>

    <section class="rule-section">
      <div class="section-title">
        <div>
          <h3>奖励规则</h3>
          <p>好友注册时填写有效邀请码，注册成功后双方奖励立即进入余额。</p>
        </div>
        <a-button type="primary" :loading="saving" @click="saveRule">保存规则</a-button>
      </div>
      <a-form :model="ruleForm" layout="inline" class="rule-form">
        <a-form-item label="邀请人奖励">
          <a-input-number v-model="ruleForm.inviterReward" :min="0" :precision="2" style="width: 190px">
            <template #prefix>￥</template>
          </a-input-number>
        </a-form-item>
        <a-form-item label="新用户奖励">
          <a-input-number v-model="ruleForm.inviteeReward" :min="0" :precision="2" style="width: 190px">
            <template #prefix>￥</template>
          </a-input-number>
        </a-form-item>
        <a-form-item label="活动状态">
          <a-radio-group v-model="ruleForm.status" type="button">
            <a-radio value="启用">启用</a-radio>
            <a-radio value="停用">停用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </section>

    <section class="record-section">
      <div class="section-title">
        <div>
          <h3>邀请记录</h3>
          <p>记录注册时使用的邀请码及双方实际到账金额。</p>
        </div>
        <a-button @click="loadData">刷新</a-button>
      </div>
      <a-table
        class="no-wrap-table"
        :columns="columns"
        :data="overview.records || []"
        row-key="id"
        :pagination="{ pageSize: 10 }"
        :scroll="{ x: 910 }"
      >
        <template #inviterReward="{ record }">￥{{ money(record.inviterReward) }}</template>
        <template #inviteeReward="{ record }">￥{{ money(record.inviteeReward) }}</template>
        <template #status="{ record }"><a-tag color="green">{{ record.status }}</a-tag></template>
        <template #createTime="{ record }">{{ formatDateTime(record.createTime) }}</template>
      </a-table>
    </section>
  </a-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { getPromotionAdminOverview, updatePromotionRule } from '../../api/promotion'
import { formatDateTime } from '../../utils/format'

const overview = ref({ successCount: 0, totalReward: 0, records: [] })
const ruleForm = reactive({ inviterReward: 10, inviteeReward: 5, status: '启用' })
const saving = ref(false)

const columns = [
  { title: '邀请码', dataIndex: 'inviteCode', width: 120 },
  { title: '邀请人', dataIndex: 'inviterMemberName', width: 120 },
  { title: '新用户', dataIndex: 'inviteeMemberName', width: 120 },
  { title: '邀请人奖励', slotName: 'inviterReward', width: 130 },
  { title: '新用户奖励', slotName: 'inviteeReward', width: 130 },
  { title: '状态', slotName: 'status', width: 100 },
  { title: '邀请成功时间', slotName: 'createTime', width: 190 }
]

function money(value) {
  return Number(value || 0).toFixed(2)
}

async function loadData() {
  const data = await getPromotionAdminOverview()
  overview.value = data
  Object.assign(ruleForm, data.rule || { inviterReward: 10, inviteeReward: 5, status: '启用' })
}

async function saveRule() {
  if (Number(ruleForm.inviterReward) < 0 || Number(ruleForm.inviteeReward) < 0) {
    Message.error('奖励金额不能小于 0')
    return
  }
  saving.value = true
  try {
    await updatePromotionRule(ruleForm)
    Message.success('推广规则保存成功')
    await loadData()
  } finally {
    saving.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.promotion-stats {
  margin-bottom: 20px;
  border: 1px solid #f2f3f5;
  border-radius: 4px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  overflow: hidden;
}

.promotion-stats > div {
  min-height: 94px;
  padding: 18px 20px;
  border-right: 1px solid #f2f3f5;
}

.promotion-stats > div:last-child {
  border-right: 0;
}

.promotion-stats span,
.promotion-stats strong {
  display: block;
}

.promotion-stats span {
  color: #86909c;
  font-size: 13px;
}

.promotion-stats strong {
  margin-top: 10px;
  font-size: 24px;
  font-weight: 600;
  white-space: nowrap;
}

.rule-section,
.record-section {
  padding: 20px 0;
  border-top: 1px solid #f2f3f5;
}

.record-section {
  padding-bottom: 0;
}

.section-title {
  margin-bottom: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.section-title h3,
.section-title p {
  margin: 0;
}

.section-title h3 {
  font-size: 16px;
}

.section-title p {
  margin-top: 5px;
  color: #86909c;
  font-size: 12px;
}

.section-title :deep(.arco-btn) {
  flex: 0 0 auto;
  white-space: nowrap;
}

.rule-form {
  padding: 18px;
  border: 1px solid #e5e6eb;
  border-radius: 4px;
  background: #f7f8fa;
}

.rule-form :deep(.arco-form-item) {
  margin-bottom: 0;
}

@media (max-width: 760px) {
  .promotion-stats {
    grid-template-columns: 1fr;
  }

  .promotion-stats > div {
    min-height: 76px;
    border-right: 0;
    border-bottom: 1px solid #f2f3f5;
  }

  .promotion-stats > div:last-child {
    border-bottom: 0;
  }

  .rule-form :deep(.arco-form-item),
  .rule-form :deep(.arco-input-number) {
    width: 100% !important;
  }
}
</style>
