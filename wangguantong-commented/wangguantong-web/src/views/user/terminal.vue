<template>
  <div class="wt-shell">
    <!-- 左侧导航固定展示账号摘要、三个业务入口、修改密码和退出按钮。 -->
    <aside class="wt-sidebar">
      <div class="wt-brand">
        <div class="wt-brand-mark">W</div>
        <div>
          <strong>网管通</strong>
          <span>电竞网吧自助终端</span>
        </div>
      </div>

      <nav class="wt-nav" aria-label="用户端功能导航">
        <button :class="{ active: activeTab === 'seat' }" @click="activeTab = 'seat'">
          <IconDesktop />
          <span>座位上机</span>
        </button>
        <button :class="{ active: activeTab === 'food' }" @click="activeTab = 'food'">
          <IconApps />
          <span>自助点餐</span>
          <em v-if="cartItemCount">{{ cartItemCount }}</em>
        </button>
        <button :class="{ active: activeTab === 'records' }" @click="activeTab = 'records'">
          <IconList />
          <span>消费记录</span>
        </button>
      </nav>

      <div class="wt-sidebar-account">
        <div class="wt-user-row">
          <div class="wt-avatar">{{ currentMember?.name?.slice(0, 1) || '用' }}</div>
          <div>
            <strong>{{ currentMember?.name || '用户' }}</strong>
            <span>{{ currentMember?.memberLevel || '普通会员' }}</span>
          </div>
        </div>
        <div class="wt-balance-row">
          <span>账户余额</span>
          <strong>￥{{ money(currentMember?.balance) }}</strong>
        </div>
        <div class="wt-user-status">
          <span>{{ currentMember?.status || '-' }}</span>
          <span v-if="currentRunningRecord" class="online">上机中</span>
        </div>
      </div>

      <div class="wt-sidebar-actions">
        <button title="修改密码" @click="openPasswordModal">
          <IconSettings />
          <span>修改密码</span>
        </button>
        <button title="退出登录" @click="logout">
          <IconPoweroff />
          <span>退出登录</span>
        </button>
      </div>
    </aside>

    <!-- 右侧工作区根据 activeTab 切换座位、点餐和记录内容。 -->
    <div class="wt-workspace">
      <header class="wt-topbar">
        <div>
          <h1>{{ pageTitle }}</h1>
          <p>{{ pageSubtitle }}</p>
        </div>
        <div class="wt-topbar-user">
          <span>当前用户</span>
          <strong>{{ currentMember?.name || '用户' }}</strong>
        </div>
      </header>

      <main class="wt-content">
        <!-- 三个业务页共用的账户摘要条。 -->
        <section class="wt-account-band" aria-label="账户摘要">
          <div class="wt-account-name">
            <div class="wt-avatar small">{{ currentMember?.name?.slice(0, 1) || '用' }}</div>
            <div>
              <strong>{{ currentMember?.name || '-' }}</strong>
              <span>{{ currentMember?.phone || '-' }}</span>
            </div>
          </div>
          <div class="wt-account-item">
            <span>会员级别</span>
            <strong>{{ currentMember?.memberLevel || '-' }}</strong>
          </div>
          <div class="wt-account-item">
            <span>上机权益</span>
            <strong>{{ discountText }}</strong>
          </div>
          <div class="wt-account-item">
            <span>账户状态</span>
            <strong class="status-normal">{{ currentMember?.status || '-' }}</strong>
          </div>
          <div class="wt-account-balance">
            <div>
              <span>可用余额</span>
              <strong>￥{{ money(currentMember?.balance) }}</strong>
            </div>
            <button type="button" class="wt-recharge-entry" @click="openRechargeModal">
              <IconPlus />
              <span>充值</span>
            </button>
          </div>
        </section>

        <!-- 座位页：未上机时显示所有机位；上机后只显示当前电脑和下机操作。 -->
        <section v-if="activeTab === 'seat'" class="wt-layout-grid">
          <div class="wt-surface wt-seat-surface">
            <div class="wt-section-head">
              <div>
                <h2>{{ currentRunningRecord ? '当前上机电脑' : '电脑座位图' }}</h2>
                <p>{{ currentRunningRecord ? '查看实时计费信息，结束使用后请自助下机。' : '绿色机位可直接上机或预约。' }}</p>
              </div>
              <div v-if="!currentRunningRecord" class="wt-legend">
                <span><i class="free"></i>空闲</span>
                <span><i class="using"></i>使用中</span>
                <span><i class="reserved"></i>预约</span>
                <span><i class="repair"></i>维修</span>
              </div>
            </div>

            <div v-if="currentRunningRecord" class="wt-running">
              <div class="wt-running-machine">
                <div>
                  <span>正在使用</span>
                  <strong>{{ currentRunningRecord.computerNo }}</strong>
                  <p>{{ activeComputer?.area || '当前机位' }} · {{ formatDateTime(currentRunningRecord.startTime) }}</p>
                </div>
                <div class="wt-running-state"><i></i>计费中</div>
              </div>
              <div class="wt-metric-strip">
                <div>
                  <span>上机时长</span>
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
                  <strong>￥{{ money(currentMember?.balance) }}</strong>
                </div>
              </div>
              <div v-if="!balanceEnough" class="wt-warning">
                <IconExclamationCircleFill />
                <div>
                  <strong>余额不足</strong>
                  <span>{{ balanceWarningText }}</span>
                </div>
              </div>
              <div class="wt-running-actions">
                <a-popconfirm content="确认下机结算吗？" @ok="submitEndOnline">
                  <a-button type="primary">下机结算</a-button>
                </a-popconfirm>
              </div>
            </div>

            <div v-else class="wt-area-list">
              <section v-for="area in areaGroups" :key="area.name" class="wt-area-section">
                <div class="wt-area-head">
                  <div>
                    <strong>{{ area.name }}</strong>
                    <span>{{ area.computers.length }} 台设备</span>
                  </div>
                  <span>{{ area.computers.filter((item) => item.status === '空闲').length }} 台空闲</span>
                </div>
                <div class="wt-seat-grid">
                  <article
                    v-for="computer in area.computers"
                    :key="computer.id"
                    class="wt-seat-card"
                    :class="seatClass(computer.status)"
                  >
                    <div class="wt-seat-status"><i></i>{{ computer.status }}</div>
                    <div class="wt-seat-number">{{ computer.computerNo }}</div>
                    <div class="wt-seat-meta">
                      <span>{{ computer.area }}</span>
                      <strong>￥{{ money(computer.pricePerHour) }}/小时</strong>
                    </div>
                    <div v-if="computer.status === '空闲'" class="wt-seat-actions">
                      <a-popconfirm
                        :content="`确认使用 ${computer.computerNo} 上机吗？确认后将立即开始计费。`"
                        @ok="submitOnline(computer)"
                      >
                        <a-button type="primary" long>上机</a-button>
                      </a-popconfirm>
                      <a-button @click="openReservation(computer)">预约</a-button>
                    </div>
                    <a-button v-else disabled long>不可操作</a-button>
                  </article>
                </div>
              </section>
            </div>
          </div>

          <!-- 上机期间右侧栏改为快速点餐；未上机时显示各状态机位数量。 -->
          <aside v-if="currentRunningRecord" class="wt-rail wt-order-rail">
            <div class="wt-rail-head">
              <div>
                <h2>快速点餐</h2>
                <p>商品送至当前机位</p>
              </div>
              <button class="wt-text-button" @click="activeTab = 'food'">查看全部</button>
            </div>
            <div v-if="foodItems.length === 0" class="wt-empty">暂无可售商品</div>
            <div v-else class="wt-quick-food-list">
              <article v-for="item in foodItems.slice(0, 5)" :key="item.id">
                <div>
                  <strong>{{ item.name }}</strong>
                  <span>{{ getFoodCategory(item) }} · ￥{{ money(item.price) }}</span>
                </div>
                <div class="wt-stepper small">
                  <button type="button" :disabled="!foodCart[item.id]" @click="decreaseCartItem(item.id)">
                    <IconMinus />
                  </button>
                  <span>{{ foodCart[item.id] || 0 }}</span>
                  <button type="button" @click="addCartItem(item)"><IconPlus /></button>
                </div>
              </article>
            </div>
            <div class="wt-mini-cart">
              <div>
                <span>购物车 · {{ cartItemCount }} 件</span>
                <strong>￥{{ cartTotal }}</strong>
              </div>
              <a-button type="primary" :disabled="cartItems.length === 0" @click="submitCartOrder">下单</a-button>
            </div>
          </aside>

          <aside v-else class="wt-rail">
            <div class="wt-rail-head">
              <div>
                <h2>机位概览</h2>
                <p>当前电脑状态</p>
              </div>
            </div>
            <div class="wt-status-list">
              <div><i class="free"></i><span>空闲电脑</span><strong>{{ freeCount }}</strong></div>
              <div><i class="using"></i><span>使用中</span><strong>{{ usingCount }}</strong></div>
              <div><i class="reserved"></i><span>预约锁定</span><strong>{{ reservedCount }}</strong></div>
              <div><i class="repair"></i><span>维修中</span><strong>{{ repairCount }}</strong></div>
            </div>
            <div class="wt-notice">
              <IconInfoCircle />
              <div>
                <strong>使用提示</strong>
                <p>上机按会员等级计费，余额不足时请前往前台充值。</p>
              </div>
            </div>
          </aside>
        </section>

        <!-- 点餐页：左侧按分类展示商品，右侧购物车只展示清单并统一下单。 -->
        <section v-if="activeTab === 'food'" class="wt-food-layout">
          <div class="wt-surface wt-food-surface">
            <div class="wt-section-head">
              <div>
                <h2>商品列表</h2>
                <p>选择数量后在购物车统一下单。</p>
              </div>
              <div class="wt-category-tabs">
                <button
                  v-for="category in foodCategories"
                  :key="category"
                  :class="{ active: selectedFoodCategory === category }"
                  @click="selectedFoodCategory = category"
                >
                  {{ category }}
                </button>
              </div>
            </div>

            <div v-if="visibleFoodGroups.length === 0" class="wt-empty large">暂无可售商品</div>
            <div v-else class="wt-food-groups">
              <section v-for="group in visibleFoodGroups" :key="group.name" class="wt-food-group">
                <div class="wt-food-group-title">
                  <strong>{{ group.name }}</strong>
                  <span>{{ group.items.length }} 件商品</span>
                </div>
                <div class="wt-food-grid">
                  <article v-for="item in group.items" :key="item.id" class="wt-food-card">
                    <div class="wt-food-copy">
                      <span>{{ group.name }}</span>
                      <strong>{{ item.name }}</strong>
                      <p>{{ item.remark || '网吧商品' }}</p>
                    </div>
                    <div class="wt-food-bottom">
                      <strong>￥{{ money(item.price) }}</strong>
                      <div class="wt-stepper">
                        <button type="button" :disabled="!foodCart[item.id]" @click="decreaseCartItem(item.id)">
                          <IconMinus />
                        </button>
                        <span>{{ foodCart[item.id] || 0 }}</span>
                        <button type="button" @click="addCartItem(item)"><IconPlus /></button>
                      </div>
                    </div>
                  </article>
                </div>
              </section>
            </div>
          </div>

          <aside class="wt-rail wt-cart-rail">
            <div class="wt-rail-head">
              <div>
                <h2>购物车</h2>
                <p>{{ cartItemCount }} 件商品</p>
              </div>
              <button class="wt-text-button" :disabled="cartItems.length === 0" @click="clearCart">清空</button>
            </div>
            <div v-if="cartItems.length === 0" class="wt-cart-empty">
              <IconApps />
              <strong>购物车为空</strong>
              <span>从左侧选择商品</span>
            </div>
            <div v-else class="wt-cart-list">
              <div v-for="item in cartItems" :key="item.id" class="wt-cart-row">
                <div>
                  <strong>{{ item.name }}</strong>
                  <span>￥{{ money(item.price) }} × {{ item.quantity }}</span>
                </div>
                <strong>￥{{ item.total }}</strong>
              </div>
            </div>
            <div class="wt-cart-summary">
              <div><span>商品数量</span><strong>{{ cartItemCount }} 件</strong></div>
              <div class="total"><span>应付合计</span><strong>￥{{ cartTotal }}</strong></div>
              <a-button type="primary" long :disabled="cartItems.length === 0" @click="submitCartOrder">下单</a-button>
              <p>费用将从会员余额中扣除</p>
            </div>
          </aside>
        </section>

        <!-- 消费记录页：余额明细、上机记录、订单记录共用统计摘要和标签切换。 -->
        <section v-if="activeTab === 'records'" class="wt-surface wt-record-surface">
          <div class="wt-record-summary">
            <div>
              <span>当前余额</span>
              <strong>￥{{ money(currentMember?.balance) }}</strong>
            </div>
            <div>
              <span>累计消费</span>
              <strong>￥{{ totalSpent }}</strong>
            </div>
            <div>
              <span>上机记录</span>
              <strong>{{ onlineRecords.length }} 条</strong>
            </div>
            <div>
              <span>点餐订单</span>
              <strong>{{ orders.length }} 笔</strong>
            </div>
          </div>

          <div class="wt-record-toolbar">
            <div>
              <h2>消费明细</h2>
              <p>账户变动与消费记录</p>
            </div>
            <div class="wt-record-tabs">
              <button :class="{ active: recordTab === 'balance' }" @click="recordTab = 'balance'">余额明细</button>
              <button :class="{ active: recordTab === 'online' }" @click="recordTab = 'online'">上机记录</button>
              <button :class="{ active: recordTab === 'order' }" @click="recordTab = 'order'">订单记录</button>
            </div>
          </div>

          <a-table
            v-if="recordTab === 'balance'"
            class="no-wrap-table wt-record-table"
            :columns="balanceColumns"
            :data="balanceDetails"
            row-key="createTime"
            :pagination="false"
            :scroll="{ x: 760 }"
          >
            <template #amount="{ record }">
              <span :class="Number(record.amount) >= 0 ? 'wt-amount-plus' : 'wt-amount-minus'">
                {{ Number(record.amount) >= 0 ? '+' : '' }}{{ record.amount }}
              </span>
            </template>
            <template #createTime="{ record }">{{ formatDateTime(record.createTime) }}</template>
          </a-table>

          <a-table
            v-if="recordTab === 'online'"
            class="no-wrap-table wt-record-table"
            :columns="onlineColumns"
            :data="onlineRecords"
            row-key="id"
            :pagination="false"
            :scroll="{ x: 980 }"
          >
            <template #startTime="{ record }">{{ formatDateTime(record.startTime) }}</template>
            <template #endTime="{ record }">{{ formatDateTime(record.endTime) }}</template>
            <template #status="{ record }">
              <a-tag :color="record.status === '进行中' ? 'blue' : 'green'">{{ record.status }}</a-tag>
            </template>
          </a-table>

          <a-table
            v-if="recordTab === 'order'"
            class="no-wrap-table wt-record-table"
            :columns="orderColumns"
            :data="orders"
            row-key="id"
            :pagination="false"
            :scroll="{ x: 920 }"
          >
            <template #createTime="{ record }">{{ formatDateTime(record.createTime) }}</template>
            <template #status="{ record }">
              <a-tag :color="orderStatusColor(record.status)">{{ record.status }}</a-tag>
            </template>
          </a-table>
        </section>
      </main>
    </div>

    <!-- 以下三个弹窗放在页面末尾，通过对应 visible 变量控制显示。 -->
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
          <div class="wt-form-tip">只能预约当前时间起 1 小时内，超时未上机自动取消。</div>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:visible="passwordVisible" title="修改密码" @ok="submitPasswordChange">
      <a-form :model="passwordForm" layout="vertical">
        <a-form-item label="原密码"><a-input-password v-model="passwordForm.oldPassword" /></a-form-item>
        <a-form-item label="新密码"><a-input-password v-model="passwordForm.newPassword" /></a-form-item>
        <a-form-item label="确认新密码"><a-input-password v-model="passwordForm.confirmPassword" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:visible="rechargeVisible"
      title="余额充值"
      width="min(520px, calc(100vw - 24px))"
      ok-text="确认充值"
      cancel-text="取消"
      :ok-loading="rechargeSubmitting"
      :on-before-ok="submitRecharge"
    >
      <div class="wt-recharge-dialog">
        <div class="wt-recharge-balance">
          <span>当前余额</span>
          <strong>￥{{ money(currentMember?.balance) }}</strong>
        </div>
        <div class="wt-recharge-field">
          <label>选择充值金额</label>
          <div class="wt-recharge-options">
            <button
              v-for="amount in rechargeOptions"
              :key="amount"
              type="button"
              :class="{ active: Number(rechargeForm.amount) === amount }"
              @click="rechargeForm.amount = amount"
            >
              ￥{{ amount }}
            </button>
          </div>
        </div>
        <div class="wt-recharge-field">
          <label>自定义金额</label>
          <a-input-number
            v-model="rechargeForm.amount"
            :min="1"
            :max="10000"
            :precision="2"
            placeholder="请输入充值金额"
            style="width: 100%"
          >
            <template #prefix>￥</template>
          </a-input-number>
        </div>
        <div class="wt-recharge-notice">
          <IconInfoCircle />
          <span>本系统不接入支付平台，确认后将直接增加账户余额。</span>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import {
  IconApps,
  IconDesktop,
  IconExclamationCircleFill,
  IconInfoCircle,
  IconList,
  IconMinus,
  IconPlus,
  IconPoweroff,
  IconSettings
} from '@arco-design/web-vue/es/icon'
import { getComputerList } from '../../api/computer'
import { addFoodOrder, getAvailableFoodItems } from '../../api/food'
import { getMemberList } from '../../api/member'
import { endOnline, startOnline } from '../../api/online'
import { addRecharge } from '../../api/recharge'
import { addReservation } from '../../api/reservation'
import { changeUserPassword, getUserBalanceDetail, getUserOnlineRecords, getUserOrders } from '../../api/user'
import { formatDateTime } from '../../utils/format'

