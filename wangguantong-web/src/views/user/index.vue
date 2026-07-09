<template>
  <div class="user-terminal">
    <header class="terminal-header">
      <div class="brand">
        <div class="brand-icon">W</div>
        <div>
          <div class="brand-name">网管通</div>
          <div class="brand-sub">电竞网吧自助终端</div>
        </div>
      </div>

      <nav class="terminal-nav">
        <button :class="{ active: activeTab === 'seat' }" @click="activeTab = 'seat'">座位上机</button>
        <button :class="{ active: activeTab === 'food' }" @click="activeTab = 'food'">自助点餐</button>
        <button :class="{ active: activeTab === 'records' }" @click="activeTab = 'records'">消费记录</button>
      </nav>

      <div class="header-actions">
        <span>{{ currentMember?.name || '用户' }}</span>
        <a-button @click="openPasswordModal">修改密码</a-button>
        <a-button @click="logout">退出</a-button>
      </div>
    </header>

    <main class="terminal-main">
      <section class="hero-panel">
        <div class="hero-copy">
          <div class="hero-kicker">{{ currentMember?.memberLevel || '会员' }} · {{ discountText }}</div>
          <h1>{{ pageTitle }}</h1>
          <p>{{ pageSubtitle }}</p>
        </div>

        <div class="account-strip">
          <div class="account-main">
            <div class="avatar">{{ currentMember?.name?.slice(0, 1) || '用' }}</div>
            <div>
              <strong>{{ currentMember?.name || '-' }}</strong>
              <span>{{ currentMember?.phone || '-' }}</span>
            </div>
          </div>
          <div class="account-balance">
            <span>余额</span>
            <strong>￥{{ currentMember?.balance || 0 }}</strong>
          </div>
          <div class="account-chips">
            <span class="account-chip">{{ currentMember?.status || '-' }}</span>
            <span v-if="currentRunningRecord" class="account-chip online-chip">上机中</span>
          </div>
        </div>
      </section>

      <section v-if="activeTab === 'seat'" class="terminal-grid">
        <div class="primary-panel seat-panel">
          <div class="panel-head">
            <div>
              <h2>{{ currentRunningRecord ? '当前上机电脑' : '电脑座位图' }}</h2>
              <p>{{ currentRunningRecord ? '您正在使用以下电脑，可查看实时上机时长并自助下机。' : '选择空闲机位，立即上机或预约锁定。' }}</p>
            </div>
            <div v-if="!currentRunningRecord" class="seat-legend">
              <span><i class="dot free"></i>空闲</span>
              <span><i class="dot using"></i>使用中</span>
              <span><i class="dot reserved"></i>预约</span>
              <span><i class="dot repair"></i>维修</span>
            </div>
          </div>

          <div v-if="currentRunningRecord" class="current-online-card">
            <div class="current-online-main">
              <div>
                <div class="current-label">正在使用</div>
                <div class="current-computer">{{ currentRunningRecord.computerNo }}</div>
                <div class="current-meta">{{ activeComputer?.area || '当前机位' }} · {{ formatDateTime(currentRunningRecord.startTime) }}</div>
              </div>
              <a-tag color="blue">进行中</a-tag>
            </div>

            <div class="current-metrics">
              <div>
                <span>实时上机时长</span>
                <strong>{{ runningDurationText }}</strong>
              </div>
              <div>
                <span>计费小时</span>
                <strong>{{ chargeHours }} 小时</strong>
              </div>
              <div>
                <span>当前消费</span>
                <strong>￥{{ estimatedCurrentAmount }}</strong>
              </div>
              <div>
                <span>账户余额</span>
                <strong>￥{{ currentMember?.balance || 0 }}</strong>
              </div>
            </div>

            <div v-if="!balanceEnough" class="current-warning">
              <span>!</span>
              <div>
                <strong>余额提醒</strong>
                <p>{{ balanceWarningText }}</p>
              </div>
            </div>

            <div class="current-actions">
              <a-popconfirm content="确认下机结算吗？" @ok="submitEndOnline">
                <a-button type="primary">下机结算</a-button>
              </a-popconfirm>
            </div>
          </div>

          <div v-else class="area-list">
            <section v-for="area in areaGroups" :key="area.name" class="area-block">
              <div class="area-title">
                <strong>{{ area.name }}</strong>
                <span>{{ area.computers.length }} 台设备</span>
              </div>
              <div class="seat-grid">
                <article v-for="computer in area.computers" :key="computer.id" class="seat-card" :class="seatClass(computer.status)">
                  <div class="seat-top">
                    <span>{{ computer.status }}</span>
                    <small>{{ computer.area }}</small>
                  </div>
                  <div class="seat-no">{{ computer.computerNo }}</div>
                  <div class="seat-price">￥{{ computer.pricePerHour }}/小时</div>
                  <div v-if="computer.status === '空闲'" class="seat-actions">
                    <a-button type="primary" @click="submitOnline(computer)">上机</a-button>
                    <a-button @click="openReservation(computer)">预约</a-button>
                  </div>
                  <a-button v-else disabled>不可操作</a-button>
                </article>
              </div>
            </section>
          </div>
        </div>

        <aside class="side-panel">
          <div class="side-title">{{ currentRunningRecord ? '自助点餐' : '机位概览' }}</div>
          <div v-if="currentRunningRecord" class="side-food-list">
            <div v-if="foodItems.length === 0" class="side-empty">暂无可售商品</div>
            <section v-for="group in sideFoodGroups" :key="group.name" class="side-food-group">
              <div class="side-food-category">{{ group.name }}</div>
              <div class="side-food-items">
                <article v-for="item in group.items" :key="item.id" class="side-food-card">
                  <div>
                    <strong>{{ item.name }}</strong>
                    <span>{{ item.remark || '网吧商品' }}</span>
                  </div>
                  <div class="side-food-action">
                    <b>￥{{ item.price }}</b>
                    <div class="quantity-stepper">
                      <button type="button" :disabled="!foodCart[item.id]" @click="decreaseCartItem(item.id)">-</button>
                      <span>{{ foodCart[item.id] || 0 }}</span>
                      <button type="button" @click="addCartItem(item)">+</button>
                    </div>
                  </div>
                </article>
              </div>
            </section>

            <div class="cart-box">
              <div class="cart-head">
                <strong>购物车</strong>
                <button type="button" :disabled="cartItems.length === 0" @click="clearCart">清空</button>
              </div>
              <div v-if="cartItems.length === 0" class="cart-empty">请选择商品数量加入购物车</div>
              <div v-else class="cart-list">
                <div v-for="item in cartItems" :key="item.id" class="cart-row">
                  <span>{{ item.name }}</span>
                  <div>
                    <em>x {{ item.quantity }}</em>
                    <b>￥{{ item.total }}</b>
                  </div>
                </div>
              </div>
              <div class="cart-footer">
                <span>合计：<strong>￥{{ cartTotal }}</strong></span>
                <a-button type="primary" size="small" :disabled="cartItems.length === 0" @click="submitCartOrder">结算</a-button>
              </div>
            </div>
          </div>
          <div v-else class="status-list">
            <div><span class="line free"></span><small>空闲电脑</small><strong>{{ freeCount }}</strong></div>
            <div><span class="line using"></span><small>使用中</small><strong>{{ usingCount }}</strong></div>
            <div><span class="line reserved"></span><small>预约锁定</small><strong>{{ reservedCount }}</strong></div>
            <div><span class="line repair"></span><small>维修中</small><strong>{{ repairCount }}</strong></div>
          </div>
          <div class="notice-box">
            <strong>{{ currentRunningRecord ? '点餐提示' : '自助提示' }}</strong>
            <p>{{ currentRunningRecord ? '下单后费用会从会员余额扣除，余额不足时请前往前台充值。' : '上机费用按会员等级自动折扣；余额不足时请前往前台充值。' }}</p>
          </div>
        </aside>
      </section>

      <section v-if="activeTab === 'food'" class="primary-panel">
        <div class="panel-head">
          <div>
            <h2>自助点餐</h2>
            <p>选择商品后下单，费用将从会员余额扣除。</p>
          </div>
        </div>
        <div class="food-grid">
          <article v-for="item in foodItems" :key="item.id" class="food-card">
            <div class="food-image">{{ item.name.slice(0, 1) }}</div>
            <div class="food-category">{{ getFoodCategory(item) }}</div>
            <div class="food-name">{{ item.name }}</div>
            <div class="food-remark">{{ item.remark || '网吧商品' }}</div>
            <div class="food-bottom">
              <strong>￥{{ item.price }}</strong>
              <div class="quantity-stepper">
                <button type="button" :disabled="!foodCart[item.id]" @click="decreaseCartItem(item.id)">-</button>
                <span>{{ foodCart[item.id] || 0 }}</span>
                <button type="button" @click="addCartItem(item)">+</button>
              </div>
            </div>
          </article>
        </div>
        <div class="food-cart-panel">
          <div class="food-cart-head">
            <div>
              <strong>购物车</strong>
              <span>{{ cartItems.length === 0 ? '未选择商品' : `共 ${cartItemCount} 件商品` }}</span>
            </div>
            <div v-if="cartItems.length === 0" class="cart-empty">请选择商品数量加入购物车</div>
            <div class="food-cart-actions">
              <strong>￥{{ cartTotal }}</strong>
              <a-button :disabled="cartItems.length === 0" @click="clearCart">清空</a-button>
              <a-button type="primary" :disabled="cartItems.length === 0" @click="submitCartOrder">下单</a-button>
            </div>
          </div>
          <div v-if="cartItems.length > 0" class="food-cart-list">
            <div v-for="item in cartItems" :key="item.id" class="cart-row">
              <span>{{ item.name }}</span>
              <div>
                <em>x {{ item.quantity }}</em>
                <b>￥{{ item.total }}</b>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section v-if="activeTab === 'records'" class="primary-panel">
        <div class="panel-head">
          <div>
            <h2>我的消费记录</h2>
            <p>查看余额明细、上机记录和点餐订单。</p>
          </div>
          <div class="record-switch">
            <button :class="{ active: recordTab === 'balance' }" @click="recordTab = 'balance'">余额明细</button>
            <button :class="{ active: recordTab === 'online' }" @click="recordTab = 'online'">上机记录</button>
            <button :class="{ active: recordTab === 'order' }" @click="recordTab = 'order'">订单记录</button>
          </div>
        </div>

        <a-table
          v-if="recordTab === 'balance'"
          class="no-wrap-table record-table"
          :columns="balanceColumns"
          :data="balanceDetails"
          row-key="createTime"
          :pagination="false"
          :scroll="{ x: 760 }"
        >
          <template #amount="{ record }">
            <span :class="Number(record.amount) >= 0 ? 'amount-plus' : 'amount-minus'">
              {{ Number(record.amount) >= 0 ? '+' : '' }}{{ record.amount }}
            </span>
          </template>
          <template #createTime="{ record }">
            {{ formatDateTime(record.createTime) }}
          </template>
        </a-table>

        <a-table
          v-if="recordTab === 'online'"
          class="no-wrap-table record-table"
          :columns="onlineColumns"
          :data="onlineRecords"
          row-key="id"
          :pagination="false"
          :scroll="{ x: 980 }"
        >
          <template #startTime="{ record }">
            {{ formatDateTime(record.startTime) }}
          </template>
          <template #endTime="{ record }">
            {{ formatDateTime(record.endTime) }}
          </template>
          <template #status="{ record }">
            <a-tag :color="record.status === '进行中' ? 'blue' : 'green'">{{ record.status }}</a-tag>
          </template>
        </a-table>

        <a-table
          v-if="recordTab === 'order'"
          class="no-wrap-table record-table"
          :columns="orderColumns"
          :data="orders"
          row-key="id"
          :pagination="false"
          :scroll="{ x: 920 }"
        >
          <template #createTime="{ record }">
            {{ formatDateTime(record.createTime) }}
          </template>
          <template #status="{ record }">
            <a-tag :color="record.status === '已下单' ? 'green' : 'gray'">{{ record.status }}</a-tag>
          </template>
        </a-table>
      </section>
    </main>

    <a-modal v-model:visible="reservationVisible" title="预约电脑" @ok="submitReservation">
      <a-form :model="reservationForm" layout="vertical">
        <a-form-item label="电脑编号">
          <a-input :model-value="selectedComputer?.computerNo" disabled />
        </a-form-item>
        <a-form-item label="预约会员">
          <a-input :model-value="currentMember ? `${currentMember.name}（${currentMember.phone}）` : '当前登录用户'" disabled />
        </a-form-item>
        <a-form-item label="预约时间">
          <a-date-picker v-model="reservationForm.reserveTime" show-time format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
          <div class="form-tip">只能预约当前时间起 1 小时内的时间，超时未上机会自动取消。</div>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:visible="passwordVisible" title="修改密码" @ok="submitPasswordChange">
      <a-form :model="passwordForm" layout="vertical">
        <a-form-item label="原密码">
          <a-input-password v-model="passwordForm.oldPassword" />
        </a-form-item>
        <a-form-item label="新密码">
          <a-input-password v-model="passwordForm.newPassword" />
        </a-form-item>
        <a-form-item label="确认新密码">
          <a-input-password v-model="passwordForm.confirmPassword" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { getComputerList } from '../../api/computer'
