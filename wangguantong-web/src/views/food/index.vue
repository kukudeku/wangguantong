<template>
  <a-card class="page-card" title="网吧点餐">
    <a-tabs default-active-key="order">
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
          <a-button type="primary" @click="submitOrder">确认下单</a-button>
        </a-form>

        <a-form class="toolbar" :model="orderQuery" layout="inline">
          <a-form-item label="顾客姓名">
            <a-input v-model="orderQuery.customerName" allow-clear />
          </a-form-item>
          <a-form-item label="订单状态">
            <a-select v-model="orderQuery.status" allow-clear style="width: 140px">
              <a-option value="已下单">已下单</a-option>
              <a-option value="已取消">已取消</a-option>
            </a-select>
          </a-form-item>
          <a-space>
            <a-button type="primary" @click="loadOrders">查询</a-button>
            <a-button @click="resetOrderQuery">重置</a-button>
          </a-space>
        </a-form>

        <a-table :columns="orderColumns" :data="orders" row-key="id" :pagination="false">
          <template #status="{ record }">
            <a-tag :color="record.status === '已下单' ? 'green' : 'gray'">{{ record.status }}</a-tag>
          </template>
          <template #actions="{ record }">
            <a-popconfirm v-if="record.status === '已下单'" content="确认取消该订单吗？" @ok="handleCancelOrder(record.id)">
              <a-button size="small" status="danger">取消订单</a-button>
            </a-popconfirm>
            <span v-else>已取消</span>
          </template>
        </a-table>
      </a-tab-pane>

      <a-tab-pane key="item" title="商品维护">
        <a-form class="toolbar" :model="itemQuery" layout="inline">
          <a-form-item label="商品名称">
            <a-input v-model="itemQuery.name" allow-clear />
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

        <a-table :columns="itemColumns" :data="items" row-key="id" :pagination="false">
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
  </a-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { getMemberList } from '../../api/member'
import {
  addFoodItem,
  addFoodOrder,
  cancelFoodOrder,
  deleteFoodItem,
  getAvailableFoodItems,
  getFoodItems,
  getFoodOrders,
  updateFoodItem
} from '../../api/food'

const members = ref([])
const availableItems = ref([])
const items = ref([])
const orders = ref([])
const itemVisible = ref(false)

const orderForm = reactive({
  customerType: '会员',
  memberId: null,
  customerName: '',
  foodItemId: null,
  quantity: 1
})

const orderQuery = reactive({ customerName: '', status: '' })
const itemQuery = reactive({ name: '', status: '' })
const itemForm = reactive({ id: null, name: '', price: 1, status: '上架', remark: '' })

const orderColumns = [
  { title: '订单ID', dataIndex: 'id', width: 90 },
  { title: '顾客类型', dataIndex: 'customerType' },
  { title: '顾客姓名', dataIndex: 'customerName' },
  { title: '商品', dataIndex: 'foodItemName' },
  { title: '单价', dataIndex: 'price' },
  { title: '数量', dataIndex: 'quantity' },
  { title: '总金额', dataIndex: 'totalAmount' },
  { title: '状态', slotName: 'status' },
  { title: '下单时间', dataIndex: 'createTime' },
  { title: '操作', slotName: 'actions', width: 120 }
]

const itemColumns = [
  { title: '商品ID', dataIndex: 'id', width: 90 },
  { title: '商品名称', dataIndex: 'name' },
  { title: '价格', dataIndex: 'price' },
  { title: '状态', slotName: 'status' },
  { title: '备注', dataIndex: 'remark' },
  { title: '操作', slotName: 'actions', width: 160 }
]

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
  loadOrders()
}

function resetItemQuery() {
  itemQuery.name = ''
  itemQuery.status = ''
  loadItems()
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
  await addFoodOrder(orderForm)
  Message.success('下单成功')
  Object.assign(orderForm, { customerType: '会员', memberId: null, customerName: '', foodItemId: null, quantity: 1 })
  loadAll()
}

async function handleCancelOrder(id) {
  await cancelFoodOrder(id)
  Message.success('取消成功')
  loadOrders()
}

function openAddItem() {
  Object.assign(itemForm, { id: null, name: '', price: 1, status: '上架', remark: '' })
  itemVisible.value = true
}

function openEditItem(record) {
  Object.assign(itemForm, record)
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
</script>