// ===== 页面基础状态 =====
const router = useRouter()
// 三个一级功能页和记录页内的二级标签。
const activeTab = ref('seat')
const recordTab = ref('balance')
const selectedFoodCategory = ref('全部')
// 当前用户先从浏览器本地读取，随后再向后端刷新最新余额和状态。
const currentMember = ref(getStoredUser())
// 接口返回的机位、商品、余额明细、上机记录和订单列表。
const computers = ref([])
const foodItems = ref([])
const balanceDetails = ref([])
const onlineRecords = ref([])
const orders = ref([])
// 购物车使用 { 商品ID: 数量 } 结构，例如 { 1: 2, 3: 1 }。
const foodCart = reactive({})
// 当前时间每秒更新，用于在前端动态计算上机时长。
const now = ref(new Date())
// 三个弹窗及充值提交状态。两个提交标记用于阻止重复点击造成重复充值。
const reservationVisible = ref(false)
const passwordVisible = ref(false)
const rechargeVisible = ref(false)
const rechargeSubmitting = ref(false)
const rechargeSubmitted = ref(false)
// 弹窗表单和预约时临时选中的电脑。
const selectedComputer = ref(null)
const reservationForm = reactive({ reserveTime: '' })
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const rechargeForm = reactive({ amount: 50 })
// 充值弹窗的四个快捷金额。
const rechargeOptions = [20, 50, 100, 200]
// timer 负责页面时钟，runningRefreshTimer 负责定期同步后端实时数据。
let timer = null
let runningRefreshTimer = null

