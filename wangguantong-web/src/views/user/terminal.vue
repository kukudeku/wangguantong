<template>
  <div class="wt-shell">
    <aside class="wt-sidebar">
      <div class="wt-brand">
        <div class="wt-brand-mark">W</div>
        <div>
          <strong>网管通</strong>
          <span>电竞网吧自助终端</span>
        </div>
      </div>

      <nav class="wt-nav" aria-label="用户端功能导航">
        <div class="wt-nav-group" :class="{ expanded: onlineMenuExpanded }">
          <button
            type="button"
            class="wt-nav-parent"
            :class="{ 'child-active': isOnlineServicePage }"
            :aria-expanded="onlineMenuExpanded"
            @click="toggleOnlineMenu"
          >
            <IconDesktop />
            <span>上机服务</span>
            <span class="wt-nav-meta"><IconDown class="wt-nav-arrow" /></span>
          </button>
          <div v-show="onlineMenuExpanded" class="wt-subnav">
            <button type="button" :class="{ active: activeTab === 'seat' }" :aria-current="activeTab === 'seat' ? 'page' : undefined" @click="openMachineHall">
              <IconDesktop />
              <span>机位大厅</span>
            </button>
            <button type="button" :class="{ active: activeTab === 'current-online' }" :aria-current="activeTab === 'current-online' ? 'page' : undefined" @click="openCurrentOnline">
              <IconLiveBroadcast />
              <span>当前上机</span>
            </button>
            <button type="button" :class="{ active: activeTab === 'reservation-list' }" :aria-current="activeTab === 'reservation-list' ? 'page' : undefined" @click="openMyReservations">
              <IconCalendarClock />
              <span>我的预约</span>
            </button>
            <button type="button" :class="{ active: activeTab === 'online-history' }" :aria-current="activeTab === 'online-history' ? 'page' : undefined" @click="openOnlineHistory">
              <IconHistory />
              <span>上机记录</span>
            </button>
          </div>
        </div>
        <div class="wt-nav-group" :class="{ expanded: foodMenuExpanded }">
          <button
            type="button"
            class="wt-nav-parent"
            :class="{ 'child-active': isFoodPage }"
            :aria-expanded="foodMenuExpanded"
            @click="toggleFoodMenu"
          >
            <IconApps />
            <span>自助点餐</span>
            <span class="wt-nav-meta">
              <em v-if="cartItemCount">{{ cartItemCount }}</em>
              <IconDown class="wt-nav-arrow" />
            </span>
          </button>
          <div v-show="foodMenuExpanded" class="wt-subnav">
            <button type="button" :class="{ active: activeTab === 'food' }" :aria-current="activeTab === 'food' ? 'page' : undefined" @click="openFoodProducts">
              <IconApps />
              <span>商品点餐</span>
            </button>
            <button type="button" :class="{ active: activeTab === 'food-order' }" :aria-current="activeTab === 'food-order' ? 'page' : undefined" @click="openFoodOrders">
              <IconHistory />
              <span>订单记录</span>
            </button>
          </div>
        </div>
        <div class="wt-nav-group" :class="{ expanded: activityMenuExpanded }">
          <button
            type="button"
            class="wt-nav-parent"
            :class="{ 'child-active': isActivityPage }"
            :aria-expanded="activityMenuExpanded"
            @click="toggleActivityMenu"
          >
            <IconGift />
            <span>活动中心</span>
            <span class="wt-nav-meta"><IconDown class="wt-nav-arrow" /></span>
          </button>
          <div v-show="activityMenuExpanded" class="wt-subnav">
            <button type="button" :class="{ active: activeTab === 'coupon' }" :aria-current="activeTab === 'coupon' ? 'page' : undefined" @click="openDailySignIn">
              <IconCheckCircleFill />
              <span>每日签到</span>
            </button>
            <button type="button" :class="{ active: activeTab === 'coupon-list' }" :aria-current="activeTab === 'coupon-list' ? 'page' : undefined" @click="openMyCoupons">
              <IconGift />
              <span>我的优惠券</span>
            </button>
          </div>
        </div>
        <div class="wt-nav-group" :class="{ expanded: promotionMenuExpanded }">
          <button
            type="button"
            class="wt-nav-parent"
            :class="{ 'child-active': isPromotionPage }"
            :aria-expanded="promotionMenuExpanded"
            @click="togglePromotionMenu"
          >
            <IconUserAdd />
            <span>推广计划</span>
            <span class="wt-nav-meta"><IconDown class="wt-nav-arrow" /></span>
          </button>
          <div v-show="promotionMenuExpanded" class="wt-subnav">
            <button type="button" :class="{ active: activeTab === 'promotion' }" :aria-current="activeTab === 'promotion' ? 'page' : undefined" @click="openPromotionInvite">
              <IconUserAdd />
              <span>邀请好友</span>
            </button>
            <button type="button" :class="{ active: activeTab === 'promotion-record' }" :aria-current="activeTab === 'promotion-record' ? 'page' : undefined" @click="openPromotionRecords">
              <IconHistory />
              <span>邀请记录</span>
            </button>
          </div>
        </div>
        <div class="wt-nav-group" :class="{ expanded: serviceMenuExpanded }">
          <button
            type="button"
            class="wt-nav-parent"
            :class="{ 'child-active': isServicePage }"
            :aria-expanded="serviceMenuExpanded"
            @click="toggleServiceMenu"
          >
            <IconCustomerService />
            <span>服务中心</span>
            <span class="wt-nav-meta"><IconDown class="wt-nav-arrow" /></span>
          </button>
          <div v-show="serviceMenuExpanded" class="wt-subnav">
            <button type="button" :class="{ active: activeTab === 'service-call' }" :aria-current="activeTab === 'service-call' ? 'page' : undefined" @click="openServiceCall">
              <IconCustomerService />
              <span>呼叫网管</span>
            </button>
            <button type="button" :class="{ active: activeTab === 'service-repair' }" :aria-current="activeTab === 'service-repair' ? 'page' : undefined" @click="openServiceRepair">
              <IconTool />
              <span>故障报修</span>
            </button>
            <button type="button" :class="{ active: activeTab === 'service-record' }" :aria-current="activeTab === 'service-record' ? 'page' : undefined" @click="openServiceRecords">
              <IconHistory />
              <span>服务记录</span>
            </button>
          </div>
        </div>
        <div class="wt-nav-group" :class="{ expanded: balanceMenuExpanded }">
          <button
            type="button"
            class="wt-nav-parent"
            :class="{ 'child-active': isBalancePage }"
            :aria-expanded="balanceMenuExpanded"
            @click="toggleBalanceMenu"
          >
            <IconList />
            <span>账户服务</span>
            <span class="wt-nav-meta"><IconDown class="wt-nav-arrow" /></span>
          </button>
          <div v-show="balanceMenuExpanded" class="wt-subnav">
            <button type="button" :class="{ active: activeTab === 'balance' }" :aria-current="activeTab === 'balance' ? 'page' : undefined" @click="openBalancePage">
              <IconSafe />
              <span>余额账户</span>
            </button>
            <button type="button" :class="{ active: activeTab === 'voucher' }" :aria-current="activeTab === 'voucher' ? 'page' : undefined" @click="openVoucherPage">
              <IconScan />
              <span>团购验券</span>
            </button>
          </div>
        </div>
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
          <span v-else-if="currentReservation" class="reserved">已预约</span>
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
        <section v-if="isOnlineServicePage" class="wt-account-band" aria-label="账户摘要">
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
          </div>
        </section>

        <section v-if="activeTab === 'seat'" class="wt-layout-grid">
          <div class="wt-surface wt-seat-surface">
            <div class="wt-section-head">
              <div>
                <h2>电脑座位图</h2>
                <p>{{ currentReservation ? '紫色机位为本人预约，可前往“我的预约”操作。' : '绿色机位可直接上机或预约。' }}</p>
              </div>
              <div class="wt-legend">
                <span><i class="free"></i>空闲</span>
                <span><i class="using"></i>使用中</span>
                <span><i class="reserved"></i>预约</span>
                <span><i class="repair"></i>维修</span>
              </div>
            </div>

            <div class="wt-area-list">
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
                    :class="[seatClass(computer.status), { 'has-own-reservation': ownReservationMap.has(computer.id) }]"
                  >
                    <div class="wt-seat-status"><i></i>{{ computer.status }}</div>
                    <div class="wt-seat-number">{{ computer.computerNo }}</div>
                    <div class="wt-seat-meta">
                      <span>{{ computer.area }}</span>
                      <strong>￥{{ money(computer.pricePerHour) }}/小时</strong>
                    </div>
                    <div v-if="ownReservationMap.has(computer.id)" class="wt-reservation-note">
                      <span>我的预约</span>
                      <strong>{{ formatDateTime(ownReservationMap.get(computer.id).reserveTime) }} 前上机</strong>
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
                    <div v-else-if="ownReservationMap.has(computer.id)" class="wt-seat-actions">
                      <a-popconfirm
                        :content="`确认使用预约的 ${computer.computerNo} 上机吗？确认后将立即开始计费。`"
                        @ok="submitReservedOnline(ownReservationMap.get(computer.id))"
                      >
                        <a-button type="primary" long>预约上机</a-button>
                      </a-popconfirm>
                      <a-popconfirm content="确认取消该预约吗？" @ok="submitCancelReservation(ownReservationMap.get(computer.id))">
                        <a-button status="danger">取消预约</a-button>
                      </a-popconfirm>
                    </div>
                    <a-button v-else disabled long>不可操作</a-button>
                  </article>
                </div>
              </section>
            </div>
          </div>

          <aside class="wt-rail">
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

        <section v-if="activeTab === 'current-online'" class="wt-layout-grid" :class="{ 'single-column': !currentRunningRecord }">
          <div class="wt-surface wt-seat-surface">
            <div class="wt-section-head">
              <div>
                <h2>当前上机</h2>
                <p>实时查看上机时长与消费金额</p>
              </div>
              <a-button @click="refreshRunningData">刷新</a-button>
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
                <a-button status="warning" @click="openRepairModal">
                  <template #icon><IconTool /></template>
                  机器报修
                </a-button>
                <a-popconfirm content="确认下机结算吗？" @ok="submitEndOnline">
                  <a-button type="primary">下机结算</a-button>
                </a-popconfirm>
              </div>
            </div>

            <div v-else class="wt-empty large wt-online-empty">
              <div>
                <IconLiveBroadcast />
                <strong>当前未上机</strong>
                <a-button type="primary" @click="openMachineHall">前往机位大厅</a-button>
              </div>
            </div>
          </div>

          <aside v-if="currentRunningRecord" class="wt-rail wt-order-rail">
            <div class="wt-rail-head">
              <div>
                <h2>快速点餐</h2>
                <p>商品送至当前机位</p>
              </div>
              <button class="wt-text-button" @click="openFoodProducts">查看全部</button>
            </div>
            <div v-if="foodItems.length === 0" class="wt-empty">暂无可售商品</div>
            <div v-else class="wt-quick-food-list">
              <article v-for="item in foodItems.slice(0, 5)" :key="item.id">
                <div class="wt-quick-food-info">
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
              <a-button type="primary" :disabled="cartItems.length === 0" @click="openFoodProducts">去结算</a-button>
            </div>
          </aside>
        </section>

        <section v-if="activeTab === 'reservation-list'" class="wt-surface wt-record-surface">
          <div class="wt-record-toolbar">
            <div>
              <h2>我的预约</h2>
              <p>查看预约状态并完成预约上机</p>
            </div>
            <a-button @click="loadUserRecords">刷新</a-button>
          </div>
          <a-table
            class="no-wrap-table wt-record-table"
            :columns="reservationColumns"
            :data="reservations"
            row-key="id"
            :pagination="false"
            :scroll="{ x: 900 }"
          >
            <template #reserveTime="{ record }">{{ formatDateTime(record.reserveTime) }}</template>
            <template #createTime="{ record }">{{ formatDateTime(record.createTime) }}</template>
            <template #status="{ record }"><a-tag :color="reservationStatusColor(record.status)">{{ record.status }}</a-tag></template>
            <template #reservationActions="{ record }">
              <div v-if="record.status === '已预约'" class="wt-table-actions">
                <a-popconfirm :content="`确认使用预约的 ${record.computerNo} 上机吗？`" @ok="submitReservedOnline(record)">
                  <a-button type="primary" size="small">预约上机</a-button>
                </a-popconfirm>
                <a-popconfirm content="确认取消该预约吗？" @ok="submitCancelReservation(record)">
                  <a-button status="danger" size="small">取消预约</a-button>
                </a-popconfirm>
              </div>
              <span v-else>-</span>
            </template>
          </a-table>
        </section>

        <section v-if="activeTab === 'online-history'" class="wt-surface wt-record-surface">
          <div class="wt-record-toolbar">
            <div>
              <h2>上机记录</h2>
              <p>查看历史上机与扣费记录</p>
            </div>
            <a-button @click="loadUserRecords">刷新</a-button>
          </div>
          <a-table
            class="no-wrap-table wt-record-table"
            :columns="onlineColumns"
            :data="onlineHistoryRecords"
            row-key="id"
            :pagination="false"
            :scroll="{ x: 980 }"
          >
            <template #startTime="{ record }">{{ formatDateTime(record.startTime) }}</template>
            <template #endTime="{ record }">{{ formatDateTime(record.endTime) }}</template>
            <template #status="{ record }"><a-tag color="green">{{ record.status }}</a-tag></template>
          </a-table>
        </section>

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
              <div class="wt-cart-coupon">
                <span>优惠券</span>
                <a-select v-model="selectedCouponId" allow-clear placeholder="不使用优惠券" size="small">
                  <a-option v-for="coupon in eligibleCoupons" :key="coupon.id" :value="coupon.id">
                    {{ coupon.couponName }}（减 ￥{{ money(coupon.discountAmount) }}）
                  </a-option>
                </a-select>
              </div>
              <div v-if="cartDiscount > 0"><span>优惠金额</span><strong class="wt-discount-value">-￥{{ cartDiscount }}</strong></div>
              <div class="wt-cart-payment">
                <span>支付方式</span>
                <a-radio-group v-model="paymentMethod" type="button" size="small" :options="paymentMethods" />
              </div>
              <div class="total"><span>应付合计</span><strong>￥{{ cartPayable }}</strong></div>
              <a-button type="primary" long :loading="paymentSubmitting" :disabled="cartItems.length === 0" @click="submitCartOrder">下单</a-button>
            </div>
          </aside>
        </section>

        <section v-if="activeTab === 'food-order'" class="wt-surface wt-record-surface">
          <div class="wt-record-toolbar wt-order-toolbar">
            <div>
              <h2>订单记录</h2>
              <p>查看点餐订单、支付状态和处理进度</p>
            </div>
            <a-button @click="loadUserRecords">刷新</a-button>
          </div>

          <a-table
            class="no-wrap-table wt-record-table"
            :columns="orderColumns"
            :data="orders"
            row-key="id"
            :pagination="false"
            :scroll="{ x: 1480 }"
          >
            <template #createTime="{ record }">{{ formatDateTime(record.createTime) }}</template>
            <template #status="{ record }">
              <a-tag :color="orderStatusColor(record.status)">{{ record.status }}</a-tag>
            </template>
            <template #paymentStatus="{ record }">
              <a-tag :color="paymentStatusColor(record.paymentStatus)">{{ record.paymentStatus || '已支付' }}</a-tag>
            </template>
            <template #coupon="{ record }">{{ record.couponName || '-' }}</template>
            <template #orderActions="{ record }">
              <a-button
                v-if="record.status === '待支付' && record.paymentOutTradeNo"
                type="primary"
                size="small"
                @click="resumePayment(record)"
              >继续支付</a-button>
              <span v-else>-</span>
            </template>
          </a-table>
        </section>

        <section v-if="activeTab === 'coupon'" class="wt-surface wt-sign-surface">
            <div class="wt-section-head">
              <div>
                <h2>每日签到</h2>
                <p>连续签到达到指定天数，自动领取点餐优惠券。</p>
              </div>
              <a-button type="primary" :disabled="signInInfo.signedToday" @click="submitSignIn">
                {{ signInInfo.signedToday ? '今日已签到' : '立即签到' }}
              </a-button>
            </div>
            <div class="wt-sign-summary">
              <span>当前连续签到</span>
              <strong>{{ signInInfo.consecutiveDays || 0 }} <small>天</small></strong>
              <p>{{ signInInfo.signedToday ? '今日签到已完成，明天继续。' : '今天还未签到。' }}</p>
            </div>
            <div class="wt-reward-title">签到奖励</div>
            <div class="wt-reward-grid">
              <article v-for="rule in signInInfo.rules || []" :key="rule.id" :class="{ reached: (signInInfo.consecutiveDays || 0) >= rule.consecutiveDays }">
                <span>连续 {{ rule.consecutiveDays }} 天</span>
                <strong>{{ rule.couponName }}</strong>
                <small>{{ (signInInfo.consecutiveDays || 0) >= rule.consecutiveDays ? '已达成' : '未达成' }}</small>
              </article>
            </div>
        </section>

        <section v-if="activeTab === 'coupon-list'" class="wt-surface wt-coupon-wallet-surface">
          <div class="wt-section-head">
            <div>
              <h2>我的优惠券</h2>
              <p>{{ availableCouponCount }} 张可使用</p>
            </div>
            <a-button @click="loadCouponData">刷新</a-button>
          </div>
          <div v-if="userCoupons.length === 0" class="wt-empty large">
            <div><IconGift /><strong>暂无优惠券</strong></div>
          </div>
          <div v-else class="wt-user-coupon-list wt-coupon-wallet-list">
            <article v-for="coupon in userCoupons" :key="coupon.id" :class="coupon.status === '可使用' ? 'available' : 'disabled'">
              <div><strong>￥{{ money(coupon.discountAmount) }}</strong><span>满 ￥{{ money(coupon.minSpend) }} 可用</span></div>
              <div><b>{{ coupon.couponName }}</b><span>{{ coupon.status }} · {{ formatDateTime(coupon.expireTime) }} 到期</span></div>
            </article>
          </div>
        </section>

        <section v-if="activeTab === 'promotion'" class="wt-promotion-layout">
          <div class="wt-surface wt-promotion-surface">
            <div class="wt-section-head">
              <div>
                <h2>邀请好友</h2>
                <p>好友注册时填写您的邀请码，注册成功后双方奖励自动到账。</p>
              </div>
              <a-tag :color="promotionInfo.activityStatus === '启用' ? 'green' : 'gray'">
                {{ promotionInfo.activityStatus === '启用' ? '活动进行中' : '活动已暂停' }}
              </a-tag>
            </div>

            <div class="wt-invite-code-panel">
              <div>
                <span>我的邀请码</span>
                <strong>{{ promotionInfo.inviteCode || '-' }}</strong>
                <p>将邀请码发送给好友，好友在用户注册页面填写。</p>
              </div>
              <a-button type="primary" :disabled="!promotionInfo.inviteCode" @click="copyInviteCode">
                <template #icon><IconCopy /></template>
                复制邀请码
              </a-button>
            </div>

            <div class="wt-promotion-steps">
              <div><b>1</b><span><strong>分享邀请码</strong><small>发送给未注册好友</small></span></div>
              <div><b>2</b><span><strong>好友完成注册</strong><small>注册时填写邀请码</small></span></div>
              <div><b>3</b><span><strong>奖励自动到账</strong><small>可在余额明细查看</small></span></div>
            </div>

          </div>

          <aside class="wt-rail wt-promotion-rail">
            <div class="wt-rail-head"><div><h2>推广收益</h2><p>奖励进入账户余额</p></div></div>
            <div class="wt-promotion-summary">
              <div><span>成功邀请</span><strong>{{ promotionInfo.invitedCount || 0 }} 人</strong></div>
              <div><span>累计奖励</span><strong>￥{{ money(promotionInfo.totalReward) }}</strong></div>
            </div>
            <div class="wt-promotion-rule">
              <h3>当前奖励</h3>
              <div><span>您可获得</span><strong>￥{{ money(promotionInfo.inviterReward) }}</strong></div>
              <div><span>好友可获得</span><strong>￥{{ money(promotionInfo.inviteeReward) }}</strong></div>
              <p>奖励金额以好友注册成功时的活动规则为准。</p>
            </div>
          </aside>
        </section>

        <section v-if="activeTab === 'promotion-record'" class="wt-surface wt-record-surface wt-promotion-record-surface">
          <div class="wt-record-summary wt-promotion-record-summary">
            <div>
              <span>成功邀请</span>
              <strong>{{ promotionInfo.invitedCount || 0 }} 人</strong>
            </div>
            <div>
              <span>累计奖励</span>
              <strong>￥{{ money(promotionInfo.totalReward) }}</strong>
            </div>
          </div>

          <div class="wt-record-toolbar">
            <div>
              <h2>邀请记录</h2>
              <p>查看好友注册与奖励到账记录</p>
            </div>
            <a-button @click="loadPromotionData">刷新</a-button>
          </div>

          <a-table
            class="no-wrap-table wt-record-table"
            :columns="promotionColumns"
            :data="promotionInfo.records || []"
            row-key="id"
            :pagination="false"
            :scroll="{ x: 680 }"
          >
            <template #reward="{ record }">+￥{{ money(record.inviterReward) }}</template>
            <template #status="{ record }"><a-tag color="green">{{ record.status }}</a-tag></template>
            <template #createTime="{ record }">{{ formatDateTime(record.createTime) }}</template>
          </a-table>
        </section>

        <section v-if="activeTab === 'service-call'" class="wt-service-layout">
          <div class="wt-surface wt-service-form-surface">
            <div class="wt-section-head">
              <div>
                <h2>呼叫网管</h2>
                <p>提交服务需求，工作人员将在后台受理</p>
              </div>
            </div>
            <a-form :model="serviceCallForm" layout="vertical" class="wt-service-form">
              <a-form-item label="服务类型" required>
                <a-select v-model="serviceCallForm.requestCategory" placeholder="请选择服务类型">
                  <a-option value="设备协助">设备协助</a-option>
                  <a-option value="网络协助">网络协助</a-option>
                  <a-option value="商品配送">商品配送</a-option>
                  <a-option value="账户咨询">账户咨询</a-option>
                  <a-option value="其他服务">其他服务</a-option>
                </a-select>
              </a-form-item>
              <a-form-item label="所在位置" required>
                <a-input v-model="serviceCallForm.location" :max-length="100" placeholder="例如 A001、电竞一区或前台" />
              </a-form-item>
              <a-form-item label="服务说明" required>
                <a-textarea
                  v-model="serviceCallForm.description"
                  :max-length="500"
                  :auto-size="{ minRows: 5, maxRows: 8 }"
                  show-word-limit
                  placeholder="请简要说明需要工作人员协助的事项"
                />
              </a-form-item>
              <a-button type="primary" class="wt-service-submit" @click="submitServiceCall">
                <template #icon><IconCustomerService /></template>
                提交呼叫
              </a-button>
            </a-form>
          </div>
          <aside class="wt-rail wt-service-rail">
            <div class="wt-rail-head">
              <div>
                <h2>服务状态</h2>
                <p>个人工单概览</p>
              </div>
            </div>
            <div class="wt-status-list">
              <div><i class="reserved"></i><span>待处理</span><strong>{{ serviceStatusCount('待处理') }}</strong></div>
              <div><i class="using"></i><span>处理中</span><strong>{{ serviceStatusCount('处理中') }}</strong></div>
              <div><i class="free"></i><span>已完成</span><strong>{{ serviceStatusCount('已完成') }}</strong></div>
            </div>
            <a-button long @click="openServiceRecords">查看服务记录</a-button>
          </aside>
        </section>

        <section v-if="activeTab === 'service-repair'" class="wt-service-layout">
          <div class="wt-surface wt-service-form-surface">
            <div class="wt-section-head">
              <div>
                <h2>故障报修</h2>
                <p>选择故障电脑并描述问题</p>
              </div>
            </div>
            <a-form :model="serviceRepairForm" layout="vertical" class="wt-service-form">
              <a-form-item label="故障电脑" required>
                <a-select v-model="serviceRepairForm.computerId" placeholder="请选择电脑" allow-search>
                  <a-option v-for="computer in computers" :key="computer.id" :value="computer.id">
                    {{ computer.computerNo }} · {{ computer.area }}
                  </a-option>
                </a-select>
              </a-form-item>
              <a-form-item label="故障类型" required>
                <a-select v-model="serviceRepairForm.faultCategory">
                  <a-option value="电脑故障">电脑故障</a-option>
                  <a-option value="网络故障">网络故障</a-option>
                  <a-option value="外设故障">外设故障</a-option>
                  <a-option value="软件故障">软件故障</a-option>
                  <a-option value="其他故障">其他故障</a-option>
                </a-select>
              </a-form-item>
              <a-form-item label="故障说明" required>
                <a-textarea
                  v-model="serviceRepairForm.description"
                  :max-length="500"
                  :auto-size="{ minRows: 5, maxRows: 8 }"
                  show-word-limit
                  placeholder="请描述故障现象，便于工作人员提前准备"
                />
              </a-form-item>
              <a-button type="primary" class="wt-service-submit" @click="submitServiceRepair">
                <template #icon><IconTool /></template>
                提交报修
              </a-button>
            </a-form>
          </div>
          <aside class="wt-rail wt-service-rail">
            <div class="wt-rail-head">
              <div>
                <h2>当前机位</h2>
                <p>报修电脑信息</p>
              </div>
            </div>
            <div class="wt-service-machine">
              <span>电脑编号</span>
              <strong>{{ selectedServiceComputer?.computerNo || '未选择' }}</strong>
              <p>{{ selectedServiceComputer?.area || '请选择需要报修的电脑' }}</p>
            </div>
            <a-button long @click="openServiceRecords">查看报修进度</a-button>
          </aside>
        </section>

        <section v-if="activeTab === 'service-record'" class="wt-surface wt-record-surface">
          <div class="wt-record-toolbar">
            <div>
              <h2>服务记录</h2>
              <p>查看呼叫与报修的处理进度</p>
            </div>
            <a-button @click="loadServiceRecords">刷新</a-button>
          </div>
          <div class="wt-record-summary wt-service-summary">
            <div><span>全部工单</span><strong>{{ serviceRecords.length }}</strong></div>
            <div><span>待处理</span><strong>{{ serviceStatusCount('待处理') }}</strong></div>
            <div><span>处理中</span><strong>{{ serviceStatusCount('处理中') }}</strong></div>
            <div><span>已完成</span><strong>{{ serviceStatusCount('已完成') }}</strong></div>
          </div>
          <a-table
            class="no-wrap-table wt-record-table"
            :columns="serviceColumns"
            :data="serviceRecords"
            row-key="id"
            :pagination="false"
            :scroll="{ x: 1100 }"
          >
            <template #serviceType="{ record }">
              <a-tag :color="record.serviceType === '呼叫网管' ? 'arcoblue' : 'orange'">{{ record.serviceType || '故障报修' }}</a-tag>
            </template>
            <template #serviceLocation="{ record }">{{ record.serviceLocation || record.computerNo || '-' }}</template>
            <template #status="{ record }"><a-tag :color="serviceStatusColor(record.status)">{{ record.status }}</a-tag></template>
            <template #createTime="{ record }">{{ formatDateTime(record.createTime) }}</template>
            <template #finishTime="{ record }">{{ formatDateTime(record.finishTime) }}</template>
          </a-table>
        </section>

        <section v-if="activeTab === 'balance'" class="wt-surface wt-record-surface">
          <div class="wt-balance-hero">
            <div>
              <span>当前可用余额</span>
              <strong>￥{{ money(currentMember?.balance) }}</strong>
              <p>充值成功后，余额可用于自助上机和点餐。</p>
            </div>
            <button type="button" class="wt-balance-recharge" @click="openRechargeModal">
              <IconPlus />
              <span>充值</span>
            </button>
          </div>

          <div class="wt-record-summary wt-balance-summary">
            <div>
              <span>余额变动</span>
              <strong>{{ balanceDetails.length }} 条</strong>
            </div>
            <div>
              <span>余额消费</span>
              <strong>￥{{ totalSpent }}</strong>
            </div>
          </div>

          <div class="wt-record-toolbar">
            <div>
              <h2>余额明细</h2>
              <p>查看充值与消费记录</p>
            </div>
          </div>

          <a-table
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

        </section>

        <section v-if="activeTab === 'voucher'" class="wt-surface wt-voucher-surface">
          <div class="wt-section-head">
            <div>
              <h2>团购验券</h2>
              <p>核销后台已录入的有效券码</p>
            </div>
            <a-button @click="loadUserRecords">刷新余额</a-button>
          </div>

          <div class="wt-voucher-body">
            <form class="wt-voucher-form" @submit.prevent="submitVoucherRecharge">
              <label for="voucher-code">团购券码</label>
              <a-input
                id="voucher-code"
                v-model="voucherForm.voucherCode"
                allow-clear
                size="large"
                placeholder="请输入团购券码"
              >
                <template #prefix><IconScan /></template>
              </a-input>
              <a-button type="primary" html-type="submit" size="large" :loading="voucherSubmitting">
                核销并充值
              </a-button>
            </form>

            <aside class="wt-voucher-account" aria-label="核销账户">
              <span>当前余额</span>
              <strong>￥{{ money(currentMember?.balance) }}</strong>
              <dl>
                <div><dt>核销账户</dt><dd>{{ currentMember?.name || '-' }}</dd></div>
                <div><dt>会员级别</dt><dd>{{ currentMember?.memberLevel || '-' }}</dd></div>
              </dl>
            </aside>
          </div>
        </section>
      </main>
    </div>

    <a-modal v-model:visible="reservationVisible" title="预约电脑" @ok="submitReservation">
      <a-form :model="reservationForm" layout="vertical">
        <a-form-item label="电脑编号">
          <a-input :model-value="selectedComputer?.computerNo" disabled />
        </a-form-item>
        <a-form-item label="预约会员">
          <a-input :model-value="currentMember ? `${currentMember.name}（${currentMember.phone}）` : '当前登录用户'" disabled />
        </a-form-item>
        <a-form-item label="预约保留至">
          <a-date-picker v-model="reservationForm.reserveTime" show-time format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
          <div class="wt-form-tip">请选择最近 1 小时内的保留截止时间，并在此时间前点击“预约上机”。</div>
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

    <a-modal v-model:visible="repairVisible" title="机器报修" :on-before-ok="submitRepair">
      <a-form :model="repairForm" layout="vertical">
        <a-form-item label="报修电脑">
          <a-input :model-value="currentRunningRecord?.computerNo" disabled />
        </a-form-item>
        <a-form-item label="故障说明" required>
          <a-textarea
            v-model="repairForm.problemDescription"
            :max-length="500"
            show-word-limit
            placeholder="请描述键盘、鼠标、显示器或系统故障"
          />
        </a-form-item>
        <div class="wt-dialog-notice">提交后管理员会尽快处理，故障期间可联系前台协助。</div>
      </a-form>
    </a-modal>

    <a-modal
      v-model:visible="paymentDialog.visible"
      :title="paymentDialog.paymentMethod"
      width="min(440px, calc(100vw - 24px))"
      :footer="false"
      :mask-closable="false"
      :closable="paymentDialog.status !== '已支付'"
      :esc-to-close="paymentDialog.status !== '已支付'"
      @cancel="handlePaymentDialogCancel"
    >
      <div v-if="paymentDialog.status === '已支付'" class="wt-payment-success">
        <div class="wt-payment-success-icon"><IconCheckCircleFill /></div>
        <strong>支付成功</strong>
        <span>订单已提交，可在“自助点餐 - 订单记录”中查看。</span>
        <div class="wt-payment-success-amount">
          <span>支付金额</span>
          <strong>￥{{ money(paymentDialog.amount) }}</strong>
        </div>
        <a-button type="primary" long @click="confirmPaymentSuccess">确认</a-button>
      </div>
      <div v-else class="wt-payment-dialog">
        <div class="wt-payment-amount">
          <span>待支付金额</span>
          <strong>￥{{ money(paymentDialog.amount) }}</strong>
          <small>支付单号：{{ paymentDialog.outTradeNo }}</small>
        </div>
        <div v-if="paymentDialog.paymentMethod === '微信支付'" class="wt-payment-qr">
          <img :src="paymentQrCodeUrl" alt="微信支付二维码" />
          <strong>请使用微信扫码支付</strong>
          <span>支付完成后页面会自动更新</span>
        </div>
        <div v-else class="wt-alipay-waiting">
          <div class="wt-alipay-mark">支</div>
          <strong>请在支付宝收银台完成支付</strong>
          <span>收银台未打开时，可点击下方按钮重新打开。</span>
          <a-button type="primary" @click="openAlipayCashier">打开支付宝收银台</a-button>
        </div>
        <div class="wt-payment-state">
          <span>支付状态</span>
          <a-tag :color="paymentStatusColor(paymentDialog.status)">{{ paymentDialog.status }}</a-tag>
        </div>
        <a-button long :loading="paymentChecking" @click="checkPaymentStatus(true)">查询支付结果</a-button>
      </div>
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
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import {
  IconApps,
  IconCalendarClock,
  IconCheckCircleFill,
  IconCopy,
  IconCustomerService,
  IconDesktop,
  IconDown,
  IconExclamationCircleFill,
  IconGift,
  IconHistory,
  IconInfoCircle,
  IconList,
  IconLiveBroadcast,
  IconMinus,
  IconPlus,
  IconPoweroff,
  IconSafe,
  IconScan,
  IconSettings,
  IconTool,
  IconUserAdd
} from '@arco-design/web-vue/es/icon'
import { getComputerList } from '../../api/computer'
import {
  addFoodOrderBatch,
  getAlipayPaymentPageUrl,
  getAvailableFoodItems,
  getPaymentStatus,
  getWechatPaymentQrCodeUrl
} from '../../api/food'
import { getSignInStatus, getUserCoupons, userSignIn } from '../../api/coupon'
import { getMemberList } from '../../api/member'
import { endOnline, startOnline } from '../../api/online'
import { addRecharge } from '../../api/recharge'
import { getRepairList, reportRepair } from '../../api/repair'
import { getPromotionOverview } from '../../api/promotion'
import { addReservation, cancelReservation, getReservationList, startReservation } from '../../api/reservation'
import { changeUserPassword, getUserBalanceDetail, getUserOnlineRecords, getUserOrders } from '../../api/user'
import { redeemVoucher } from '../../api/voucher'
import { formatDateTime } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const activeTab = ref('seat')
const onlineMenuExpanded = ref(true)
const foodMenuExpanded = ref(false)
const activityMenuExpanded = ref(false)
const promotionMenuExpanded = ref(false)
const serviceMenuExpanded = ref(false)
const balanceMenuExpanded = ref(false)
const selectedFoodCategory = ref('全部')
const currentMember = ref(getStoredUser())
const computers = ref([])
const foodItems = ref([])
const balanceDetails = ref([])
const onlineRecords = ref([])
const orders = ref([])
const reservations = ref([])
const serviceRecords = ref([])
const userCoupons = ref([])
const signInInfo = ref({ signedToday: false, consecutiveDays: 0, rules: [] })
const promotionInfo = ref({
  inviteCode: '', inviterReward: 0, inviteeReward: 0, activityStatus: '停用',
  invitedCount: 0, totalReward: 0, records: []
})
const selectedCouponId = ref(null)
const paymentMethod = ref('余额支付')
const paymentMethods = ['余额支付', '微信支付', '支付宝支付']
const foodCart = reactive({})
const now = ref(new Date())
const reservationVisible = ref(false)
const passwordVisible = ref(false)
const rechargeVisible = ref(false)
const repairVisible = ref(false)
const paymentSubmitting = ref(false)
const paymentChecking = ref(false)
const paymentDialog = reactive({
  visible: false,
  outTradeNo: '',
  orderBatchNo: '',
  paymentMethod: '',
  amount: 0,
  status: '待支付'
})
const rechargeSubmitting = ref(false)
const rechargeSubmitted = ref(false)
const voucherSubmitting = ref(false)
const selectedComputer = ref(null)
const reservationForm = reactive({ reserveTime: '' })
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const rechargeForm = reactive({ amount: 50 })
const voucherForm = reactive({ voucherCode: '' })
const repairForm = reactive({ problemDescription: '' })
const serviceCallForm = reactive({ requestCategory: '设备协助', location: '', description: '' })
const serviceRepairForm = reactive({ computerId: null, faultCategory: '电脑故障', description: '' })
const rechargeOptions = [20, 50, 100, 200]
let timer = null
let runningRefreshTimer = null
let paymentPollTimer = null

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

