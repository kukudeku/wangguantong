<template>
  <a-card class="page-card" :title="activeTab === 'item' ? '商品维护' : '点餐订单'">
    <a-tabs v-model:active-key="activeTab" @change="changeTab">
      <a-tab-pane key="order" title="点餐下单">
        <a-form class="toolbar" :model="orderForm" layout="inline">
          <a-form-item label="顾客类型">
            <a-select v-model="orderForm.customerType" style="width: 120px">
              <a-option value="会员">会员</a-option>
              <a-option value="散客">散客</a-option>
            </a-select>
          </a-form-item>
          <a-form-item v-if="orderForm.customerType === '会员'" label="会员">
            <a-select v-model="orderForm.memberId" placeholder="请选择会员" style="width: 220px">
              <a-option v-for="member in members" :key="member.id" :value="member.id">
                {{ member.name }}（余额：{{ member.balance }}）
              </a-option>
            </a-select>
          </a-form-item>
          <a-form-item v-else label="散客姓名">
            <a-input v-model="orderForm.customerName" placeholder="例如 散客001" />
          </a-form-item>
          <a-form-item label="商品">
            <a-select v-model="orderForm.foodItemId" placeholder="请选择商品" style="width: 220px">
              <a-option v-for="item in availableItems" :key="item.id" :value="item.id">
                {{ item.name }}（￥{{ item.price }}）
              </a-option>
            </a-select>
          </a-form-item>
          <a-form-item label="数量">
            <a-input-number v-model="orderForm.quantity" :min="1" />
          </a-form-item>
          <a-form-item label="支付方式">
            <a-select v-model="orderForm.paymentMethod" style="width: 140px">
              <a-option value="余额支付" :disabled="orderForm.customerType === '散客'">余额支付</a-option>
              <a-option value="微信支付">微信支付</a-option>
              <a-option value="支付宝支付">支付宝支付</a-option>
            </a-select>
          </a-form-item>
          <a-button type="primary" @click="submitOrder">确认下单</a-button>
        </a-form>

        <a-form class="toolbar" :model="orderQuery" layout="inline">
          <a-form-item label="顾客姓名">
            <a-input v-model="orderQuery.customerName" allow-clear />
          </a-form-item>
          <a-form-item label="订单状态">
            <a-select v-model="orderQuery.status" allow-clear style="width: 140px">
              <a-option value="已下单">已下单</a-option>
              <a-option value="待支付">待支付</a-option>
              <a-option value="退款中">退款中</a-option>
              <a-option value="已完成">已完成</a-option>
              <a-option value="已取消">已取消</a-option>
            </a-select>
          </a-form-item>
          <a-form-item label="支付方式">
            <a-select v-model="orderQuery.paymentMethod" allow-clear placeholder="全部方式" style="width: 140px">
              <a-option value="余额支付">余额支付</a-option>
              <a-option value="微信支付">微信支付</a-option>
              <a-option value="支付宝支付">支付宝支付</a-option>
            </a-select>
          </a-form-item>
          <a-space>
            <a-button type="primary" @click="loadOrders">查询</a-button>
            <a-button @click="resetOrderQuery">重置</a-button>
          </a-space>
        </a-form>

        <a-table class="no-wrap-table" :columns="orderColumns" :data="orders" row-key="id" :pagination="false" :scroll="{ x: 1780 }">
          <template #status="{ record }">
            <a-tag :color="orderStatusColor(record.status)">{{ record.status }}</a-tag>
          </template>
          <template #createTime="{ record }">
            {{ formatDateTime(record.createTime) }}
          </template>
          <template #paymentStatus="{ record }">
            <a-tag :color="paymentStatusColor(record.paymentStatus)">{{ record.paymentStatus || '已支付' }}</a-tag>
          </template>
          <template #actions="{ record }">
            <div v-if="record.status === '已下单'" class="action-buttons">
              <a-popconfirm content="确认该订单已完成吗？" @ok="handleCompleteOrder(record.id)">
                <a-button size="small" type="primary">完成订单</a-button>
              </a-popconfirm>
              <a-popconfirm content="确认取消该订单吗？" @ok="handleCancelOrder(record.id)">
                <a-button size="small" status="danger">取消订单</a-button>
              </a-popconfirm>
            </div>
            <a-button
              v-else-if="record.status === '待支付' && record.paymentOutTradeNo"
              size="small"
              type="primary"
              @click="resumePayment(record)"
            >继续支付</a-button>
            <span v-else>{{ record.status }}</span>
          </template>
        </a-table>
      </a-tab-pane>

      <a-tab-pane key="item" title="商品维护">
        <a-form class="toolbar" :model="itemQuery" layout="inline">
          <a-form-item label="商品名称">
            <a-input v-model="itemQuery.name" allow-clear />
          </a-form-item>
          <a-form-item label="分类">
            <a-select v-model="itemQuery.category" allow-clear style="width: 120px">
              <a-option v-for="category in foodCategories" :key="category" :value="category">{{ category }}</a-option>
            </a-select>
          </a-form-item>
          <a-form-item label="状态">
            <a-select v-model="itemQuery.status" allow-clear style="width: 120px">
              <a-option value="上架">上架</a-option>
              <a-option value="下架">下架</a-option>
            </a-select>
          </a-form-item>
          <a-space>
            <a-button type="primary" @click="loadItems">查询</a-button>
            <a-button @click="resetItemQuery">重置</a-button>
            <a-button type="primary" @click="openAddItem">新增商品</a-button>
          </a-space>
        </a-form>

        <a-table class="no-wrap-table" :columns="itemColumns" :data="items" row-key="id" :pagination="false" :scroll="{ x: 860 }">
          <template #status="{ record }">
            <a-tag :color="record.status === '上架' ? 'green' : 'gray'">{{ record.status }}</a-tag>
          </template>
          <template #actions="{ record }">
            <div class="action-buttons">
              <a-button size="small" @click="openEditItem(record)">编辑</a-button>
              <a-popconfirm content="确认删除该商品吗？" @ok="handleDeleteItem(record.id)">
                <a-button size="small" status="danger">删除</a-button>
              </a-popconfirm>
            </div>
          </template>
        </a-table>
      </a-tab-pane>
    </a-tabs>

    <a-modal v-model:visible="itemVisible" :title="itemForm.id ? '编辑商品' : '新增商品'" @ok="saveItem">
      <a-form :model="itemForm" layout="vertical">
        <a-form-item label="商品名称">
          <a-input v-model="itemForm.name" />
        </a-form-item>
        <a-form-item label="分类">
          <a-select v-model="itemForm.category">
            <a-option v-for="category in foodCategories" :key="category" :value="category">{{ category }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="价格">
          <a-input-number v-model="itemForm.price" :min="0.1" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model="itemForm.status">
            <a-option value="上架">上架</a-option>
            <a-option value="下架">下架</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model="itemForm.remark" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:visible="paymentDialog.visible"
      :title="paymentDialog.paymentMethod"
      width="420px"
      :footer="false"
      :mask-closable="false"
      @cancel="stopPaymentPolling"
    >
      <div class="admin-payment-dialog">
        <div class="admin-payment-amount">
          <span>待支付金额</span>
          <strong>￥{{ Number(paymentDialog.amount || 0).toFixed(2) }}</strong>
          <small>{{ paymentDialog.outTradeNo }}</small>
        </div>
        <template v-if="paymentDialog.paymentMethod === '微信支付'">
          <img class="admin-payment-qr" :src="paymentQrCodeUrl" alt="微信支付二维码" />
          <p>请顾客使用微信扫码支付</p>
        </template>
        <template v-else>
          <p>请在支付宝电脑网站收银台完成支付。</p>
          <a-button type="primary" long @click="openAlipayCashier">打开支付宝收银台</a-button>
        </template>
        <div class="admin-payment-status">
          <span>支付状态</span>
          <a-tag :color="paymentStatusColor(paymentDialog.status)">{{ paymentDialog.status }}</a-tag>
        </div>
        <a-button long :loading="paymentChecking" @click="checkPaymentStatus(true)">查询支付结果</a-button>
      </div>
    </a-modal>
  </a-card>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { getMemberList } from '../../api/member'
import {
  addFoodItem,
  addFoodOrder,
  cancelFoodOrder,
  completeFoodOrder,
  deleteFoodItem,
  getAlipayPaymentPageUrl,
  getAvailableFoodItems,
  getFoodItems,
  getFoodOrders,
  getPaymentStatus,
  getWechatPaymentQrCodeUrl,
  updateFoodItem
} from '../../api/food'
import { formatDateTime } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const activeTab = ref(route.query.tab === 'item' ? 'item' : 'order')
const members = ref([])
const availableItems = ref([])
const items = ref([])
const orders = ref([])
const itemVisible = ref(false)
const paymentChecking = ref(false)
const paymentDialog = reactive({ visible: false, outTradeNo: '', paymentMethod: '', amount: 0, status: '待支付' })
let paymentPollTimer = null
const foodCategories = ['饮料', '餐食', '零食', '其他']

const orderForm = reactive({
  customerType: '会员',
  memberId: null,
  customerName: '',
  foodItemId: null,
  quantity: 1,
  paymentMethod: '余额支付'
})

const orderQuery = reactive({ customerName: '', status: '', paymentMethod: '' })
const itemQuery = reactive({ name: '', category: '', status: '' })
const itemForm = reactive({ id: null, name: '', category: '饮料', price: 1, status: '上架', remark: '' })

const orderColumns = [
  { title: '订单ID', dataIndex: 'id', width: 90 },
  { title: '订单批次', dataIndex: 'batchNo', width: 190 },
  { title: '顾客类型', dataIndex: 'customerType' },
  { title: '顾客姓名', dataIndex: 'customerName' },
  { title: '商品', dataIndex: 'foodItemName' },
  { title: '单价', dataIndex: 'price' },
  { title: '数量', dataIndex: 'quantity' },
  { title: '优惠前', dataIndex: 'grossAmount', width: 100 },
  { title: '优惠金额', dataIndex: 'discountAmount', width: 100 },
  { title: '优惠券', dataIndex: 'couponName', width: 150 },
  { title: '实付金额', dataIndex: 'totalAmount', width: 100 },
  { title: '支付方式', dataIndex: 'paymentMethod', width: 120 },
  { title: '支付状态', slotName: 'paymentStatus', width: 110 },
  { title: '状态', slotName: 'status' },
  { title: '下单时间', slotName: 'createTime' },
  { title: '操作', slotName: 'actions', width: 180 }
]

const itemColumns = [
  { title: '商品ID', dataIndex: 'id', width: 90 },
  { title: '商品名称', dataIndex: 'name' },
  { title: '分类', dataIndex: 'category' },
  { title: '价格', dataIndex: 'price' },
  { title: '状态', slotName: 'status' },
  { title: '备注', dataIndex: 'remark' },
  { title: '操作', slotName: 'actions', width: 160 }
]

const paymentQrCodeUrl = computed(() => paymentDialog.outTradeNo
  ? getWechatPaymentQrCodeUrl(paymentDialog.outTradeNo) : '')

async function loadBaseData() {
  members.value = await getMemberList({})
  availableItems.value = await getAvailableFoodItems()
}

async function loadItems() {
  items.value = await getFoodItems(itemQuery)
  availableItems.value = await getAvailableFoodItems()
}

async function loadOrders() {
  orders.value = await getFoodOrders(orderQuery)
}

async function loadAll() {
  await loadBaseData()
  await loadItems()
  await loadOrders()
}

function resetOrderQuery() {
  orderQuery.customerName = ''
  orderQuery.status = ''
  orderQuery.paymentMethod = ''
  loadOrders()
}

function resetItemQuery() {
  itemQuery.name = ''
  itemQuery.category = ''
  itemQuery.status = ''
  loadItems()
}

function changeTab(tab) {
  if (route.query.tab !== tab) router.replace({ path: route.path, query: { ...route.query, tab } })
}

async function submitOrder() {
  if (orderForm.customerType === '会员' && !orderForm.memberId) {
    Message.error('请选择会员')
    return
  }
  if (!orderForm.foodItemId) {
    Message.error('请选择商品')
    return
  }
  const selectedMethod = orderForm.paymentMethod
  const alipayWindow = selectedMethod === '支付宝支付' ? window.open('about:blank', '_blank') : null
  try {
    const result = await addFoodOrder(orderForm)
    Object.assign(orderForm, { customerType: '会员', memberId: null, customerName: '', foodItemId: null, quantity: 1, paymentMethod: '余额支付' })
    if (selectedMethod === '余额支付' || result.status === '已支付') {
      if (alipayWindow) alipayWindow.close()
      Message.success('下单成功')
    } else {
      openPaymentDialog(result)
      if (selectedMethod === '支付宝支付') {
        const cashierUrl = getAlipayPaymentPageUrl(result.outTradeNo)
        if (alipayWindow) alipayWindow.location.href = cashierUrl
        else Message.warning('收银台窗口被浏览器拦截，请在弹窗中重新打开')
      }
    }
    loadAll()
  } catch (error) {
    if (alipayWindow) alipayWindow.close()
  }
}

async function handleCancelOrder(id) {
  await cancelFoodOrder(id)
  Message.success('取消或退款申请已提交')
  loadOrders()
}

async function handleCompleteOrder(id) {
  await completeFoodOrder(id)
  Message.success('订单已完成')
  loadOrders()
}

function orderStatusColor(status) {
  if (status === '已下单') return 'blue'
  if (status === '已完成') return 'green'
  if (status === '待支付') return 'orange'
  if (status === '退款中') return 'orange'
  return 'gray'
}

function paymentStatusColor(status) {
  if (status === '已支付') return 'green'
  if (status === '部分退款' || status === '退款中') return 'orange'
  if (status === '已退款' || status === '已关闭') return 'gray'
  return 'blue'
}

function openPaymentDialog(payment) {
  Object.assign(paymentDialog, {
    visible: true,
    outTradeNo: payment.outTradeNo,
    paymentMethod: payment.paymentMethod,
    amount: payment.amount,
    status: payment.status || '待支付'
  })
  startPaymentPolling()
}

function resumePayment(order) {
  openPaymentDialog({
    outTradeNo: order.paymentOutTradeNo,
    paymentMethod: order.paymentMethod,
    amount: order.paymentAmount || order.totalAmount,
    status: order.paymentStatus || '待支付'
  })
}

function openAlipayCashier() {
  if (paymentDialog.outTradeNo) {
    window.open(getAlipayPaymentPageUrl(paymentDialog.outTradeNo), '_blank')
  }
}

function startPaymentPolling() {
  stopPaymentPolling()
  paymentPollTimer = window.setInterval(() => checkPaymentStatus(false), 3000)
}

function stopPaymentPolling() {
  if (paymentPollTimer) window.clearInterval(paymentPollTimer)
  paymentPollTimer = null
}

async function checkPaymentStatus(manual) {
  if (!paymentDialog.outTradeNo || paymentChecking.value) return
  paymentChecking.value = true
  try {
    const result = await getPaymentStatus(paymentDialog.outTradeNo)
    paymentDialog.status = result.status
    if (result.status === '已支付') {
      stopPaymentPolling()
      paymentDialog.visible = false
      Message.success('支付成功，订单已提交')
      await loadAll()
    } else if (result.status === '已关闭') {
      stopPaymentPolling()
      Message.error('支付单已超时关闭')
      await loadOrders()
    } else if (manual) {
      Message.info('暂未查询到支付成功')
    }
  } catch (error) {
    stopPaymentPolling()
  } finally {
    paymentChecking.value = false
  }
}

function openAddItem() {
  Object.assign(itemForm, { id: null, name: '', category: '饮料', price: 1, status: '上架', remark: '' })
  itemVisible.value = true
}

function openEditItem(record) {
  Object.assign(itemForm, { ...record, category: record.category || '其他' })
  itemVisible.value = true
}

async function saveItem() {
  if (itemForm.id) {
    await updateFoodItem(itemForm)
  } else {
    await addFoodItem(itemForm)
  }
  Message.success('保存成功')
  itemVisible.value = false
  loadItems()
}

async function handleDeleteItem(id) {
  await deleteFoodItem(id)
  Message.success('删除成功')
  loadItems()
}

onMounted(loadAll)

onBeforeUnmount(stopPaymentPolling)

watch(() => route.query.tab, (tab) => {
  activeTab.value = tab === 'item' ? 'item' : 'order'
})

watch(() => orderForm.customerType, (customerType) => {
  if (customerType === '散客' && orderForm.paymentMethod === '余额支付') {
    orderForm.paymentMethod = '微信支付'
  }
})
</script>

<style scoped>
.admin-payment-dialog {
  display: grid;
  gap: 14px;
  text-align: center;
}

.admin-payment-amount {
  padding: 14px;
  border: 1px solid #e5e8ef;
  border-radius: 6px;
  background: #f7f8fa;
}

.admin-payment-amount span,
.admin-payment-amount strong,
.admin-payment-amount small {
  display: block;
}

.admin-payment-amount strong {
  margin: 4px 0;
  font-size: 28px;
}

.admin-payment-amount small,
.admin-payment-dialog p {
  color: #6b778c;
  overflow-wrap: anywhere;
}

.admin-payment-qr {
  width: 240px;
  aspect-ratio: 1;
  margin: 0 auto;
  border: 1px solid #d9dee8;
  object-fit: contain;
}

.admin-payment-status {
  min-height: 42px;
  padding: 0 12px;
  border: 1px solid #e5e8ef;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