// ===== 消费记录的三张表格列定义 =====
const balanceColumns = [
  { title: '类型', dataIndex: 'type', width: 110 },
  { title: '说明', dataIndex: 'description', width: 300 },
  { title: '金额', slotName: 'amount', width: 130 },
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
  { title: '商品', dataIndex: 'foodItemName', width: 170 },
  { title: '单价', dataIndex: 'price', width: 100 },
  { title: '数量', dataIndex: 'quantity', width: 90 },
  { title: '总金额', dataIndex: 'totalAmount', width: 120 },
  { title: '状态', slotName: 'status', width: 110 },
  { title: '下单时间', slotName: 'createTime', width: 200 }
]

function getStoredUser() {
  // localStorage 只能保存字符串，因此登录时 JSON.stringify，读取时 JSON.parse。
  const text = localStorage.getItem('user')
  return text ? JSON.parse(text) : null
}

// ===== 页面展示用的计算属性 =====
// computed 会缓存计算结果，并在依赖的数据变化时自动更新模板。
const discountText = computed(() => {
  if (!currentMember.value) return '-'
  if (currentMember.value.memberLevel === '钻石会员') return '上机 8 折'
  if (currentMember.value.memberLevel === '黄金会员') return '上机 9 折'
  if (currentMember.value.memberLevel === '普通会员') return '上机不打折'
  return '无折扣'
})