const reservationColumns = [
  { title: '电脑编号', dataIndex: 'computerNo', width: 130 },
  { title: '预约保留至', slotName: 'reserveTime', width: 200 },
  { title: '状态', slotName: 'status', width: 110 },
  { title: '创建时间', slotName: 'createTime', width: 200 },
  { title: '操作', slotName: 'reservationActions', width: 240 }
]

const orderColumns = [
  { title: '商品', dataIndex: 'foodItemName', width: 170 },
  { title: '单价', dataIndex: 'price', width: 100 },
  { title: '数量', dataIndex: 'quantity', width: 90 },
  { title: '优惠金额', dataIndex: 'discountAmount', width: 110 },
  { title: '优惠券', slotName: 'coupon', width: 160 },
  { title: '总金额', dataIndex: 'totalAmount', width: 120 },
  { title: '支付方式', dataIndex: 'paymentMethod', width: 120 },
  { title: '支付状态', slotName: 'paymentStatus', width: 110 },
  { title: '状态', slotName: 'status', width: 110 },
  { title: '下单时间', slotName: 'createTime', width: 200 },
  { title: '操作', slotName: 'orderActions', width: 110 }
]

const promotionColumns = [
  { title: '好友', dataIndex: 'inviteeMemberName', width: 140 },
  { title: '我的奖励', slotName: 'reward', width: 130 },
  { title: '状态', slotName: 'status', width: 100 },
  { title: '邀请成功时间', slotName: 'createTime', width: 190 }
]