import { getAvailableFoodItems, addFoodOrder } from '../../api/food'
import { getMemberList } from '../../api/member'
import { addReservation } from '../../api/reservation'
import { endOnline, startOnline } from '../../api/online'
import { changeUserPassword, getUserBalanceDetail, getUserOnlineRecords, getUserOrders } from '../../api/user'
import { formatDateTime } from '../../utils/format'

const router = useRouter()
const activeTab = ref('seat')
const recordTab = ref('balance')
const currentMember = ref(getStoredUser())
const computers = ref([])
const foodItems = ref([])
const balanceDetails = ref([])
const onlineRecords = ref([])
const orders = ref([])
const foodQuantity = reactive({})
const foodCart = reactive({})
const now = ref(new Date())
const reservationVisible = ref(false)
const passwordVisible = ref(false)
const selectedComputer = ref(null)
const reservationForm = reactive({ reserveTime: '' })
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
let timer = null
let runningRefreshTimer = null

const balanceColumns = [
  { title: '类型', dataIndex: 'type', width: 100 },
  { title: '说明', dataIndex: 'description', width: 260 },
  { title: '金额', slotName: 'amount', width: 120 },
  { title: '时间', slotName: 'createTime', width: 200 }
]

const onlineColumns = [
  { title: '电脑编号', dataIndex: 'computerNo', width: 120 },
  { title: '开始时间', slotName: 'startTime', width: 190 },
  { title: '下机时间', slotName: 'endTime', width: 190 },
  { title: '已扣金额', dataIndex: 'totalAmount', width: 120 },
  { title: '当前应扣', dataIndex: 'currentAmount', width: 120 },
  { title: '状态', slotName: 'status', width: 110 }
]

const orderColumns = [
  { title: '商品', dataIndex: 'foodItemName', width: 160 },
  { title: '单价', dataIndex: 'price', width: 100 },
  { title: '数量', dataIndex: 'quantity', width: 90 },
  { title: '总金额', dataIndex: 'totalAmount', width: 120 },
  { title: '状态', slotName: 'status', width: 110 },
  { title: '下单时间', slotName: 'createTime', width: 200 }
]

function getStoredUser() {
  const text = localStorage.getItem('user')
  return text ? JSON.parse(text) : null
}

const discountText = computed(() => {
  if (!currentMember.value) return ''
  if (currentMember.value.memberLevel === '钻石会员') return '上机 8 折'
  if (currentMember.value.memberLevel === '黄金会员') return '上机 9 折'
  if (currentMember.value.memberLevel === '普通会员') return '上机不打折'
  return '无折扣'
})

const pageTitle = computed(() => {
  if (activeTab.value === 'food') return '自助点餐'
  if (activeTab.value === 'records') return '消费记录'
  return '座位与上机'
})

const pageSubtitle = computed(() => {
  if (activeTab.value === 'food') return '饮料、泡面、热食一键下单'
  if (activeTab.value === 'records') return '余额明细、上机记录、订单记录'
  if (currentRunningRecord.value) return '当前已有电脑正在使用，可在本页自助下机'
  return '选择空闲电脑，自助上机或预约'
})