const pageTitle = computed(() => {
  // 一级导航切换时同步更新右侧顶部标题。
  if (activeTab.value === 'food') return '自助点餐'
  if (activeTab.value === 'records') return '消费记录'
  return '座位与上机'
})

const pageSubtitle = computed(() => {
  // 座位页还会根据是否正在上机显示不同副标题。
  if (activeTab.value === 'food') return '选择商品，确认清单后下单'
  if (activeTab.value === 'records') return '查看账户余额与历史消费'
  if (currentRunningRecord.value) return '当前已有电脑正在使用，可在本页自助下机'
  return '选择空闲电脑，自助上机或预约'
})

// 从自己的上机记录中找出唯一一条进行中记录，并找到对应电脑资料。
const currentRunningRecord = computed(() => onlineRecords.value.find((item) => item.status === '进行中'))
const activeComputer = computed(() => computers.value.find((item) => item.id === currentRunningRecord.value?.computerId))

// 商品先按分类组成 [{ name, items }]，再根据当前分类标签决定显示哪些组。
const groupedFoodItems = computed(() => {
  const map = new Map()
  foodItems.value.forEach((item) => {
    const category = getFoodCategory(item)
    if (!map.has(category)) map.set(category, [])
    map.get(category).push(item)
  })
  return Array.from(map.entries()).map(([name, items]) => ({ name, items }))
})
const foodCategories = computed(() => ['全部', ...groupedFoodItems.value.map((group) => group.name)])
const visibleFoodGroups = computed(() => {
  if (selectedFoodCategory.value === '全部') return groupedFoodItems.value
  return groupedFoodItems.value.filter((group) => group.name === selectedFoodCategory.value)
})
// 根据 foodCart 中数量大于 0 的商品生成完整购物车清单，并计算每项小计。
const cartItems = computed(() => foodItems.value
  .filter((item) => Number(foodCart[item.id] || 0) > 0)
  .map((item) => {
    const quantity = Number(foodCart[item.id] || 0)
    const total = (Number(item.price || 0) * quantity).toFixed(2)
    return { ...item, quantity, total }
  }))