const serviceColumns = [
  { title: '服务类型', slotName: 'serviceType', width: 120 },
  { title: '电脑编号', dataIndex: 'computerNo', width: 110 },
  { title: '所在位置', slotName: 'serviceLocation', width: 140 },
  { title: '服务内容', dataIndex: 'problemDescription', width: 300 },
  { title: '状态', slotName: 'status', width: 100 },
  { title: '处理说明', dataIndex: 'processRemark', width: 220 },
  { title: '提交时间', slotName: 'createTime', width: 190 },
  { title: '结束时间', slotName: 'finishTime', width: 190 }
]

function getStoredUser() {
  const text = localStorage.getItem('user')
  return text ? JSON.parse(text) : null
}

const discountText = computed(() => {
  if (!currentMember.value) return '-'
  if (currentMember.value.memberLevel === '钻石会员') return '上机 8 折'
  if (currentMember.value.memberLevel === '黄金会员') return '上机 9 折'
  if (currentMember.value.memberLevel === '普通会员') return '上机不打折'
  return '无折扣'
})

const isOnlineServicePage = computed(() => ['seat', 'current-online', 'reservation-list', 'online-history'].includes(activeTab.value))
const isFoodPage = computed(() => activeTab.value === 'food' || activeTab.value === 'food-order')
const isActivityPage = computed(() => activeTab.value === 'coupon' || activeTab.value === 'coupon-list')
const isPromotionPage = computed(() => activeTab.value === 'promotion' || activeTab.value === 'promotion-record')
const isServicePage = computed(() => ['service-call', 'service-repair', 'service-record'].includes(activeTab.value))
const isBalancePage = computed(() => activeTab.value === 'balance' || activeTab.value === 'voucher')

