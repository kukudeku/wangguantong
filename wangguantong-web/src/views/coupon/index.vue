<template>
  <a-card class="page-card" :title="activeTab === 'rule' ? '签到奖励规则' : '优惠券模板'">
    <a-tabs v-model:active-key="activeTab" @change="changeTab">
      <a-tab-pane key="template" title="优惠券模板">
        <a-form class="toolbar" :model="query" layout="inline">
          <a-form-item label="名称"><a-input v-model="query.name" allow-clear /></a-form-item>
          <a-form-item label="状态">
            <a-select v-model="query.status" allow-clear style="width: 130px">
              <a-option value="启用">启用</a-option>
              <a-option value="停用">停用</a-option>
            </a-select>
          </a-form-item>
          <a-space>
            <a-button type="primary" @click="loadTemplates">查询</a-button>
            <a-button @click="resetQuery">重置</a-button>
            <a-button type="primary" @click="openTemplate()">新增优惠券</a-button>
          </a-space>
        </a-form>

        <a-table class="no-wrap-table" :columns="templateColumns" :data="templates" row-key="id" :pagination="false" :scroll="{ x: 1050 }">
          <template #status="{ record }"><a-tag :color="record.status === '启用' ? 'green' : 'gray'">{{ record.status }}</a-tag></template>
          <template #createTime="{ record }">{{ formatDateTime(record.createTime) }}</template>
          <template #actions="{ record }">
            <div class="action-buttons">
              <a-button size="small" @click="openTemplate(record)">编辑</a-button>
              <a-popconfirm :content="`确认${record.status === '启用' ? '停用' : '启用'}该优惠券吗？`" @ok="toggleTemplate(record)">
                <a-button size="small" :status="record.status === '启用' ? 'danger' : 'normal'">{{ record.status === '启用' ? '停用' : '启用' }}</a-button>
              </a-popconfirm>
            </div>
          </template>
        </a-table>
      </a-tab-pane>

      <a-tab-pane key="rule" title="签到奖励规则">
        <div class="toolbar">
          <a-button type="primary" @click="openRule()">新增签到规则</a-button>
        </div>
        <a-alert class="rule-alert">用户连续签到达到指定天数时，系统自动发放对应优惠券。同一天数只能配置一条规则。</a-alert>
        <a-table class="no-wrap-table" :columns="ruleColumns" :data="rules" row-key="id" :pagination="false" :scroll="{ x: 760 }">
          <template #days="{ record }">连续签到 {{ record.consecutiveDays }} 天</template>
          <template #createTime="{ record }">{{ formatDateTime(record.createTime) }}</template>
          <template #actions="{ record }">
            <div class="action-buttons">
              <a-button size="small" @click="openRule(record)">编辑</a-button>
              <a-popconfirm content="确认删除该签到规则吗？" @ok="removeRule(record.id)">
                <a-button size="small" status="danger">删除</a-button>
              </a-popconfirm>
            </div>
          </template>
        </a-table>
      </a-tab-pane>
    </a-tabs>

    <a-modal v-model:visible="templateVisible" :title="templateForm.id ? '编辑优惠券' : '新增优惠券'" :on-before-ok="submitTemplate">
      <a-form :model="templateForm" layout="vertical">
        <a-form-item label="优惠券名称" required><a-input v-model="templateForm.name" placeholder="例如 满 10 减 3 元" /></a-form-item>
        <a-form-item label="使用门槛" required><a-input-number v-model="templateForm.minSpend" :min="0" :precision="2" style="width: 100%"><template #prefix>￥</template></a-input-number></a-form-item>
        <a-form-item label="优惠金额" required><a-input-number v-model="templateForm.discountAmount" :min="0.01" :precision="2" style="width: 100%"><template #prefix>￥</template></a-input-number></a-form-item>
        <a-form-item label="领取后有效天数" required><a-input-number v-model="templateForm.validDays" :min="1" style="width: 100%" /></a-form-item>
        <a-form-item label="状态"><a-select v-model="templateForm.status"><a-option value="启用">启用</a-option><a-option value="停用">停用</a-option></a-select></a-form-item>
        <a-form-item label="备注"><a-textarea v-model="templateForm.remark" :max-length="200" show-word-limit /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:visible="ruleVisible" :title="ruleForm.id ? '编辑签到规则' : '新增签到规则'" :on-before-ok="submitRule">
      <a-form :model="ruleForm" layout="vertical">
        <a-form-item label="连续签到天数" required><a-input-number v-model="ruleForm.consecutiveDays" :min="1" style="width: 100%" /></a-form-item>
        <a-form-item label="奖励优惠券" required>
          <a-select v-model="ruleForm.couponTemplateId" placeholder="请选择已启用优惠券">
            <a-option v-for="item in enabledTemplates" :key="item.id" :value="item.id">{{ item.name }}（满 {{ item.minSpend }} 减 {{ item.discountAmount }}）</a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { deleteSignInRule, getCouponTemplates, getSignInRules, saveCouponTemplate, saveSignInRule, updateCouponTemplateStatus } from '../../api/coupon'