// 购物车商品总件数和总金额会随加减按钮自动更新。
const cartItemCount = computed(() => cartItems.value.reduce((sum, item) => sum + item.quantity, 0))
const cartTotal = computed(() => cartItems.value.reduce((sum, item) => sum + Number(item.total), 0).toFixed(2))
// 右侧机位概览所需的四个状态数量。
const freeCount = computed(() => computers.value.filter((item) => item.status === '空闲').length)
const usingCount = computed(() => computers.value.filter((item) => item.status === '使用中').length)
const reservedCount = computed(() => computers.value.filter((item) => item.status === '预约锁定').length)
const repairCount = computed(() => computers.value.filter((item) => item.status === '维修中').length)
// 余额明细中负数代表消费；取绝对值后累加得到累计消费。
const totalSpent = computed(() => balanceDetails.value
  .reduce((sum, item) => {
    const amount = Number(item.amount || 0)
    return amount < 0 ? sum + Math.abs(amount) : sum
  }, 0)
  .toFixed(2))

const runningMinutes = computed(() => {
  // 当前时间减开始时间得到毫秒，再除以 60000 转成分钟。
  if (!currentRunningRecord.value?.startTime) return 0
  const start = new Date(currentRunningRecord.value.startTime)
  const diff = Math.floor((now.value.getTime() - start.getTime()) / 60000)
  return diff < 0 ? 0 : diff
})