const pageTitle = computed(() => {
  if (activeTab.value === 'seat') return '机位大厅'
  if (activeTab.value === 'current-online') return '当前上机'
  if (activeTab.value === 'reservation-list') return '我的预约'
  if (activeTab.value === 'online-history') return '上机记录'
  if (activeTab.value === 'food') return '自助点餐'
  if (activeTab.value === 'food-order') return '订单记录'
  if (activeTab.value === 'coupon') return '每日签到'
  if (activeTab.value === 'coupon-list') return '我的优惠券'
  if (activeTab.value === 'promotion') return '邀请好友'
  if (activeTab.value === 'promotion-record') return '邀请记录'
  if (activeTab.value === 'service-call') return '呼叫网管'
  if (activeTab.value === 'service-repair') return '故障报修'
  if (activeTab.value === 'service-record') return '服务记录'
  if (activeTab.value === 'balance') return '余额账户'
  if (activeTab.value === 'voucher') return '团购验券'
  return '座位与上机'
})

const pageSubtitle = computed(() => {
  if (activeTab.value === 'seat') return '查看电脑状态并选择空闲机位'
  if (activeTab.value === 'current-online') return '查看实时计费信息并进行下机操作'
  if (activeTab.value === 'reservation-list') return '管理预约并在保留时间内上机'
  if (activeTab.value === 'online-history') return '查看历史上机与扣费记录'
  if (activeTab.value === 'food') return '选择商品，确认清单后下单'
  if (activeTab.value === 'food-order') return '查看点餐订单、支付状态和处理进度'
  if (activeTab.value === 'coupon') return '每日签到，连续天数越多奖励越高'
  if (activeTab.value === 'coupon-list') return '查看可用优惠券和使用期限'
  if (activeTab.value === 'promotion') return '邀请好友注册，双方领取余额奖励'
  if (activeTab.value === 'promotion-record') return '查看邀请成功记录和奖励明细'
  if (activeTab.value === 'service-call') return '提交服务需求，工作人员将及时处理'
  if (activeTab.value === 'service-repair') return '提交电脑故障并查看处理进度'
  if (activeTab.value === 'service-record') return '查看全部服务工单和处理结果'
  if (activeTab.value === 'balance') return '充值余额并查看账户明细'
  if (activeTab.value === 'voucher') return '核销有效团购券，金额自动充入余额'
  return ''
})