const currentRunningRecord = computed(() => onlineRecords.value.find((item) => item.status === '进行中'))
const activeComputer = computed(() => computers.value.find((item) => item.id === currentRunningRecord.value?.computerId))
const groupedFoodItems = computed(() => {
  const map = new Map()
  foodItems.value.forEach((item) => {
    const category = getFoodCategory(item)
    if (!map.has(category)) map.set(category, [])
    map.get(category).push(item)
  })
  return Array.from(map.entries()).map(([name, items]) => ({ name, items }))
})
const sideFoodGroups = computed(() => groupedFoodItems.value.map((group) => ({
  name: group.name,
  items: group.items.slice(0, 4)
})))
const cartItems = computed(() => foodItems.value
  .filter((item) => Number(foodCart[item.id] || 0) > 0)
  .map((item) => {
    const quantity = Number(foodCart[item.id] || 0)
    const total = (Number(item.price || 0) * quantity).toFixed(2)
    return { ...item, quantity, total }
  }))
const cartItemCount = computed(() => cartItems.value.reduce((sum, item) => sum + item.quantity, 0))
const cartTotal = computed(() => cartItems.value.reduce((sum, item) => sum + Number(item.total), 0).toFixed(2))
const freeCount = computed(() => computers.value.filter((item) => item.status === '空闲').length)
const usingCount = computed(() => computers.value.filter((item) => item.status === '使用中').length)
const reservedCount = computed(() => computers.value.filter((item) => item.status === '预约锁定').length)
const repairCount = computed(() => computers.value.filter((item) => item.status === '维修中').length)

const runningMinutes = computed(() => {
  if (!currentRunningRecord.value?.startTime) return 0
  const start = new Date(currentRunningRecord.value.startTime)
  const diff = Math.floor((now.value.getTime() - start.getTime()) / 60000)
  return diff < 0 ? 0 : diff
})

const runningDurationText = computed(() => {
  const minutes = runningMinutes.value
  const hours = Math.floor(minutes / 60)
  const restMinutes = minutes % 60
  if (hours <= 0) return `${restMinutes} 分钟`
  return `${hours} 小时 ${restMinutes} 分钟`
})

const chargeHours = computed(() => Math.max(1, Math.ceil(runningMinutes.value / 60)))

const discountRate = computed(() => {
  if (currentRunningRecord.value?.discountRate) return Number(currentRunningRecord.value.discountRate)
  if (currentMember.value?.memberLevel === '钻石会员') return 0.8
  if (currentMember.value?.memberLevel === '黄金会员') return 0.9
  if (currentMember.value?.memberLevel === '普通会员') return 1
  return 1
})

const estimatedCurrentAmount = computed(() => {
  const price = Number(activeComputer.value?.pricePerHour || 0)
  if (!price && currentRunningRecord.value?.currentAmount !== undefined) {
    return Number(currentRunningRecord.value.currentAmount).toFixed(2)
  }
  return (price * chargeHours.value * discountRate.value).toFixed(2)
})

const pendingChargeAmount = computed(() => {
  const price = Number(activeComputer.value?.pricePerHour || 0)
  const nextAmount = price
    ? price * (chargeHours.value + 1) * discountRate.value
    : Number(estimatedCurrentAmount.value || 0)
  const paidAmount = Number(currentRunningRecord.value?.totalAmount || 0)
  const pendingAmount = nextAmount - paidAmount
  return pendingAmount > 0 ? pendingAmount : 0
})

const balanceEnough = computed(() => Number(currentMember.value?.balance || 0) >= pendingChargeAmount.value)

const balanceWarningText = computed(() => {
  if (currentRunningRecord.value?.warningMessage) return currentRunningRecord.value.warningMessage
  return '当前余额不足以支付下一计费小时，请及时前往前台充值。'
})

const areaGroups = computed(() => {
  const map = new Map()
  computers.value.forEach((computer) => {
    const area = computer.area || '未分区'
    if (!map.has(area)) map.set(area, [])
    map.get(area).push(computer)
  })
  return Array.from(map.entries()).map(([name, list]) => ({
    name,
    computers: list.sort((a, b) => String(a.computerNo).localeCompare(String(b.computerNo)))
  }))
})

function seatClass(status) {
  if (status === '空闲') return 'seat-free'
  if (status === '使用中') return 'seat-using'
  if (status === '预约锁定') return 'seat-reserved'
  return 'seat-repair'
}

function getFoodCategory(item) {
  if (item.category) return item.category
  const text = `${item.name || ''}${item.remark || ''}`
  if (/可乐|雪碧|水|茶|咖啡|饮|奶|汁/.test(text)) return '饮料'
  if (/泡面|饭|面|餐|肠|热狗|汉堡/.test(text)) return '餐食'
  if (/薯片|饼干|糖|巧克力|零食/.test(text)) return '零食'
  return '其他'
}

async function loadData() {
  computers.value = await getComputerList({})
  foodItems.value = await getAvailableFoodItems()
  foodItems.value.forEach((item) => {
    if (!foodQuantity[item.id]) foodQuantity[item.id] = 1
  })
  await loadUserRecords()
}

async function loadUserRecords() {
  if (!currentMember.value?.id) return
  onlineRecords.value = await getUserOnlineRecords(currentMember.value.id)
  balanceDetails.value = await getUserBalanceDetail(currentMember.value.id)
  orders.value = await getUserOrders(currentMember.value.id)
}

async function refreshRunningData() {
  if (!currentMember.value?.id) return
  computers.value = await getComputerList({})
  await loadUserRecords()
  await refreshCurrentMember()
}

async function refreshCurrentMember() {
  if (!currentMember.value?.phone) return
  const list = await getMemberList({ phone: currentMember.value.phone })
  const member = list.find((item) => item.phone === currentMember.value.phone)
  if (member) {
    currentMember.value = member
    localStorage.setItem('user', JSON.stringify(member))
  }
}

function logout() {
  localStorage.removeItem('user')
  router.push('/user/login')
}

function openPasswordModal() {
  Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
  passwordVisible.value = true
}

async function submitPasswordChange() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    Message.error('请完整填写密码信息')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    Message.error('两次输入的新密码不一致')
    return
  }
  const member = await changeUserPassword({
    memberId: String(currentMember.value.id),
    oldPassword: passwordForm.oldPassword,
    newPassword: passwordForm.newPassword
  })
  currentMember.value = member
  localStorage.setItem('user', JSON.stringify(member))
  passwordVisible.value = false
  Message.success('密码修改成功')
}

async function submitFoodOrder(item) {
  if (!checkUserCanOperate()) return
  await addFoodOrder({
    customerType: '会员',
    memberId: currentMember.value.id,
    customerName: currentMember.value.name,
    foodItemId: item.id,
    quantity: foodQuantity[item.id] || 1
  })
  Message.success('下单成功')
  await refreshCurrentMember()
  await loadUserRecords()
}

function addCartItem(item) {
  if (!checkUserCanOperate()) return
  foodCart[item.id] = Number(foodCart[item.id] || 0) + 1
}

function decreaseCartItem(itemId) {
  const current = Number(foodCart[itemId] || 0)
  if (current <= 1) {
    delete foodCart[itemId]
    return
  }
  foodCart[itemId] = current - 1
}

function clearCart() {
  Object.keys(foodCart).forEach((key) => {
    delete foodCart[key]
  })
}

async function submitCartOrder() {
  if (!checkUserCanOperate()) return
  if (cartItems.value.length === 0) {
    Message.warning('请先选择商品')
    return
  }
  if (Number(currentMember.value?.balance || 0) < Number(cartTotal.value)) {
    Message.error('会员余额不足，请先充值')
    return
  }
  for (const item of cartItems.value) {
    await addFoodOrder({
      customerType: '会员',
      memberId: currentMember.value.id,
      customerName: currentMember.value.name,
      foodItemId: item.id,
      quantity: item.quantity
    })
  }
  Message.success('点餐结算成功')
  clearCart()
  await refreshCurrentMember()
  await loadUserRecords()
}

function openReservation(computer) {
  if (!checkUserCanOperate()) return
  if (currentMember.value.userType !== '会员') {
    Message.error('散客不能预约，请先联系前台充值成为普通会员')
    return
  }
  selectedComputer.value = computer
  reservationForm.reserveTime = ''
  reservationVisible.value = true
}

async function submitOnline(computer) {
  if (!checkUserCanOperate()) return
  if (currentRunningRecord.value) {
    Message.warning('您已有正在使用的电脑，请先下机后再选择其他电脑')
    return
  }
  if (currentMember.value.userType !== '会员') {
    Message.error('散客不能自助上机，请先联系前台充值成为普通会员')
    return
  }
  await startOnline({ memberId: currentMember.value.id, computerId: computer.id })
  Message.success('上机成功')
  await refreshCurrentMember()
  await loadData()
}