const runningDurationText = computed(() => {
  // 把分钟数转成“x 小时 y 分钟”的易读文字。
  const minutes = runningMinutes.value
  const hours = Math.floor(minutes / 60)
  const restMinutes = minutes % 60
  if (hours <= 0) return `${restMinutes} 分钟`
  return `${hours} 小时 ${restMinutes} 分钟`
})

// 计费小时向上取整，且上机不足一小时也至少按一小时计算。
const chargeHours = computed(() => Math.max(1, Math.ceil(runningMinutes.value / 60)))
const discountRate = computed(() => {
  // 优先使用后端随上机记录返回的折扣；没有时按当前会员等级补算。
  if (currentRunningRecord.value?.discountRate) return Number(currentRunningRecord.value.discountRate)
  if (currentMember.value?.memberLevel === '钻石会员') return 0.8
  if (currentMember.value?.memberLevel === '黄金会员') return 0.9
  return 1
})
const estimatedCurrentAmount = computed(() => {
  // 页面估算金额 = 当前机位小时单价 × 计费小时 × 折扣。
  const price = Number(activeComputer.value?.pricePerHour || 0)
  if (!price && currentRunningRecord.value?.currentAmount !== undefined) {
    return Number(currentRunningRecord.value.currentAmount).toFixed(2)
  }
  return (price * chargeHours.value * discountRate.value).toFixed(2)
})
const pendingChargeAmount = computed(() => {
  // 计算进入下一个计费小时需要补扣的金额，用来提前判断余额是否足够。
  const price = Number(activeComputer.value?.pricePerHour || 0)
  const nextAmount = price
    ? price * (chargeHours.value + 1) * discountRate.value
    : Number(estimatedCurrentAmount.value || 0)
  const paidAmount = Number(currentRunningRecord.value?.totalAmount || 0)
  return Math.max(0, nextAmount - paidAmount)
})
const balanceEnough = computed(() => Number(currentMember.value?.balance || 0) >= pendingChargeAmount.value)
const balanceWarningText = computed(() => currentRunningRecord.value?.warningMessage || '余额不足以支付下一计费小时，请及时充值。')