import { formatDateTime } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const activeTab = ref(route.query.tab === 'rule' ? 'rule' : 'template')
const templates = ref([])
const rules = ref([])
const templateVisible = ref(false)
const ruleVisible = ref(false)
const query = reactive({ name: '', status: '' })
const templateForm = reactive({ id: null, name: '', minSpend: 0, discountAmount: 1, validDays: 7, status: '启用', remark: '' })
const ruleForm = reactive({ id: null, consecutiveDays: 1, couponTemplateId: null })
const enabledTemplates = computed(() => templates.value.filter((item) => item.status === '启用'))

const templateColumns = [
  { title: '名称', dataIndex: 'name', width: 180 }, { title: '使用门槛', dataIndex: 'minSpend', width: 110 },
  { title: '优惠金额', dataIndex: 'discountAmount', width: 110 }, { title: '有效天数', dataIndex: 'validDays', width: 100 },
  { title: '状态', slotName: 'status', width: 90 }, { title: '备注', dataIndex: 'remark', width: 220 },
  { title: '创建时间', slotName: 'createTime', width: 180 }, { title: '操作', slotName: 'actions', width: 150 }
]
const ruleColumns = [
  { title: '奖励条件', slotName: 'days', width: 180 }, { title: '奖励优惠券', dataIndex: 'couponName' },
  { title: '创建时间', slotName: 'createTime', width: 200 }, { title: '操作', slotName: 'actions', width: 160 }
]

async function loadTemplates() { templates.value = await getCouponTemplates(query) }
async function loadRules() { rules.value = await getSignInRules() }
function resetQuery() { query.name = ''; query.status = ''; loadTemplates() }
function changeTab(tab) {
  if (route.query.tab !== tab) router.replace({ path: route.path, query: { ...route.query, tab } })
}
function openTemplate(record) {
  Object.assign(templateForm, record || { id: null, name: '', minSpend: 0, discountAmount: 1, validDays: 7, status: '启用', remark: '' })
  templateVisible.value = true
}
async function submitTemplate() {
  if (!templateForm.name.trim()) { Message.error('请输入优惠券名称'); return false }
  await saveCouponTemplate(templateForm); Message.success('优惠券保存成功'); await loadTemplates(); return true
}
async function toggleTemplate(record) {
  await updateCouponTemplateStatus({ id: String(record.id), status: record.status === '启用' ? '停用' : '启用' }); Message.success('状态已更新'); loadTemplates()
}
function openRule(record) {
  Object.assign(ruleForm, record || { id: null, consecutiveDays: 1, couponTemplateId: null })
  ruleVisible.value = true
}
async function submitRule() {
  if (!ruleForm.couponTemplateId) { Message.error('请选择奖励优惠券'); return false }
  await saveSignInRule(ruleForm); Message.success('签到规则保存成功'); await loadRules(); return true
}
async function removeRule(id) { await deleteSignInRule(id); Message.success('签到规则已删除'); loadRules() }
onMounted(() => Promise.all([loadTemplates(), loadRules()]))
watch(() => route.query.tab, (tab) => { activeTab.value = tab === 'rule' ? 'rule' : 'template' })
</script>

<style scoped>
.rule-alert { margin-bottom: 16px; }
</style>