async function submitEndOnline() {
  if (!currentRunningRecord.value) {
    Message.error('当前没有进行中的上机记录')
    return
  }
  await endOnline(currentRunningRecord.value.id)
  Message.success('下机结算成功')
  await refreshCurrentMember()
  await loadData()
}

async function submitReservation() {
  if (!selectedComputer.value || !currentMember.value) {
    Message.error('请选择电脑')
    return
  }
  if (!validateReservationTime(reservationForm.reserveTime)) return
  await addReservation({
    memberId: currentMember.value.id,
    computerId: selectedComputer.value.id,
    reserveTime: reservationForm.reserveTime
  })
  Message.success('预约成功')
  reservationVisible.value = false
  selectedComputer.value = null
  await loadData()
}

function validateReservationTime(value) {
  if (!value) {
    Message.error('请选择预约时间')
    return false
  }
  const reserveTime = new Date(String(value).replace(' ', 'T')).getTime()
  const nowTime = Date.now()
  if (Number.isNaN(reserveTime)) {
    Message.error('预约时间格式不正确')
    return false
  }
  if (reserveTime < nowTime) {
    Message.error('预约时间不能早于当前时间')
    return false
  }
  if (reserveTime > nowTime + 60 * 60 * 1000) {
    Message.error('用户预约只能预约最近 1 小时')
    return false
  }
  return true
}

function checkUserCanOperate() {
  if (!currentMember.value) {
    Message.error('请先登录用户系统')
    router.push('/user/login')
    return false
  }
  if (currentMember.value.status !== '正常') {
    Message.error('用户状态不是正常，请联系前台')
    return false
  }
  return true
}

onMounted(() => {
  if (!currentMember.value) {
    router.push('/user/login')
    return
  }
  loadData()
  refreshCurrentMember()
  timer = window.setInterval(() => {
    now.value = new Date()
  }, 1000)
  runningRefreshTimer = window.setInterval(() => {
    if (currentRunningRecord.value) {
      refreshRunningData()
    }
  }, 30000)
})

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
  if (runningRefreshTimer) window.clearInterval(runningRefreshTimer)
})
</script>

<style scoped>
.user-terminal {
  min-height: 100vh;
  color: #e7eefc;
  background:
    linear-gradient(rgba(15, 23, 42, 0.32) 1px, transparent 1px),
    linear-gradient(90deg, rgba(15, 23, 42, 0.32) 1px, transparent 1px),
    radial-gradient(circle at 12% 18%, rgba(37, 99, 235, 0.42), transparent 31%),
    radial-gradient(circle at 88% 6%, rgba(20, 184, 166, 0.26), transparent 26%),
    radial-gradient(circle at 72% 84%, rgba(124, 58, 237, 0.22), transparent 30%),
    #020617;
  background-size: 34px 34px, 34px 34px, auto, auto, auto, auto;
  font-family: "Inter", "PingFang SC", "Microsoft YaHei", sans-serif;
}

.user-terminal::before {
  content: "";
  position: fixed;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(rgba(255, 255, 255, 0.035) 50%, transparent 50%);
  background-size: 100% 7px;
  opacity: 0.32;
  mix-blend-mode: overlay;
}

.terminal-header {
  position: sticky;
  top: 18px;
  z-index: 10;
  width: calc(100% - 48px);
  max-width: 1540px;
  min-height: 82px;
  margin: 18px auto 0;
  padding: 14px 18px;
  border: 1px solid rgba(148, 163, 184, 0.22);
  border-radius: 22px;
  background: rgba(2, 6, 23, 0.78);
  box-shadow: 0 24px 80px rgba(2, 6, 23, 0.42), inset 0 1px 0 rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(18px);
  display: grid;
  grid-template-columns: 290px minmax(360px, 1fr) auto;
  align-items: center;
  gap: 18px;
}

.brand,
.account-main,
.header-actions,
.seat-legend,
.seat-top {
  display: flex;
  align-items: center;
}

.brand {
  gap: 13px;
}

.brand-icon {
  width: 48px;
  height: 48px;
  border: 1px solid rgba(125, 211, 252, 0.55);
  border-radius: 15px;
  background: linear-gradient(135deg, #2563eb, #22c55e);
  box-shadow: 0 0 28px rgba(34, 197, 94, 0.32);
  display: grid;
  place-items: center;
  font-size: 24px;
  font-weight: 950;
}

.brand-name {
  color: #ffffff;
  font-size: 21px;
  font-weight: 950;
}

.brand-sub {
  margin-top: 3px;
  color: #8ba4c6;
  font-size: 13px;
}

.terminal-nav {
  height: 54px;
  padding: 5px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 16px;
  background: rgba(15, 23, 42, 0.72);
  display: grid;
  grid-template-columns: repeat(3, minmax(118px, 1fr));
  gap: 6px;
}

.terminal-nav button,
.record-switch button {
  border: 0;
  cursor: pointer;
  font: inherit;
  transition: background 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
}

.terminal-nav button {
  border-radius: 12px;
  background: transparent;
  color: #9fb0ca;
  font-weight: 850;
}

.terminal-nav button:hover {
  color: #ffffff;
  background: rgba(59, 130, 246, 0.18);
}

.terminal-nav button.active {
  color: #ffffff;
  background: linear-gradient(135deg, #2563eb, #0891b2);
  box-shadow: 0 0 24px rgba(14, 165, 233, 0.24);
}

.header-actions {
  gap: 12px;
  white-space: nowrap;
}

.header-actions span {
  padding: 10px 13px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 12px;
  color: #cbd5e1;
  background: rgba(15, 23, 42, 0.62);
  font-weight: 800;
}

.header-actions :deep(.arco-btn) {
  border-color: rgba(148, 163, 184, 0.22);
  color: #dbeafe;
  background: rgba(15, 23, 42, 0.72);
}

.terminal-main {
  position: relative;
  z-index: 1;
  max-width: 1540px;
  margin: 0 auto;
  padding: 26px 24px 42px;
}

.hero-panel {
  min-height: 238px;
  padding: 30px;
  border: 1px solid rgba(125, 211, 252, 0.24);
  border-radius: 28px;
  background:
    linear-gradient(120deg, rgba(15, 23, 42, 0.92), rgba(8, 47, 73, 0.84)),
    radial-gradient(circle at 82% 28%, rgba(34, 197, 94, 0.28), transparent 34%);
  box-shadow: 0 28px 90px rgba(2, 6, 23, 0.48), inset 0 1px 0 rgba(255, 255, 255, 0.08);
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(460px, 560px);
  align-items: center;
  gap: 30px;
  overflow: hidden;
}

.hero-kicker {
  width: fit-content;
  padding: 8px 12px;
  border: 1px solid rgba(103, 232, 249, 0.28);
  border-radius: 999px;
  color: #67e8f9;
  background: rgba(8, 145, 178, 0.15);
  font-size: 14px;
  font-weight: 950;
}

.hero-copy h1 {
  margin: 18px 0 12px;
  color: #ffffff;
  font-size: 58px;
  line-height: 1.02;
  letter-spacing: 0;
}

.hero-copy p {
  margin: 0;
  max-width: 620px;
  color: #b6c5da;
  font-size: 18px;
}

.account-strip {
  min-height: 148px;
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 22px;
  background: rgba(15, 23, 42, 0.56);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08);
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 14px;
  align-items: center;
}

.account-main {
  min-width: 0;
  gap: 14px;
}

.avatar {
  flex: 0 0 auto;
  width: 64px;
  height: 64px;
  border: 1px solid rgba(125, 211, 252, 0.55);
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.35), rgba(34, 197, 94, 0.25));
  color: #e0f2fe;
  box-shadow: 0 0 34px rgba(14, 165, 233, 0.22);
  display: grid;
  place-items: center;
  font-size: 28px;
  font-weight: 950;
}

.account-main strong,
.account-balance strong {
  display: block;
  color: #ffffff;
  font-size: 24px;
  overflow-wrap: anywhere;
}

.account-main span,
.account-balance span {
  display: block;
  margin-top: 5px;
  color: #8ba4c6;
}

.account-balance {
  min-width: 152px;
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(34, 197, 94, 0.12);
  text-align: right;
}

.account-balance strong {
  color: #86efac;
  font-size: 38px;
}