const currentRunningRecord = computed(() => onlineRecords.value.find((item) => item.status === '进行中'))
const onlineHistoryRecords = computed(() => onlineRecords.value.filter((item) => item.status !== '进行中'))
const currentReservation = computed(() => reservations.value.find((item) => item.status === '已预约'))
const ownReservationMap = computed(() => new Map(reservations.value
  .filter((item) => item.status === '已预约')
  .map((item) => [item.computerId, item])))
const activeComputer = computed(() => computers.value.find((item) => item.id === currentRunningRecord.value?.computerId))
const selectedServiceComputer = computed(() => computers.value.find((item) => item.id === serviceRepairForm.computerId))
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
const cartItems = computed(() => foodItems.value
  .filter((item) => Number(foodCart[item.id] || 0) > 0)
  .map((item) => {
    const quantity = Number(foodCart[item.id] || 0)
    const total = (Number(item.price || 0) * quantity).toFixed(2)
    return { ...item, quantity, total }
  }))
const cartItemCount = computed(() => cartItems.value.reduce((sum, item) => sum + item.quantity, 0))
const cartTotal = computed(() => cartItems.value.reduce((sum, item) => sum + Number(item.total), 0).toFixed(2))
const eligibleCoupons = computed(() => userCoupons.value.filter((coupon) =>
  coupon.status === '可使用' && Number(cartTotal.value) >= Number(coupon.minSpend || 0)))