const areaGroups = computed(() => {
  // 使用 Map 按 area 分组，最后按电脑编号排序，保证座位图稳定易找。
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

function money(value) {
  // 整数显示为 5，小数统一显示为 5.50。
  const number = Number(value || 0)
  return Number.isInteger(number) ? String(number) : number.toFixed(2)
}

function seatClass(status) {
  // 状态转换为 CSS 类，用不同颜色区分空闲、使用、预约和维修。
  if (status === '空闲') return 'is-free'
  if (status === '使用中') return 'is-using'
  if (status === '预约锁定') return 'is-reserved'
  return 'is-repair'
}

function getFoodCategory(item) {
  // 新数据直接使用后台设置的分类；旧数据没有分类时根据名称/备注做兼容推断。
  if (item.category) return item.category
  const text = `${item.name || ''}${item.remark || ''}`
  if (/可乐|雪碧|水|茶|咖啡|饮|奶|汁/.test(text)) return '饮料'
  if (/泡面|饭|面|餐|肠|热狗|汉堡/.test(text)) return '餐食'
  if (/薯片|饼干|糖|巧克力|零食/.test(text)) return '零食'
  return '其他'
}

function orderStatusColor(status) {
  // 订单状态映射为 Arco 标签颜色。
  if (status === '已完成') return 'green'
  if (status === '已取消') return 'gray'
  return 'orange'
}

async function loadData() {
  // ===== 数据加载与定时刷新 =====
  // 首次进入用户端需要机位、可售商品以及当前用户的三类记录。
  computers.value = await getComputerList({})
  foodItems.value = await getAvailableFoodItems()
  await loadUserRecords()
}

async function loadUserRecords() {
  // 三个接口都以当前会员 id 隔离数据，用户只能查看自己的记录。
  if (!currentMember.value?.id) return
  onlineRecords.value = await getUserOnlineRecords(currentMember.value.id)
  balanceDetails.value = await getUserBalanceDetail(currentMember.value.id)
  orders.value = await getUserOrders(currentMember.value.id)
}

async function refreshRunningData() {
  // 上机期间每 30 秒同步机位、实时记录和最新余额。
  if (!currentMember.value?.id) return
  computers.value = await getComputerList({})
  await loadUserRecords()
  await refreshCurrentMember()
}

async function refreshCurrentMember() {
  // 通过手机号查询最新用户资料，并用精确匹配防止使用相似查询结果。
  if (!currentMember.value?.phone) return
  const list = await getMemberList({ phone: currentMember.value.phone })
  const member = list.find((item) => item.phone === currentMember.value.phone)
  if (member) {
    // 同时更新响应式变量和本地缓存，刷新页面后余额仍是最新值。
    currentMember.value = member
    localStorage.setItem('user', JSON.stringify(member))
  }
}

function logout() {
  // ===== 账号操作 =====
  // 清除本地用户凭证后返回用户登录页。
  localStorage.removeItem('user')
  router.push('/user/login')
}

function openPasswordModal() {
  // 每次打开弹窗都清空上次输入，避免密码残留。
  Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
  passwordVisible.value = true
}

function openRechargeModal() {
  // 默认选中 50 元，并重置防重复提交标记。
  rechargeForm.amount = 50
  rechargeSubmitted.value = false
  rechargeVisible.value = true
}

async function submitRecharge() {
  // 本项目是模拟充值，不接支付平台；这里只校验合理范围后直接请求后端加余额。
  const amount = Number(rechargeForm.amount)
  if (!Number.isFinite(amount) || amount <= 0) {
    Message.error('充值金额必须大于 0')
    return false
  }
  if (amount > 10000) {
    Message.error('单次充值不能超过 10000 元')
    return false
  }
  // 用户连续点击确定时直接忽略后续请求，避免同一笔充值执行两次。
  if (rechargeSubmitting.value || rechargeSubmitted.value) return false

  rechargeSubmitting.value = true
  rechargeSubmitted.value = true
  try {
    // 充值成功后刷新账户余额和余额明细。
    await addRecharge({ memberId: currentMember.value.id, amount })
    await refreshCurrentMember()
    await loadUserRecords()
    Message.success(`充值 ${money(amount)} 元成功`)
    return true
  } catch (error) {
    // 失败时恢复 submitted 标记，用户修改后可以再次提交。
    rechargeSubmitted.value = false
    return false
  } finally {
    rechargeSubmitting.value = false
  }
}

async function submitPasswordChange() {
  // 修改密码先检查三个输入框和两次新密码是否一致。
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    Message.error('请完整填写密码信息')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    Message.error('两次输入的新密码不一致')
    return
  }
  // 旧密码是否正确由后端校验，成功后返回最新用户对象。
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

function addCartItem(item) {
  // ===== 购物车与下单 =====
  // 加号只修改本地购物车数量，不会立即创建订单。
  if (!checkUserCanOperate()) return
  foodCart[item.id] = Number(foodCart[item.id] || 0) + 1
}

function decreaseCartItem(itemId) {
  // 数量减到 0 时直接删除该键，让商品从购物车清单消失。
  const current = Number(foodCart[itemId] || 0)
  if (current <= 1) {
    delete foodCart[itemId]
    return
  }
  foodCart[itemId] = current - 1
}

function clearCart() {
  // reactive 对象应逐个 delete 属性，Vue 才能正确更新依赖它的计算属性。
  Object.keys(foodCart).forEach((key) => delete foodCart[key])
}

async function submitCartOrder() {
  // 下单前校验用户状态、购物车非空和当前余额。
  if (!checkUserCanOperate()) return
  if (cartItems.value.length === 0) {
    Message.warning('请先选择商品')
    return
  }
  if (Number(currentMember.value?.balance || 0) < Number(cartTotal.value)) {
    Message.error('会员余额不足，请先充值')
    return
  }
  // 当前后端接口一次保存一个商品，因此按购物车清单逐项创建订单。
  for (const item of cartItems.value) {
    await addFoodOrder({
      customerType: '会员',
      memberId: currentMember.value.id,
      customerName: currentMember.value.name,
      foodItemId: item.id,
      quantity: item.quantity
    })
  }
  Message.success('下单成功')
  // 全部成功后清空购物车，并刷新余额和订单记录。
  clearCart()
  await refreshCurrentMember()
  await loadUserRecords()
}

function openReservation(computer) {
  // ===== 上机、下机与预约 =====
  // 只有正常会员可以预约；散客需先充值成为普通会员。
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
  // 页面确认框由模板中的 a-popconfirm 提供，确认后才进入此函数。
  if (!checkUserCanOperate()) return
  if (currentRunningRecord.value) {
    Message.warning('您已有正在使用的电脑，请先下机')
    return
  }
  if (currentMember.value.userType !== '会员') {
    Message.error('散客不能自助上机，请先联系前台充值成为普通会员')
    return
  }
  // 后端会再次校验余额和机位状态，并立即扣除首小时费用。
  await startOnline({ memberId: currentMember.value.id, computerId: computer.id })
  Message.success('上机成功')
  await refreshCurrentMember()
  await loadData()
}

async function submitEndOnline() {
  // 下机后刷新数据，页面会从“当前电脑”恢复成完整座位图。
  if (!currentRunningRecord.value) {
    Message.error('当前没有进行中的上机记录')
    return
  }
  await endOnline(currentRunningRecord.value.id)
  Message.success('下机成功')
  await refreshCurrentMember()
  await loadData()
}

async function submitReservation() {
  // 先验证电脑和时间，再提交会员、电脑及预约时间。
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
  // 浏览器校验未来一小时限制；后端也有相同校验，避免前端被绕过。
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
  // 点餐、上机和预约共用此检查，确保用户已登录且账号状态正常。
  if (!currentMember.value) {
    Message.error('请先登录用户系统')
    router.push('/user/login')
    return false
  }
  if (currentMember.value.status !== '正常') {
    Message.error('用户状态异常，请联系前台')
    return false
  }
  return true
}

onMounted(() => {
  // ===== 生命周期 =====
  // 组件挂载时验证登录、加载数据，并启动页面时间和后端同步定时器。
  if (!currentMember.value) {
    router.push('/user/login')
    return
  }
  loadData()
  refreshCurrentMember()
  // 每秒只更新本地时间，不产生网络请求。
  timer = window.setInterval(() => {
    now.value = new Date()
  }, 1000)
  // 只有正在上机时才每 30 秒访问后端，减少空闲状态下的请求。
  runningRefreshTimer = window.setInterval(() => {
    if (currentRunningRecord.value) refreshRunningData()
  }, 30000)
})

onBeforeUnmount(() => {
  // 离开用户端时销毁定时器，防止重复进入页面后出现多个定时任务。
  if (timer) window.clearInterval(timer)
  if (runningRefreshTimer) window.clearInterval(runningRefreshTimer)
})
</script>

<!-- 用户端样式较长，拆到独立 CSS；scoped 仍会限制它只作用于本组件。 -->
<style scoped src="./terminal.css"></style>