.account-chip {
  grid-column: 1 / -1;
  width: fit-content;
  padding: 8px 13px;
  border: 1px solid rgba(34, 197, 94, 0.28);
  border-radius: 999px;
  color: #bbf7d0;
  background: rgba(34, 197, 94, 0.16);
  font-weight: 900;
}

.terminal-grid {
  margin-top: 22px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 20px;
}

.primary-panel,
.side-panel {
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 24px;
  background: rgba(15, 23, 42, 0.74);
  box-shadow: 0 24px 78px rgba(2, 6, 23, 0.36), inset 0 1px 0 rgba(255, 255, 255, 0.06);
  color: #e7eefc;
  backdrop-filter: blur(16px);
}

.primary-panel {
  margin-top: 22px;
  padding: 24px;
}

.terminal-grid .primary-panel {
  margin-top: 0;
}

.side-panel {
  padding: 22px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 22px;
}

.panel-head h2 {
  margin: 0;
  color: #ffffff;
  font-size: 29px;
  letter-spacing: 0;
}

.panel-head p {
  margin: 7px 0 0;
  color: #8ba4c6;
}

.seat-legend {
  gap: 12px;
  color: #9fb0ca;
  flex-wrap: wrap;
}

.seat-legend span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.dot.free,
.line.free {
  background: #22c55e;
}

.dot.using,
.line.using {
  background: #38bdf8;
}

.dot.reserved,
.line.reserved {
  background: #a78bfa;
}

.dot.repair,
.line.repair {
  background: #fb923c;
}

.area-list {
  display: grid;
  gap: 24px;
}

.area-title {
  margin-bottom: 13px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.area-title strong {
  color: #f8fafc;
  font-size: 21px;
}

.area-title span {
  color: #8ba4c6;
}

.seat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.seat-card {
  min-height: 196px;
  padding: 18px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 20px;
  background: rgba(2, 6, 23, 0.58);
  display: grid;
  gap: 12px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.seat-card:hover {
  border-color: rgba(125, 211, 252, 0.45);
  box-shadow: 0 18px 48px rgba(2, 6, 23, 0.34);
}

.seat-top {
  justify-content: space-between;
  gap: 10px;
}

.seat-top span {
  padding: 5px 10px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 950;
  white-space: nowrap;
}

.seat-top small,
.seat-price {
  color: #8ba4c6;
}

.seat-no {
  color: #f8fafc;
  font-size: 38px;
  font-weight: 950;
  letter-spacing: 0;
}

.seat-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.seat-actions :deep(.arco-btn-primary),
.food-bottom :deep(.arco-btn-primary) {
  border-color: transparent;
  background: linear-gradient(135deg, #2563eb, #0891b2);
}

.seat-card :deep(.arco-btn-secondary),
.seat-card :deep(.arco-btn:not(.arco-btn-primary)) {
  border-color: rgba(148, 163, 184, 0.2);
  color: #dbeafe;
  background: rgba(148, 163, 184, 0.12);
}

.seat-card :deep(.arco-btn-disabled) {
  color: #64748b;
  background: rgba(148, 163, 184, 0.09);
}

.seat-free {
  border-color: rgba(34, 197, 94, 0.55);
  background: linear-gradient(180deg, rgba(20, 83, 45, 0.44), rgba(2, 6, 23, 0.62));
  box-shadow: inset 0 1px 0 rgba(187, 247, 208, 0.16);
}

.seat-free .seat-top span {
  color: #bbf7d0;
  background: rgba(34, 197, 94, 0.2);
}

.seat-using {
  border-color: rgba(56, 189, 248, 0.48);
  background: linear-gradient(180deg, rgba(30, 64, 175, 0.38), rgba(2, 6, 23, 0.62));
}

.seat-using .seat-top span {
  color: #bae6fd;
  background: rgba(14, 165, 233, 0.22);
}

.seat-reserved {
  border-color: rgba(167, 139, 250, 0.5);
  background: linear-gradient(180deg, rgba(91, 33, 182, 0.35), rgba(2, 6, 23, 0.62));
}

.seat-reserved .seat-top span {
  color: #ddd6fe;
  background: rgba(124, 58, 237, 0.22);
}

.seat-repair {
  border-color: rgba(251, 146, 60, 0.55);
  background: linear-gradient(180deg, rgba(154, 52, 18, 0.34), rgba(2, 6, 23, 0.62));
}

.seat-repair .seat-top span {
  color: #fed7aa;
  background: rgba(249, 115, 22, 0.22);
}

.side-title {
  color: #ffffff;
  font-size: 20px;
  font-weight: 950;
}

.status-list {
  margin-top: 18px;
  display: grid;
  gap: 12px;
}

.status-list div {
  padding: 14px;
  border: 1px solid rgba(148, 163, 184, 0.14);
  border-radius: 16px;
  background: rgba(2, 6, 23, 0.44);
  display: grid;
  grid-template-columns: 6px 1fr auto;
  gap: 10px;
  align-items: center;
}

.status-list small {
  color: #9fb0ca;
}

.status-list strong {
  color: #ffffff;
  font-size: 26px;
}

.line {
  width: 6px;
  height: 38px;
  border-radius: 999px;
}

.notice-box {
  margin-top: 18px;
  padding: 16px;
  border: 1px solid rgba(103, 232, 249, 0.22);
  border-radius: 16px;
  background: rgba(8, 145, 178, 0.13);
}

.notice-box strong {
  color: #67e8f9;
}

.notice-box p {
  margin: 8px 0 0;
  color: #b6c5da;
}

.food-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(270px, 1fr));
  gap: 16px;
}

.food-card {
  min-height: 218px;
  padding: 18px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 20px;
  background: linear-gradient(180deg, rgba(30, 41, 59, 0.82), rgba(2, 6, 23, 0.58));
  display: grid;
  gap: 12px;
}

.food-image {
  width: 68px;
  height: 68px;
  border: 1px solid rgba(125, 211, 252, 0.34);
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.35), rgba(34, 197, 94, 0.22));
  color: #e0f2fe;
  box-shadow: 0 0 26px rgba(14, 165, 233, 0.18);
  display: grid;
  place-items: center;
  font-size: 30px;
  font-weight: 950;
}

.food-name {
  color: #ffffff;
  font-size: 24px;
  font-weight: 950;
}

.food-remark {
  color: #8ba4c6;
}

.food-bottom {
  display: grid;
  grid-template-columns: 1fr 108px auto;
  gap: 10px;
  align-items: center;
}

.food-bottom strong {
  color: #86efac;
  font-size: 28px;
}

.food-bottom :deep(.arco-input-number) {
  background: rgba(15, 23, 42, 0.7);
}

.record-switch {
  padding: 5px;
  border: 1px solid rgba(148, 163, 184, 0.16);
  border-radius: 999px;
  background: rgba(2, 6, 23, 0.46);
  display: flex;
  gap: 4px;
}

.record-switch button {
  height: 38px;
  padding: 0 16px;
  border-radius: 999px;
  background: transparent;
  color: #9fb0ca;
}

.record-switch button:hover {
  color: #ffffff;
}