const selectedCoupon = computed(() => eligibleCoupons.value.find((coupon) => coupon.id === selectedCouponId.value))
const cartDiscount = computed(() => selectedCoupon.value
  ? Math.min(Number(selectedCoupon.value.discountAmount || 0), Number(cartTotal.value)).toFixed(2) : '0.00')
const cartPayable = computed(() => Math.max(0, Number(cartTotal.value) - Number(cartDiscount.value)).toFixed(2))
const paymentQrCodeUrl = computed(() => paymentDialog.outTradeNo
  ? getWechatPaymentQrCodeUrl(paymentDialog.outTradeNo) : '')
const availableCouponCount = computed(() => userCoupons.value.filter((coupon) => coupon.status === '可使用').length)
const freeCount = computed(() => computers.value.filter((item) => item.status === '空闲').length)
const usingCount = computed(() => computers.value.filter((item) => item.status === '使用中').length)
const reservedCount = computed(() => computers.value.filter((item) => item.status === '预约锁定').length)
const repairCount = computed(() => computers.value.filter((item) => item.status === '维修中').length)
const totalSpent = computed(() => balanceDetails.value
  .reduce((sum, item) => {
    const amount = Number(item.amount || 0)
    return amount < 0 ? sum + Math.abs(amount) : sum
  }, 0)
  .toFixed(2))

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
  return 1
})
const estimatedCurrentAmount = computed(() => {
  if (currentRunningRecord.value?.currentAmount !== undefined && currentRunningRecord.value?.currentAmount !== null) {
    return Number(currentRunningRecord.value.currentAmount).toFixed(2)
  }
  return Number(currentRunningRecord.value?.totalAmount || 0).toFixed(2)
})
const pendingChargeAmount = computed(() => {
  if (currentRunningRecord.value?.balanceEnough !== undefined) return 0
  const price = Number(activeComputer.value?.pricePerHour || 0)
  const nextAmount = price
    ? price * (chargeHours.value + 1) * discountRate.value
    : Number(estimatedCurrentAmount.value || 0)
  const paidAmount = Number(currentRunningRecord.value?.totalAmount || 0)
  return Math.max(0, nextAmount - paidAmount)
})
const balanceEnough = computed(() => {
  if (currentRunningRecord.value?.balanceEnough !== undefined && currentRunningRecord.value?.balanceEnough !== null) {
    return Boolean(currentRunningRecord.value.balanceEnough)
  }
  return Number(currentMember.value?.balance || 0) >= pendingChargeAmount.value
})
const balanceWarningText = computed(() => currentRunningRecord.value?.warningMessage || '余额不足以支付下一计费小时，请及时充值。')

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

function money(value) {
  const number = Number(value || 0)
  return Number.isInteger(number) ? String(number) : number.toFixed(2)
}

function seatClass(status) {
  if (status === '空闲') return 'is-free'
  if (status === '使用中') return 'is-using'
  if (status === '预约锁定') return 'is-reserved'
  return 'is-repair'
}

function getFoodCategory(item) {
  if (item.category) return item.category
  const text = `${item.name || ''}${item.remark || ''}`
  if (/可乐|雪碧|水|茶|咖啡|饮|奶|汁/.test(text)) return '饮料'
  if (/泡面|饭|面|餐|肠|热狗|汉堡/.test(text)) return '餐食'
  if (/薯片|饼干|糖|巧克力|零食/.test(text)) return '零食'
  return '其他'
}

function orderStatusColor(status) {
  if (status === '已下单') return 'blue'
  if (status === '已完成') return 'green'
  if (status === '已取消') return 'gray'
  if (status === '退款中') return 'orange'
  return 'orange'
}

function paymentStatusColor(status) {
  if (status === '已支付') return 'green'
  if (status === '部分退款') return 'orange'
  if (status === '退款中') return 'orange'
  if (status === '已退款' || status === '已关闭') return 'gray'
  return 'blue'
}

function reservationStatusColor(status) {
  if (status === '已预约') return 'purple'
  if (status === '已上机') return 'green'
  return 'gray'
}

function serviceStatusColor(status) {
  if (status === '待处理') return 'orange'
  if (status === '处理中') return 'blue'
  if (status === '已完成') return 'green'
  return 'gray'
}

function serviceStatusCount(status) {
  return serviceRecords.value.filter((item) => item.status === status).length
}

async function loadData() {
  computers.value = await getComputerList({})
  foodItems.value = await getAvailableFoodItems()
  await loadUserRecords()
  await loadCouponData()
  await loadPromotionData()
}

async function loadUserRecords() {
  if (!currentMember.value?.id) return
  onlineRecords.value = await getUserOnlineRecords(currentMember.value.id)
  balanceDetails.value = await getUserBalanceDetail(currentMember.value.id)
  orders.value = await getUserOrders(currentMember.value.id)
  reservations.value = await getReservationList({ memberId: currentMember.value.id })
  await loadServiceRecords()
}

async function loadServiceRecords() {
  if (!currentMember.value?.id) return
  serviceRecords.value = await getRepairList({ memberId: currentMember.value.id })
}

async function loadCouponData() {
  if (!currentMember.value?.id) return
  signInInfo.value = await getSignInStatus(currentMember.value.id)
  userCoupons.value = await getUserCoupons(currentMember.value.id)
}

async function loadPromotionData() {
  if (!currentMember.value?.id) return
  promotionInfo.value = await getPromotionOverview(currentMember.value.id)
}