.record-switch button.active {
  color: #ffffff;
  background: linear-gradient(135deg, #2563eb, #0891b2);
  font-weight: 850;
}

.record-table {
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 16px;
  overflow: hidden;
}

.record-table :deep(.arco-table-container),
.record-table :deep(.arco-table),
.record-table :deep(.arco-table-th),
.record-table :deep(.arco-table-td) {
  background: rgba(2, 6, 23, 0.34);
  color: #dbeafe;
}

.record-table :deep(.arco-table-th) {
  color: #9fb0ca;
  background: rgba(15, 23, 42, 0.88);
}

.record-table :deep(.arco-table-border .arco-table-td),
.record-table :deep(.arco-table-border .arco-table-th),
.record-table :deep(.arco-table-td),
.record-table :deep(.arco-table-th) {
  border-color: rgba(148, 163, 184, 0.14);
}

.record-table :deep(.arco-table-tr:hover .arco-table-td),
.record-table :deep(.arco-table-tr-hover .arco-table-td),
.record-table :deep(.arco-table-tr-checked .arco-table-td) {
  color: #f8fafc !important;
  background: rgba(14, 165, 233, 0.14) !important;
}

.amount-plus {
  color: #22c55e;
  font-weight: 950;
}

.amount-minus {
  color: #f87171;
  font-weight: 950;
}

@media (max-width: 1180px) {
  .terminal-header,
  .hero-panel,
  .terminal-grid {
    grid-template-columns: 1fr;
  }

  .terminal-nav {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 760px) {
  .terminal-header {
    position: static;
    width: calc(100% - 24px);
    margin-top: 12px;
  }

  .terminal-main {
    padding-left: 12px;
    padding-right: 12px;
  }

  .terminal-nav,
  .account-strip,
  .food-bottom {
    grid-template-columns: 1fr;
    height: auto;
  }

  .hero-panel,
  .primary-panel,
  .side-panel {
    border-radius: 18px;
  }

  .hero-copy h1 {
    font-size: 36px;
  }

  .panel-head {
    flex-direction: column;
  }

  .record-switch {
    width: 100%;
    border-radius: 16px;
    display: grid;
    grid-template-columns: 1fr;
  }
}

/* Light customer terminal theme */
.user-terminal {
  color: #0f172a;
  background:
    linear-gradient(rgba(14, 165, 233, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(14, 165, 233, 0.08) 1px, transparent 1px),
    radial-gradient(circle at 12% 10%, rgba(34, 211, 238, 0.28), transparent 28%),
    radial-gradient(circle at 86% 4%, rgba(34, 197, 94, 0.18), transparent 24%),
    radial-gradient(circle at 72% 88%, rgba(99, 102, 241, 0.12), transparent 26%),
    #f5fbff;
}

.user-terminal::before {
  display: none;
}

.terminal-header {
  border: 1px solid rgba(203, 213, 225, 0.9);
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.1), inset 0 1px 0 rgba(255, 255, 255, 0.96);
}

.brand-icon {
  border-color: rgba(8, 145, 178, 0.2);
  background: linear-gradient(135deg, #0891b2, #22c55e);
  box-shadow: 0 12px 26px rgba(8, 145, 178, 0.22);
  color: #ffffff;
}

.brand-name {
  color: #0f172a;
}

.brand-sub {
  color: #64748b;
}

.terminal-nav {
  border-color: #dbeafe;
  background: #eef6ff;
}

.terminal-nav button {
  color: #475569;
}

.terminal-nav button:hover {
  color: #0369a1;
  background: #ffffff;
}

.terminal-nav button.active {
  color: #ffffff;
  background: linear-gradient(135deg, #0891b2, #22c55e);
  box-shadow: 0 10px 22px rgba(8, 145, 178, 0.22);
}

.header-actions span {
  border-color: #e2e8f0;
  color: #0f172a;
  background: #ffffff;
}

.header-actions :deep(.arco-btn) {
  border-color: #cbd5e1;
  color: #334155;
  background: #ffffff;
}

.header-actions :deep(.arco-btn),
.header-actions :deep(.arco-btn-secondary),
.header-actions :deep(.arco-btn-outline) {
  border-color: #cbd5e1 !important;
  color: #334155 !important;
  background: #ffffff !important;
}

.hero-panel {
  border-color: rgba(125, 211, 252, 0.54);
  background:
    linear-gradient(120deg, rgba(255, 255, 255, 0.96), rgba(236, 254, 255, 0.92)),
    radial-gradient(circle at 88% 24%, rgba(34, 197, 94, 0.18), transparent 34%);
  box-shadow: 0 22px 54px rgba(15, 23, 42, 0.1), inset 0 1px 0 rgba(255, 255, 255, 1);
}

.hero-kicker {
  border-color: rgba(8, 145, 178, 0.22);
  color: #0e7490;
  background: #ecfeff;
}

.hero-copy h1 {
  color: #0f172a;
}

.hero-copy p {
  color: #475569;
}

.account-strip {
  border-color: #dbeafe;
  background: #ffffff;
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.08);
}

.avatar {
  border-color: rgba(8, 145, 178, 0.22);
  background: linear-gradient(135deg, #ecfeff, #dcfce7);
  box-shadow: none;
  color: #0e7490;
}

.account-main strong,
.account-balance strong {
  color: #0f172a;
}

.account-main span,
.account-balance span {
  color: #64748b;
}

.account-balance {
  background: #f0fdf4;
}

.account-balance strong {
  color: #16a34a;
}

.account-chip {
  border-color: #bbf7d0;
  color: #15803d;
  background: #dcfce7;
}

.primary-panel,
.side-panel {
  border-color: #e2e8f0;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 18px 46px rgba(15, 23, 42, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.96);
  color: #0f172a;
}

.panel-head h2,
.area-title strong,
.seat-no,
.side-title,
.status-list strong,
.food-name {
  color: #0f172a;
}

.panel-head p,
.seat-legend,
.area-title span,
.seat-top small,
.seat-price,
.status-list small,
.food-remark {
  color: #64748b;
}

.seat-card {
  border-color: #dbe4f0;
  background: #ffffff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
}

.seat-card:hover {
  border-color: #67e8f9;
  box-shadow: 0 18px 36px rgba(8, 145, 178, 0.12);
}

.seat-card :deep(.arco-btn:not(.arco-btn-primary)) {
  border-color: #dbe4f0;
  color: #334155;
  background: #f8fafc;
}

.seat-card :deep(.arco-btn-disabled) {
  color: #94a3b8;
  background: #f1f5f9;
}

.seat-free {
  border-color: #86efac;
  background: linear-gradient(180deg, #f0fdf4, #ffffff);
}

.seat-free .seat-top span {
  color: #15803d;
  background: #dcfce7;
}

.seat-using {
  border-color: #93c5fd;
  background: linear-gradient(180deg, #eff6ff, #ffffff);
}

.seat-using .seat-top span {
  color: #1d4ed8;
  background: #dbeafe;
}

.seat-reserved {
  border-color: #c4b5fd;
  background: linear-gradient(180deg, #f5f3ff, #ffffff);
}

.seat-reserved .seat-top span {
  color: #6d28d9;
  background: #ede9fe;
}

.seat-repair {
  border-color: #fdba74;
  background: linear-gradient(180deg, #fff7ed, #ffffff);
}

.seat-repair .seat-top span {
  color: #c2410c;
  background: #ffedd5;
}

.status-list div {
  border-color: #e2e8f0;
  background: #f8fafc;
}

.notice-box {
  border-color: #bae6fd;
  background: #ecfeff;
}

.notice-box strong {
  color: #0e7490;
}

.notice-box p {
  color: #475569;
}

.food-card {
  border-color: #e2e8f0;
  background: linear-gradient(180deg, #ffffff, #f8fafc);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
}

.food-image {
  border-color: #bae6fd;
  background: linear-gradient(135deg, #ecfeff, #dcfce7);
  color: #0e7490;
  box-shadow: none;
}

.food-bottom strong {
  color: #dc2626;
}

.food-bottom :deep(.arco-input-number) {
  background: #ffffff;
}

.record-switch {
  border-color: #dbe4f0;
  background: #f1f5f9;
}

.record-switch button {
  color: #475569;
}

.record-switch button:hover {
  color: #0e7490;
  background: #ffffff;
}

.record-switch button.active {
  color: #ffffff;
  background: linear-gradient(135deg, #0891b2, #22c55e);
}

.record-table {
  border-color: #e2e8f0;
}

.record-table :deep(.arco-table-container),
.record-table :deep(.arco-table),
.record-table :deep(.arco-table-th),
.record-table :deep(.arco-table-td) {
  background: #ffffff;
  color: #0f172a;
}

.record-table :deep(.arco-table-th) {
  color: #475569;
  background: #f8fafc;
}

.record-table :deep(.arco-table-header),
.record-table :deep(.arco-table-header .arco-table),
.record-table :deep(.arco-table-header .arco-table-th),
.record-table :deep(.arco-table-th) {
  color: #475569 !important;
  background: #f8fafc !important;
}

.record-table :deep(.arco-table-border .arco-table-td),
.record-table :deep(.arco-table-border .arco-table-th),
.record-table :deep(.arco-table-td),
.record-table :deep(.arco-table-th) {
  border-color: #e2e8f0;
}

.record-table :deep(.arco-table-tr:hover .arco-table-td),
.record-table :deep(.arco-table-tr-hover .arco-table-td),
.record-table :deep(.arco-table-tr-checked .arco-table-td) {
  color: #0f172a !important;
  background: #ecfeff !important;
}

.amount-plus {
  color: #16a34a;
}

.amount-minus {
  color: #dc2626;
}

.current-online-card {
  display: grid;
  gap: 18px;
}

.current-online-main {
  padding: 20px;
  border: 1px solid #dbe3ef;
  border-radius: 10px;
  background: #f9fafb;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
}

.current-label {
  color: #6b7280;
  font-size: 13px;
  font-weight: 700;
}

.current-computer {
  margin-top: 6px;
  color: #111827;
  font-size: 42px;
  line-height: 1;
  font-weight: 800;
  letter-spacing: 0;
}

.current-meta {
  margin-top: 10px;
  color: #6b7280;
}

.current-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.current-metrics div {
  min-height: 84px;
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
}

.current-metrics span {
  display: block;
  color: #6b7280;
}

.current-metrics strong {
  display: block;
  margin-top: 8px;
  color: #111827;
  font-size: 22px;
  font-weight: 800;
  white-space: nowrap;
}

.current-warning {
  padding: 14px 16px;
  border: 1px solid #fed7aa;
  border-radius: 8px;
  background: #fff7ed;
  color: #9a3412;
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.current-warning > span {
  width: 18px;
  height: 18px;
  margin-top: 1px;
  border-radius: 50%;
  background: #f97316;
  color: #ffffff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 800;
}

.current-warning strong {
  display: block;
  font-weight: 800;
}

.current-warning p {
  margin: 4px 0 0;
  color: #9a3412;
}

.current-actions {
  display: flex;
  justify-content: flex-end;
}

.side-food-list {
  margin-top: 12px;
  display: grid;
  gap: 8px;
}

.side-food-group {
  display: grid;
  gap: 6px;
}

.side-food-category,
.food-category {
  width: fit-content;
  padding: 2px 7px;
  border: 1px solid #bfdbfe;
  border-radius: 999px;
  color: #1d4ed8;
  background: #eff6ff;
  font-size: 11px;
  font-weight: 800;
}

.side-food-items {
  display: grid;
  gap: 6px;
}

.side-food-card {
  min-height: 58px;
  padding: 9px 10px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f9fafb;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
  align-items: center;
}

.side-food-card strong {
  display: block;
  color: #111827;
  font-size: 14px;
  font-weight: 800;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.side-food-card span {
  display: block;
  margin-top: 2px;
  color: #6b7280;
  font-size: 11px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.side-food-action {
  display: grid;
  grid-template-columns: auto auto;
  gap: 7px;
  align-items: center;
}

.side-food-action b {
  color: #b91c1c;
  font-size: 14px;
  min-width: 42px;
}

.cart-head button {
  height: 28px;
  padding: 0 10px;
  border: 1px solid #bfdbfe;
  border-radius: 6px;
  color: #1d4ed8;
  background: #eff6ff;
  cursor: pointer;
  font-weight: 800;
}

.quantity-stepper {
  height: 30px;
  border: 1px solid #dbe3ef;
  border-radius: 6px;
  background: #ffffff;
  display: inline-grid;
  grid-template-columns: 28px 34px 28px;
  align-items: center;
  overflow: hidden;
}

.side-food-card .quantity-stepper {
  height: 28px;
  grid-template-columns: 26px 30px 26px;
}

.quantity-stepper.small {
  height: 28px;
  grid-template-columns: 26px 30px 26px;
}

.quantity-stepper button {
  width: 100%;
  height: 100%;
  border: 0;
  color: #1d4ed8;
  background: #eff6ff;
  cursor: pointer;
  font-weight: 900;
}

.quantity-stepper button:disabled,
.cart-head button:disabled {
  color: #9ca3af;
  background: #f3f4f6;
  cursor: not-allowed;
}

.quantity-stepper span {
  margin: 0;
  color: #111827;
  font-size: 13px;
  font-weight: 800;
  text-align: center;
}

.side-empty {
  padding: 18px;
  border: 1px dashed #d1d5db;
  border-radius: 8px;
  color: #6b7280;
  background: #f9fafb;
  text-align: center;
}

.cart-box,
.food-cart-panel {
  border: 1px solid #dbe3ef;
  border-radius: 10px;
  background: #ffffff;
}

.cart-box {
  padding: 12px;
  display: grid;
  gap: 10px;
}

.cart-head,
.cart-row,
.cart-footer,
.food-cart-actions {
  display: flex;
  align-items: center;
}

.cart-head,
.cart-footer {
  justify-content: space-between;
  gap: 12px;
}

.cart-head strong,
.food-cart-panel strong {
  color: #111827;
}

.cart-empty {
  padding: 12px;
  border-radius: 8px;
  color: #6b7280;
  background: #f9fafb;
  text-align: center;
}

.cart-list {
  display: grid;
  gap: 8px;
}

.cart-row {
  justify-content: space-between;
  gap: 8px;
  color: #374151;
}

.cart-row > div {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.cart-row b,
.cart-footer strong,
.food-cart-actions > strong {
  color: #b91c1c;
}

.cart-row em {
  color: #6b7280;
  font-style: normal;
  font-weight: 700;
}

.food-cart-panel {
  margin-top: 18px;
  padding: 16px;
  display: grid;
  gap: 12px;
  width: 100%;
  box-sizing: border-box;
  align-items: stretch;
  justify-items: stretch;
}

.food-cart-panel span {
  display: block;
  margin-top: 4px;
  color: #6b7280;
}

.food-cart-head {
  display: grid;
  grid-template-columns: minmax(160px, 220px) minmax(0, 1fr) auto;
  gap: 16px;
  align-items: center;
  width: 100%;
  min-width: 0;
  justify-self: stretch;
}

.food-cart-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 10px;
  padding-top: 10px;
  border-top: 1px solid #e5e7eb;
  width: 100%;
  min-width: 0;
  justify-self: stretch;
}

.food-cart-list .cart-row,
.food-cart-head .cart-empty {
  min-width: 0;
  min-height: 44px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f9fafb;
}

.food-cart-head .cart-empty {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.food-cart-actions {
  gap: 10px;
  justify-content: flex-end;
  white-space: nowrap;
}

@media (max-width: 860px) {
  .food-cart-head {
    grid-template-columns: 1fr;
  }

  .food-cart-actions {
    justify-content: flex-start;
  }
}

/* Business customer terminal theme */
.user-terminal {
  color: #1f2937;
  background: #f3f6fa;
  background-size: auto;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif;
}

.terminal-header {
  top: 14px;
  min-height: 74px;
  padding: 12px 16px;
  border: 1px solid #d8dee8;
  border-radius: 10px;
  background: #ffffff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}

.brand-icon {
  width: 42px;
  height: 42px;
  border: 0;
  border-radius: 8px;
  background: #1d4ed8;
  box-shadow: none;
  color: #ffffff;
  font-size: 21px;
}

.brand-name {
  color: #111827;
  font-size: 19px;
  font-weight: 800;
}

.brand-sub {
  color: #6b7280;
}

.terminal-nav {
  height: 48px;
  padding: 4px;
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  background: #f4f7fb;
}

.terminal-nav button {
  border-radius: 6px;
  color: #4b5563;
  font-weight: 700;
}

.terminal-nav button:hover {
  color: #1d4ed8;
  background: #ffffff;
}

.terminal-nav button.active {
  color: #ffffff;
  background: #1d4ed8;
  box-shadow: none;
}

.terminal-nav button.active {
  color: #ffffff !important;
  background: #1d4ed8 !important;
}

.header-actions span {
  border-color: #e5e7eb;
  border-radius: 8px;
  color: #374151;
  background: #f9fafb;
  font-weight: 700;
}

.header-actions :deep(.arco-btn),
.header-actions :deep(.arco-btn-secondary),
.header-actions :deep(.arco-btn-outline) {
  border-color: #d1d5db !important;
  border-radius: 6px !important;
  color: #374151 !important;
  background: #ffffff !important;
}

.terminal-main {
  max-width: 1440px;
  padding: 22px 24px 40px;
}

.hero-panel {
  min-height: 176px;
  padding: 26px;
  border: 1px solid #d8dee8;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
}

.hero-kicker {
  padding: 6px 10px;
  border: 1px solid #bfdbfe;
  border-radius: 6px;
  color: #1d4ed8;
  background: #eff6ff;
  font-size: 13px;
  font-weight: 800;
}

.hero-copy h1 {
  margin: 14px 0 8px;
  color: #111827;
  font-size: 42px;
  line-height: 1.12;
  font-weight: 800;
}

.hero-copy p {
  color: #6b7280;
  font-size: 16px;
}

.account-strip {
  min-height: 112px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #f9fafb;
  box-shadow: none;
}

.avatar {
  width: 54px;
  height: 54px;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  background: #eff6ff;
  color: #1d4ed8;
  font-size: 24px;
}

.account-main strong,
.account-balance strong {
  color: #111827;
}

.account-main strong {
  font-size: 22px;
}

.account-main span,
.account-balance span {
  color: #6b7280;
}

.account-balance {
  min-width: 138px;
  padding: 12px 14px;
  border: 1px solid #dcfce7;
  border-radius: 8px;
  background: #f0fdf4;
}

.account-balance strong {
  color: #15803d;
  font-size: 32px;
}

.account-chip {
  padding: 6px 10px;
  border-color: #bbf7d0;
  border-radius: 6px;
  color: #15803d;
  background: #dcfce7;
}

.account-chips {
  grid-column: 1 / -1;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.online-chip {
  border: 1px solid #bfdbfe;
  color: #1d4ed8;
  background: #eff6ff;
}

.terminal-grid {
  gap: 18px;
}

.primary-panel,
.side-panel {
  border: 1px solid #d8dee8;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
  color: #1f2937;
  backdrop-filter: none;
}

.primary-panel {
  padding: 22px;
}

.panel-head h2 {
  color: #111827;
  font-size: 25px;
  font-weight: 800;
}

.panel-head p,
.seat-legend,
.area-title span,
.seat-top small,
.seat-price,
.status-list small,
.food-remark {
  color: #6b7280;
}

.area-title strong,
.seat-no,
.side-title,
.status-list strong,
.food-name {
  color: #111827;
}

.area-title strong,
.side-title {
  font-size: 18px;
}

.seat-card {
  min-height: 172px;
  padding: 16px;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: none;
}

.seat-card:hover {
  border-color: #93c5fd;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.08);
}

.seat-no {
  font-size: 32px;
  font-weight: 800;
}

.seat-top span {
  border-radius: 4px;
  font-size: 12px;
  font-weight: 800;
}

.seat-actions :deep(.arco-btn-primary),
.food-bottom :deep(.arco-btn-primary) {
  border-color: #1d4ed8;
  background: #1d4ed8;
}

.user-terminal :deep(.arco-btn-primary:not(.arco-btn-disabled)) {
  border-color: #1d4ed8 !important;
  color: #ffffff !important;
  background: #1d4ed8 !important;
}

.seat-card :deep(.arco-btn:not(.arco-btn-primary)) {
  border-color: #d1d5db;
  color: #374151;
  background: #f9fafb;
}

.seat-card :deep(.arco-btn-disabled) {
  color: #9ca3af;
  background: #f3f4f6;
}

.seat-free {
  border-color: #86efac;
  background: #f7fff9;
}

.seat-free .seat-top span {
  color: #166534;
  background: #dcfce7;
}

.seat-using {
  border-color: #93c5fd;
  background: #f8fbff;
}

.seat-using .seat-top span {
  color: #1d4ed8;
  background: #dbeafe;
}

.seat-reserved {
  border-color: #c4b5fd;
  background: #fbfaff;
}

.seat-reserved .seat-top span {
  color: #6d28d9;
  background: #ede9fe;
}

.seat-repair {
  border-color: #fdba74;
  background: #fffaf5;
}

.seat-repair .seat-top span {
  color: #c2410c;
  background: #ffedd5;
}

.status-list div {
  padding: 13px;
  border-color: #e5e7eb;
  border-radius: 8px;
  background: #f9fafb;
}

.line {
  width: 5px;
  height: 32px;
}

.notice-box {
  border-color: #bfdbfe;
  border-radius: 8px;
  background: #eff6ff;
}

.notice-box strong {
  color: #1d4ed8;
}

.notice-box p {
  color: #4b5563;
}

.food-card {
  border-color: #e5e7eb;
  border-radius: 10px;
  background: #ffffff;
  box-shadow: none;
}

.food-image {
  width: 58px;
  height: 58px;
  border-color: #bfdbfe;
  border-radius: 8px;
  background: #eff6ff;
  color: #1d4ed8;
  box-shadow: none;
}

.food-bottom strong {
  color: #b91c1c;
}

.food-bottom {
  grid-template-columns: 1fr auto;
}

.quantity-stepper {
  width: 112px;
  height: 32px;
  display: inline-grid;
  grid-template-columns: 34px 44px 34px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  background: #ffffff;
  overflow: hidden;
}

.side-food-card .quantity-stepper {
  width: 92px;
  height: 28px;
  grid-template-columns: 28px 36px 28px;
}

.quantity-stepper button {
  border: 0;
  background: #ffffff;
  color: #1d4ed8;
  font-size: 16px;
  line-height: 1;
}

.quantity-stepper button:hover:not(:disabled) {
  background: #eff6ff;
}

.quantity-stepper button:disabled {
  color: #9ca3af;
  background: #f8fafc;
}

.quantity-stepper span {
  height: 100%;
  border-left: 1px solid #e5e7eb;
  border-right: 1px solid #e5e7eb;
  background: #ffffff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.record-switch {
  border-color: #dbe3ef;
  border-radius: 8px;
  background: #f3f6fa;
}

.record-switch button {
  border-radius: 6px;
  color: #4b5563;
}

.record-switch button:hover {
  color: #1d4ed8;
  background: #ffffff;
}

.record-switch button.active {
  color: #ffffff;
  background: #1d4ed8;
}

.record-table {
  border-color: #e5e7eb;
  border-radius: 8px;
}

.record-table :deep(.arco-table-container),
.record-table :deep(.arco-table),
.record-table :deep(.arco-table-th),
.record-table :deep(.arco-table-td) {
  background: #ffffff;
  color: #1f2937;
}

.record-table :deep(.arco-table-header),
.record-table :deep(.arco-table-header .arco-table),
.record-table :deep(.arco-table-header .arco-table-th),
.record-table :deep(.arco-table-th) {
  color: #374151 !important;
  background: #f9fafb !important;
}

.record-table :deep(.arco-table-border .arco-table-td),
.record-table :deep(.arco-table-border .arco-table-th),
.record-table :deep(.arco-table-td),
.record-table :deep(.arco-table-th) {
  border-color: #e5e7eb;
}

.record-table :deep(.arco-table-tr:hover .arco-table-td),
.record-table :deep(.arco-table-tr-hover .arco-table-td),
.record-table :deep(.arco-table-tr-checked .arco-table-td) {
  color: #111827 !important;
  background: #f3f6fa !important;
}

.current-online-main {
  border-color: #dbe3ef;
  border-radius: 10px;
  background: #f9fafb;
}

.current-computer,
.current-metrics strong {
  color: #111827;
}

.current-label,
.current-meta,
.current-metrics span {
  color: #6b7280;
}

.current-metrics div {
  border-color: #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
}

.side-food-card {
  border-color: #e5e7eb;
  background: #f9fafb;
}

.amount-plus {
  color: #15803d;
}

.amount-minus {
  color: #dc2626;
}

.form-tip {
  margin-top: 6px;
  color: #6b7280;
  font-size: 12px;
  line-height: 1.5;
}

/* Final full-width ordering layout overrides */
.primary-panel > .food-grid {
  width: 100%;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
}

.primary-panel > .food-grid > .food-card {
  width: 100%;
  min-width: 0;
}

.primary-panel > .food-cart-panel {
  width: 100%;
  box-sizing: border-box;
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  align-items: stretch;
  justify-items: stretch;
}

.primary-panel > .food-cart-panel .food-cart-head {
  width: 100%;
  min-width: 0;
  display: grid;
  grid-template-columns: minmax(140px, 190px) minmax(0, 1fr) auto;
  align-items: center;
  justify-self: stretch;
}

.primary-panel > .food-cart-panel .cart-empty {
  width: 100%;
  box-sizing: border-box;
}

.primary-panel > .food-cart-panel .food-cart-list {
  width: 100%;
  min-width: 0;
  justify-self: stretch;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.primary-panel > .food-cart-panel .food-cart-actions {
  justify-self: end;
}

@media (max-width: 980px) {
  .current-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 860px) {
  .primary-panel > .food-cart-panel .food-cart-head {
    grid-template-columns: 1fr;
  }

  .primary-panel > .food-cart-panel .food-cart-actions {
    justify-self: start;
  }
}

@media (max-width: 560px) {
  .current-online-main,
  .current-actions {
    display: block;
  }

  .current-metrics {
    grid-template-columns: 1fr;
  }

  .current-actions :deep(.arco-btn) {
    width: 100%;
  }
}
</style>