async function copyInviteCode() {
  const inviteCode = promotionInfo.value.inviteCode
  if (!inviteCode) return
  try {
    await navigator.clipboard.writeText(inviteCode)
  } catch (error) {
    const input = document.createElement('input')
    input.value = inviteCode
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    input.remove()
  }
  Message.success('邀请码已复制')
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

function toggleOnlineMenu() {
  onlineMenuExpanded.value = !onlineMenuExpanded.value
  if (onlineMenuExpanded.value) {
    foodMenuExpanded.value = false
    activityMenuExpanded.value = false
    promotionMenuExpanded.value = false
    serviceMenuExpanded.value = false
    balanceMenuExpanded.value = false
  }
}

function openMachineHall() {
  onlineMenuExpanded.value = true
  foodMenuExpanded.value = false
  activityMenuExpanded.value = false
  promotionMenuExpanded.value = false
  serviceMenuExpanded.value = false
  balanceMenuExpanded.value = false
  activeTab.value = 'seat'
}

function openCurrentOnline() {
  onlineMenuExpanded.value = true
  foodMenuExpanded.value = false
  activityMenuExpanded.value = false
  promotionMenuExpanded.value = false
  serviceMenuExpanded.value = false
  balanceMenuExpanded.value = false
  activeTab.value = 'current-online'
  refreshRunningData()
}

function openMyReservations() {
  onlineMenuExpanded.value = true
  foodMenuExpanded.value = false
  activityMenuExpanded.value = false
  promotionMenuExpanded.value = false
  serviceMenuExpanded.value = false
  balanceMenuExpanded.value = false
  activeTab.value = 'reservation-list'
  loadUserRecords()
}

function openOnlineHistory() {
  onlineMenuExpanded.value = true
  foodMenuExpanded.value = false
  activityMenuExpanded.value = false
  promotionMenuExpanded.value = false
  serviceMenuExpanded.value = false
  balanceMenuExpanded.value = false
  activeTab.value = 'online-history'
  loadUserRecords()
}

function openFoodProducts() {
  onlineMenuExpanded.value = false
  foodMenuExpanded.value = true
  activityMenuExpanded.value = false
  promotionMenuExpanded.value = false
  serviceMenuExpanded.value = false
  balanceMenuExpanded.value = false
  activeTab.value = 'food'
}

function openFoodOrders() {
  onlineMenuExpanded.value = false
  foodMenuExpanded.value = true
  activityMenuExpanded.value = false
  promotionMenuExpanded.value = false
  serviceMenuExpanded.value = false
  balanceMenuExpanded.value = false
  activeTab.value = 'food-order'
  loadUserRecords()
}

function toggleFoodMenu() {
  foodMenuExpanded.value = !foodMenuExpanded.value
  if (foodMenuExpanded.value) {
    onlineMenuExpanded.value = false
    activityMenuExpanded.value = false
    promotionMenuExpanded.value = false
    serviceMenuExpanded.value = false
    balanceMenuExpanded.value = false
  }
}

function toggleActivityMenu() {
  activityMenuExpanded.value = !activityMenuExpanded.value
  if (activityMenuExpanded.value) {
    onlineMenuExpanded.value = false
    foodMenuExpanded.value = false
    promotionMenuExpanded.value = false
    serviceMenuExpanded.value = false
    balanceMenuExpanded.value = false
  }
}

function togglePromotionMenu() {
  promotionMenuExpanded.value = !promotionMenuExpanded.value
  if (promotionMenuExpanded.value) {
    onlineMenuExpanded.value = false
    foodMenuExpanded.value = false
    activityMenuExpanded.value = false
    serviceMenuExpanded.value = false
    balanceMenuExpanded.value = false
  }
}

function toggleServiceMenu() {
  serviceMenuExpanded.value = !serviceMenuExpanded.value
  if (serviceMenuExpanded.value) {
    onlineMenuExpanded.value = false
    foodMenuExpanded.value = false
    activityMenuExpanded.value = false
    promotionMenuExpanded.value = false
    balanceMenuExpanded.value = false
  }
}

function toggleBalanceMenu() {
  balanceMenuExpanded.value = !balanceMenuExpanded.value
  if (balanceMenuExpanded.value) {
    onlineMenuExpanded.value = false
    foodMenuExpanded.value = false
    activityMenuExpanded.value = false
    promotionMenuExpanded.value = false
    serviceMenuExpanded.value = false
  }
}

function openDailySignIn() {
  onlineMenuExpanded.value = false
  activityMenuExpanded.value = true
  foodMenuExpanded.value = false
  promotionMenuExpanded.value = false
  serviceMenuExpanded.value = false
  balanceMenuExpanded.value = false
  activeTab.value = 'coupon'
  loadCouponData()
}

function openMyCoupons() {
  onlineMenuExpanded.value = false
  activityMenuExpanded.value = true
  foodMenuExpanded.value = false
  promotionMenuExpanded.value = false
  serviceMenuExpanded.value = false
  balanceMenuExpanded.value = false
  activeTab.value = 'coupon-list'
  loadCouponData()
}

function openPromotionInvite() {
  onlineMenuExpanded.value = false
  promotionMenuExpanded.value = true
  foodMenuExpanded.value = false
  activityMenuExpanded.value = false
  serviceMenuExpanded.value = false
  balanceMenuExpanded.value = false
  activeTab.value = 'promotion'
  loadPromotionData()
}

function openPromotionRecords() {
  onlineMenuExpanded.value = false
  promotionMenuExpanded.value = true
  foodMenuExpanded.value = false
  activityMenuExpanded.value = false
  serviceMenuExpanded.value = false
  balanceMenuExpanded.value = false
  activeTab.value = 'promotion-record'
  loadPromotionData()
}

function openBalancePage() {
  onlineMenuExpanded.value = false
  balanceMenuExpanded.value = true
  foodMenuExpanded.value = false
  activityMenuExpanded.value = false
  promotionMenuExpanded.value = false
  serviceMenuExpanded.value = false
  activeTab.value = 'balance'
  loadUserRecords()
}

function openVoucherPage() {
  onlineMenuExpanded.value = false
  balanceMenuExpanded.value = true
  foodMenuExpanded.value = false
  activityMenuExpanded.value = false
  promotionMenuExpanded.value = false
  serviceMenuExpanded.value = false
  activeTab.value = 'voucher'
  voucherForm.voucherCode = ''
  refreshCurrentMember()
}

function openServiceCall() {
  onlineMenuExpanded.value = false
  foodMenuExpanded.value = false
  activityMenuExpanded.value = false
  promotionMenuExpanded.value = false
  serviceMenuExpanded.value = true
  balanceMenuExpanded.value = false
  activeTab.value = 'service-call'
  if (!serviceCallForm.location && currentRunningRecord.value) {
    serviceCallForm.location = `${currentRunningRecord.value.computerNo} 机位`
  }
  loadServiceRecords()
}

function openServiceRepair() {
  onlineMenuExpanded.value = false
  foodMenuExpanded.value = false
  activityMenuExpanded.value = false
  promotionMenuExpanded.value = false
  serviceMenuExpanded.value = true
  balanceMenuExpanded.value = false
  activeTab.value = 'service-repair'
  if (!serviceRepairForm.computerId && currentRunningRecord.value) {
    serviceRepairForm.computerId = currentRunningRecord.value.computerId
  }
  loadServiceRecords()
}

function openServiceRecords() {
  onlineMenuExpanded.value = false
  foodMenuExpanded.value = false
  activityMenuExpanded.value = false
  promotionMenuExpanded.value = false
  serviceMenuExpanded.value = true
  balanceMenuExpanded.value = false
  activeTab.value = 'service-record'
  loadServiceRecords()
}

async function submitServiceCall() {
  if (!checkUserCanOperate()) return
  if (!serviceCallForm.location.trim()) {
    Message.error('请填写所在位置')
    return
  }
  if (!serviceCallForm.description.trim()) {
    Message.error('请填写服务说明')
    return
  }
  await reportRepair({
    computerId: currentRunningRecord.value?.computerId || null,
    memberId: currentMember.value.id,
    serviceType: '呼叫网管',
    serviceLocation: serviceCallForm.location,
    problemDescription: `[${serviceCallForm.requestCategory}] ${serviceCallForm.description.trim()}`
  })
  Message.success('呼叫已提交，请留意处理状态')
  serviceCallForm.description = ''
  await loadServiceRecords()
  openServiceRecords()
}

async function submitServiceRepair() {
  if (!checkUserCanOperate()) return
  if (!serviceRepairForm.computerId) {
    Message.error('请选择故障电脑')
    return
  }
  if (!serviceRepairForm.description.trim()) {
    Message.error('请填写故障说明')
    return
  }
  await reportRepair({
    computerId: serviceRepairForm.computerId,
    memberId: currentMember.value.id,
    serviceType: '故障报修',
    serviceLocation: selectedServiceComputer.value?.computerNo || '',
    problemDescription: `[${serviceRepairForm.faultCategory}] ${serviceRepairForm.description.trim()}`
  })
  Message.success('报修已提交，请留意处理状态')
  serviceRepairForm.description = ''
  await loadServiceRecords()
  openServiceRecords()
}

function openPasswordModal() {
  Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
  passwordVisible.value = true
}

function openRechargeModal() {
  rechargeForm.amount = 50
  rechargeSubmitted.value = false
  rechargeVisible.value = true
}

async function submitRecharge() {
  const amount = Number(rechargeForm.amount)
  if (!Number.isFinite(amount) || amount <= 0) {
    Message.error('充值金额必须大于 0')
    return false
  }
  if (amount > 10000) {
    Message.error('单次充值不能超过 10000 元')
    return false
  }
  if (rechargeSubmitting.value || rechargeSubmitted.value) return false

  rechargeSubmitting.value = true
  rechargeSubmitted.value = true
  try {
    await addRecharge({ memberId: currentMember.value.id, amount })
    await refreshCurrentMember()
    await loadUserRecords()
    Message.success(`充值 ${money(amount)} 元成功`)
    return true
  } catch (error) {
    rechargeSubmitted.value = false
    return false
  } finally {
    rechargeSubmitting.value = false
  }
}

async function submitVoucherRecharge() {
  const voucherCode = voucherForm.voucherCode.trim()
  if (!voucherCode) {
    Message.error('请输入团购券码')
    return false
  }
  if (voucherSubmitting.value) return false
  voucherSubmitting.value = true
  try {
    await redeemVoucher({ memberId: String(currentMember.value.id), voucherCode })
    await refreshCurrentMember()
    await loadUserRecords()
    voucherForm.voucherCode = ''
    Message.success('验券成功，余额已到账')
    return true
  } catch (error) {
    return false
  } finally {
    voucherSubmitting.value = false
  }
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
  Object.keys(foodCart).forEach((key) => delete foodCart[key])
  selectedCouponId.value = null
}

async function submitCartOrder() {
  if (!checkUserCanOperate()) return
  if (cartItems.value.length === 0) {
    Message.warning('请先选择商品')
    return
  }
  if (selectedCouponId.value && !selectedCoupon.value) {
    Message.error('当前订单金额未达到所选优惠券使用门槛')
    return
  }
  if (paymentMethod.value === '余额支付' && Number(currentMember.value?.balance || 0) < Number(cartPayable.value)) {
    Message.error('会员余额不足，请先充值')
    return
  }
  if (paymentSubmitting.value) return
  paymentSubmitting.value = true
  const selectedMethod = paymentMethod.value
  const alipayWindow = selectedMethod === '支付宝支付' ? window.open('about:blank', '_blank') : null
  try {
    const result = await addFoodOrderBatch({
      memberId: currentMember.value.id,
      userCouponId: selectedCouponId.value,
      paymentMethod: selectedMethod,
      items: cartItems.value.map((item) => ({ foodItemId: item.id, quantity: item.quantity }))
    })
    const discountMessage = Number(cartDiscount.value) > 0 ? `，已优惠 ${cartDiscount.value} 元` : ''
    clearCart()
    if (selectedMethod === '余额支付') {
      if (alipayWindow) alipayWindow.close()
      Message.success(`下单成功${discountMessage}`)
      await refreshCurrentMember()
      await loadUserRecords()
      await loadCouponData()
      return
    }
    if (result.status === '已支付') {
      if (alipayWindow) alipayWindow.close()
      showPaymentSuccess(result)
      await refreshPaymentData()
      return
    }
    openPaymentDialog(result)
    if (selectedMethod === '支付宝支付') {
      const cashierUrl = getAlipayPaymentPageUrl(result.outTradeNo)
      if (alipayWindow) alipayWindow.location.href = cashierUrl
      else Message.warning('浏览器拦截了收银台窗口，请点击“打开支付宝收银台”')
    }
    await loadUserRecords()
    await loadCouponData()
  } catch (error) {
    if (alipayWindow) alipayWindow.close()
  } finally {
    paymentSubmitting.value = false
  }
}

function openPaymentDialog(payment) {
  Object.assign(paymentDialog, {
    visible: true,
    outTradeNo: payment.outTradeNo,
    orderBatchNo: payment.orderBatchNo,
    paymentMethod: payment.paymentMethod,
    amount: payment.amount,
    status: payment.status || '待支付'
  })
  if (paymentDialog.status === '已支付') {
    showPaymentSuccess(payment)
  } else {
    startPaymentPolling()
  }
}

function resumePayment(order) {
  openPaymentDialog({
    outTradeNo: order.paymentOutTradeNo,
    orderBatchNo: order.batchNo,
    paymentMethod: order.paymentMethod,
    amount: order.paymentAmount || order.totalAmount,
    status: order.paymentStatus || '待支付'
  })
  if (order.paymentMethod === '支付宝支付') openAlipayCashier()
}

function openAlipayCashier() {
  if (!paymentDialog.outTradeNo) return
  window.open(getAlipayPaymentPageUrl(paymentDialog.outTradeNo), '_blank')
}

function startPaymentPolling() {
  stopPaymentPolling()
  paymentPollTimer = window.setInterval(() => checkPaymentStatus(false), 3000)
}

function stopPaymentPolling() {
  if (paymentPollTimer) window.clearInterval(paymentPollTimer)
  paymentPollTimer = null
}

function showPaymentSuccess(payment = {}) {
  stopPaymentPolling()
  Object.assign(paymentDialog, {
    visible: true,
    outTradeNo: payment.outTradeNo || paymentDialog.outTradeNo,
    orderBatchNo: payment.orderBatchNo || paymentDialog.orderBatchNo,
    paymentMethod: payment.paymentMethod || paymentDialog.paymentMethod || '在线支付',
    amount: payment.amount ?? paymentDialog.amount,
    status: '已支付'
  })
  onlineMenuExpanded.value = false
  foodMenuExpanded.value = true
  activityMenuExpanded.value = false
  promotionMenuExpanded.value = false
  balanceMenuExpanded.value = false
  activeTab.value = 'food-order'
}

function confirmPaymentSuccess() {
  paymentDialog.visible = false
  openFoodOrders()
}

function handlePaymentDialogCancel() {
  if (paymentDialog.status === '已支付') {
    paymentDialog.visible = true
    return
  }
  stopPaymentPolling()
}

async function refreshPaymentData() {
  await refreshCurrentMember()
  await loadUserRecords()
  await loadCouponData()
}

async function checkPaymentStatus(manual) {
  if (!paymentDialog.outTradeNo || paymentChecking.value) return
  paymentChecking.value = true
  try {
    const result = await getPaymentStatus(paymentDialog.outTradeNo)
    paymentDialog.status = result.status
    if (result.status === '已支付') {
      showPaymentSuccess(result)
      await refreshPaymentData()
    } else if (result.status === '已关闭') {
      stopPaymentPolling()
      Message.error('支付单已超时关闭，请重新下单')
      await loadUserRecords()
      await loadCouponData()
    } else if (manual) {
      Message.info('暂未查询到支付成功，请完成付款后再试')
    }
  } catch (error) {
    stopPaymentPolling()
  } finally {
    paymentChecking.value = false
  }
}

async function handleAlipayReturn() {
  const outTradeNo = String(route.query.out_trade_no || '')
  const method = String(route.query.method || '')
  if (!outTradeNo || method !== 'alipay.trade.page.pay.return') return

  try {
    const result = await getPaymentStatus(outTradeNo)
    if (result.status === '已支付') {
      showPaymentSuccess(result)
      await refreshPaymentData()
    } else {
      Message.info('支付宝支付结果正在确认，请稍后在订单记录中查询')
      openFoodOrders()
    }
  } finally {
    await router.replace('/user')
  }
}

async function submitSignIn() {
  if (!checkUserCanOperate()) return
  const result = await userSignIn(currentMember.value.id)
  Message.success(result.message || '签到成功')
  await loadCouponData()
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
    Message.warning('您已有正在使用的电脑，请先下机')
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
  openCurrentOnline()
}

async function submitReservedOnline(reservation) {
  if (!checkUserCanOperate()) return
  if (currentRunningRecord.value) {
    Message.warning('您已有正在使用的电脑，请先下机')
    return
  }
  try {
    await startReservation(reservation.id, currentMember.value.id)
    Message.success('预约上机成功')
    await refreshCurrentMember()
  } finally {
    await loadData()
  }
  if (currentRunningRecord.value) openCurrentOnline()
}

async function submitCancelReservation(reservation) {
  if (!checkUserCanOperate()) return
  try {
    await cancelReservation(reservation.id, currentMember.value.id)
    Message.success('预约已取消')
  } finally {
    await loadData()
  }
}

async function submitEndOnline() {
  if (!currentRunningRecord.value) {
    Message.error('当前没有进行中的上机记录')
    return
  }
  await endOnline(currentRunningRecord.value.id)
  Message.success('下机成功')
  await refreshCurrentMember()
  await loadData()
  openMachineHall()
}

function openRepairModal() {
  if (!currentRunningRecord.value) {
    Message.error('当前没有可报修的上机电脑')
    return
  }
  repairForm.problemDescription = ''
  repairVisible.value = true
}

async function submitRepair() {
  if (!repairForm.problemDescription.trim()) {
    Message.error('请填写故障说明')
    return false
  }
  await reportRepair({
    computerId: currentRunningRecord.value.computerId,
    memberId: currentMember.value.id,
    problemDescription: repairForm.problemDescription
  })
  Message.success('报修已提交，管理员会尽快处理')
  await loadServiceRecords()
  return true
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
  Message.success('预约成功，请在保留时间前点击“预约上机”')
  reservationVisible.value = false
  selectedComputer.value = null
  await loadData()
  openMyReservations()
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
    Message.error('用户状态异常，请联系前台')
    return false
  }
  return true
}

onMounted(async () => {
  if (!currentMember.value) {
    router.push('/user/login')
    return
  }
  await loadData()
  await refreshCurrentMember()
  if (currentRunningRecord.value) {
    activeTab.value = 'current-online'
  } else if (currentReservation.value) {
    activeTab.value = 'reservation-list'
  }
  await handleAlipayReturn()
  timer = window.setInterval(() => {
    now.value = new Date()
  }, 1000)
  runningRefreshTimer = window.setInterval(() => {
    if (currentRunningRecord.value || currentReservation.value) refreshRunningData()
  }, 30000)
})

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
  if (runningRefreshTimer) window.clearInterval(runningRefreshTimer)
  stopPaymentPolling()
})
</script>

<style scoped src="./terminal.css"></style>
