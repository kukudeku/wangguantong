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
        <button :class="{ active: activeTab === 'seat' }" @click="activeTab = 'seat'">
          <IconDesktop />
          <span>座位上机</span>
        </button>
        <div class="wt-nav-group" :class="{ expanded: foodMenuExpanded }">
          <button
            type="button"
            class="wt-nav-parent"
            :class="{ 'child-active': isFoodPage }"
            :aria-expanded="foodMenuExpanded"
            @click="foodMenuExpanded = !foodMenuExpanded"
          >
            <IconApps />
            <span>自助点餐</span>
            <span class="wt-nav-meta">
              <em v-if="cartItemCount">{{ cartItemCount }}</em>
              <IconDown class="wt-nav-arrow" />
            </span>
          </button>
          <div v-show="foodMenuExpanded" class="wt-subnav">
            <button type="button" :class="{ active: activeTab === 'food' }" @click="openFoodProducts">
              <span>商品点餐</span>
            </button>
            <button type="button" :class="{ active: activeTab === 'food-order' }" @click="openFoodOrders">
              <span>订单记录</span>
            </button>
          </div>
        </div>
        <button :class="{ active: activeTab === 'coupon' }" @click="activeTab = 'coupon'">
          <IconGift />
          <span>签到领券</span>
        </button>
        <button :class="{ active: activeTab === 'promotion' }" @click="activeTab = 'promotion'">
          <IconUserAdd />
          <span>推广计划</span>
        </button>
        <button :class="{ active: activeTab === 'balance' }" @click="activeTab = 'balance'">
          <IconList />
          <span>余额</span>
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
        <section v-if="activeTab === 'seat'" class="wt-account-band" aria-label="账户摘要">
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
                <h2>{{ currentRunningRecord ? '当前上机电脑' : '电脑座位图' }}</h2>
                <p>{{ currentRunningRecord ? '查看实时计费信息，结束使用后请自助下机。' : currentReservation ? '在紫色的本人预约机位点击“预约上机”。' : '绿色机位可直接上机或预约。' }}</p>
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
                <a-button @click="openChangeComputerModal">
                  <template #icon><IconSwap /></template>
                  换机
                </a-button>
                <a-button status="warning" @click="openRepairModal">
                  <template #icon><IconTool /></template>
                  机器报修
                </a-button>
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
              <p>{{ paymentHint }}</p>
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

        <section v-if="activeTab === 'coupon'" class="wt-coupon-layout">
          <div class="wt-surface wt-sign-surface">
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
          </div>

          <aside class="wt-rail wt-coupon-rail">
            <div class="wt-rail-head">
              <div><h2>我的优惠券</h2><p>{{ availableCouponCount }} 张可使用</p></div>
            </div>
            <div v-if="userCoupons.length === 0" class="wt-cart-empty">
              <IconGift /><strong>暂无优惠券</strong><span>签到后可领取</span>
            </div>
            <div v-else class="wt-user-coupon-list">
              <article v-for="coupon in userCoupons" :key="coupon.id" :class="coupon.status === '可使用' ? 'available' : 'disabled'">
                <div><strong>￥{{ money(coupon.discountAmount) }}</strong><span>满 ￥{{ money(coupon.minSpend) }} 可用</span></div>
                <div><b>{{ coupon.couponName }}</b><span>{{ coupon.status }} · {{ formatDateTime(coupon.expireTime) }} 到期</span></div>
              </article>
            </div>
          </aside>
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

            <div class="wt-promotion-record-head">
              <div><h3>邀请记录</h3><p>已成功邀请 {{ promotionInfo.invitedCount || 0 }} 位好友</p></div>
              <button class="wt-text-button" @click="loadPromotionData">刷新</button>
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

          <div class="wt-record-summary">
            <div>
              <span>余额变动</span>
              <strong>{{ balanceDetails.length }} 条</strong>
            </div>
            <div>
              <span>余额消费</span>
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
              <h2>账户明细</h2>
              <p>查看充值、消费与历史记录</p>
            </div>
            <div class="wt-record-tabs">
              <button :class="{ active: recordTab === 'balance' }" @click="recordTab = 'balance'">余额明细</button>
              <button :class="{ active: recordTab === 'online' }" @click="recordTab = 'online'">上机记录</button>
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
            :scroll="{ x: 1230 }"
          >
            <template #computerHistory="{ record }">
              <div v-if="hasChangedComputer(record)" class="wt-change-record">
                <a-tag color="orange">已换机</a-tag>
                <span>{{ formatComputerHistory(record.computerHistory) }}</span>
              </div>
              <span v-else>未换机</span>
            </template>
            <template #startTime="{ record }">{{ formatDateTime(record.startTime) }}</template>
            <template #endTime="{ record }">{{ formatDateTime(record.endTime) }}</template>
            <template #status="{ record }">
              <a-tag :color="record.status === '进行中' ? 'blue' : 'green'">{{ record.status }}</a-tag>
            </template>
          </a-table>

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

    <a-modal v-model:visible="changeComputerVisible" title="更换电脑" :on-before-ok="submitChangeComputer">
      <a-form :model="changeComputerForm" layout="vertical">
        <a-form-item label="当前电脑">
          <a-input :model-value="currentRunningRecord?.computerNo" disabled />
        </a-form-item>
        <a-form-item label="目标电脑" required>
          <a-select v-model="changeComputerForm.targetComputerId" placeholder="请选择空闲电脑">
            <a-option v-for="computer in freeComputers" :key="computer.id" :value="computer.id">
              {{ computer.computerNo }}（{{ computer.area }}，￥{{ money(computer.pricePerHour) }}/小时，{{ changeFeeText(computer) }}）
            </a-option>
          </a-select>
        </a-form-item>
        <div class="wt-dialog-notice">{{ selectedChangeFeeText }}</div>
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
        <div class="wt-dialog-notice">提交后管理员会处理。如需立即换机，请先提交报修再使用换机功能。</div>
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
      :ok-text="rechargeMode === 'voucher' ? '核销并充值' : '确认充值'"
      cancel-text="取消"
      :ok-loading="rechargeSubmitting"
      :on-before-ok="submitRecharge"
    >
      <div class="wt-recharge-dialog">
        <div class="wt-recharge-balance">
          <span>当前余额</span>
          <strong>￥{{ money(currentMember?.balance) }}</strong>
        </div>
        <div class="wt-recharge-tabs">
          <button type="button" :class="{ active: rechargeMode === 'amount' }" @click="rechargeMode = 'amount'">余额充值</button>
          <button type="button" :class="{ active: rechargeMode === 'voucher' }" @click="rechargeMode = 'voucher'">团购验券</button>
        </div>
        <div v-if="rechargeMode === 'amount'" class="wt-recharge-field">
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
        <div v-if="rechargeMode === 'amount'" class="wt-recharge-field">
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
        <div v-else class="wt-recharge-field">
          <label>团购券码</label>
          <a-input v-model="rechargeForm.voucherCode" allow-clear placeholder="请输入后台已录入的团购券码" />
          <p class="wt-recharge-help">仅支持后台已录入且未核销的券码，金额以后台设置为准。</p>
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
  IconCheckCircleFill,
  IconCopy,
  IconDesktop,
  IconDown,
  IconExclamationCircleFill,
  IconGift,
  IconInfoCircle,
  IconList,
  IconMinus,
  IconPlus,
  IconPoweroff,
  IconSettings,
  IconSwap,
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
import { changeOnlineComputer, endOnline, startOnline } from '../../api/online'
import { addRecharge } from '../../api/recharge'
import { reportRepair } from '../../api/repair'
import { getPromotionOverview } from '../../api/promotion'
import { addReservation, cancelReservation, getReservationList, startReservation } from '../../api/reservation'
import { changeUserPassword, getUserBalanceDetail, getUserOnlineRecords, getUserOrders } from '../../api/user'
import { redeemVoucher } from '../../api/voucher'
import { formatDateTime } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const activeTab = ref('seat')
const recordTab = ref('balance')
const foodMenuExpanded = ref(true)
const selectedFoodCategory = ref('全部')
const currentMember = ref(getStoredUser())
const computers = ref([])
const foodItems = ref([])
const balanceDetails = ref([])
const onlineRecords = ref([])
const orders = ref([])
const reservations = ref([])
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
const changeComputerVisible = ref(false)
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
const rechargeMode = ref('amount')
const selectedComputer = ref(null)
const reservationForm = reactive({ reserveTime: '' })
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const rechargeForm = reactive({ amount: 50, voucherCode: '' })
const changeComputerForm = reactive({ targetComputerId: null })
const repairForm = reactive({ problemDescription: '' })
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
  { title: '换机记录', slotName: 'computerHistory', width: 250 },
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

const isFoodPage = computed(() => activeTab.value === 'food' || activeTab.value === 'food-order')

const pageTitle = computed(() => {
  if (activeTab.value === 'food') return '自助点餐'
  if (activeTab.value === 'food-order') return '订单记录'
  if (activeTab.value === 'coupon') return '签到领券'
  if (activeTab.value === 'promotion') return '推广计划'
  if (activeTab.value === 'balance') return '余额'
  return '座位与上机'
})

const pageSubtitle = computed(() => {
  if (activeTab.value === 'food') return '选择商品，确认清单后下单'
  if (activeTab.value === 'food-order') return '查看点餐订单、支付状态和处理进度'
  if (activeTab.value === 'coupon') return '每日签到，连续天数越多奖励越高'
  if (activeTab.value === 'promotion') return '邀请好友注册，双方领取余额奖励'
  if (activeTab.value === 'balance') return '充值余额并查看账户明细'
  if (currentRunningRecord.value) return '当前已有电脑正在使用，可在本页自助下机'
  return '选择空闲电脑，自助上机或预约'
})

const currentRunningRecord = computed(() => onlineRecords.value.find((item) => item.status === '进行中'))
const currentReservation = computed(() => reservations.value.find((item) => item.status === '已预约'))
const ownReservationMap = computed(() => new Map(reservations.value
  .filter((item) => item.status === '已预约')
  .map((item) => [item.computerId, item])))
const activeComputer = computed(() => computers.value.find((item) => item.id === currentRunningRecord.value?.computerId))
const freeComputers = computed(() => computers.value.filter((item) => item.status === '空闲'))
const selectedChangeComputer = computed(() => computers.value.find((item) => item.id === changeComputerForm.targetComputerId))
const selectedChangeFeeText = computed(() => {
  if (!selectedChangeComputer.value) return '目标机价格更高时只补小时差价，同价或更低时免费换机且不退差价。'
  const extraAmount = getChangeExtraAmount(selectedChangeComputer.value)
  return extraAmount > 0
    ? `本次换机需补差价 ￥${money(extraAmount)}，确认后从余额扣除。`
    : '本次免费换机，不额外扣费；已产生的费用不退还。'
})
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
const paymentHint = computed(() => paymentMethod.value === '余额支付'
  ? '费用将从会员余额中扣除'
  : paymentMethod.value === '微信支付'
    ? '下单后使用微信扫码完成真实支付'
    : '下单后将打开支付宝电脑网站收银台')
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

function getChangeExtraAmount(targetComputer) {
  const currentPrice = Number(activeComputer.value?.pricePerHour || 0)
  const targetPrice = Number(targetComputer?.pricePerHour || 0)
  return Math.max(0, (targetPrice - currentPrice) * discountRate.value)
}

function changeFeeText(targetComputer) {
  const extraAmount = getChangeExtraAmount(targetComputer)
  return extraAmount > 0 ? `补 ￥${money(extraAmount)}` : '免费换机'
}

function hasChangedComputer(record) {
  return String(record?.computerHistory || '').includes('->')
}

function formatComputerHistory(history) {
  return String(history || '-').replaceAll('->', '→')
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

function openFoodProducts() {
  foodMenuExpanded.value = true
  activeTab.value = 'food'
}

function openFoodOrders() {
  foodMenuExpanded.value = true
  activeTab.value = 'food-order'
  loadUserRecords()
}

function openPasswordModal() {
  Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
  passwordVisible.value = true
}

function openRechargeModal() {
  rechargeForm.amount = 50
  rechargeForm.voucherCode = ''
  rechargeMode.value = 'amount'
  rechargeSubmitted.value = false
  rechargeVisible.value = true
}

async function submitRecharge() {
  if (rechargeMode.value === 'voucher') {
    return submitVoucherRecharge()
  }
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
  const voucherCode = rechargeForm.voucherCode.trim()
  if (!voucherCode) {
    Message.error('请输入团购券码')
    return false
  }
  if (rechargeSubmitting.value || rechargeSubmitted.value) return false
  rechargeSubmitting.value = true
  rechargeSubmitted.value = true
  try {
    await redeemVoucher({ memberId: String(currentMember.value.id), voucherCode })
    await refreshCurrentMember()
    await loadUserRecords()
    Message.success('验券成功，余额已到账')
    return true
  } catch (error) {
    rechargeSubmitted.value = false
    return false
  } finally {
    rechargeSubmitting.value = false
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
  foodMenuExpanded.value = true
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
}

function openChangeComputerModal() {
  if (!currentRunningRecord.value) {
    Message.error('当前没有进行中的上机记录')
    return
  }
  if (freeComputers.value.length === 0) {
    Message.warning('当前没有可更换的空闲电脑')
    return
  }
  changeComputerForm.targetComputerId = null
  changeComputerVisible.value = true
}

async function submitChangeComputer() {
  if (!changeComputerForm.targetComputerId) {
    Message.error('请选择目标电脑')
    return false
  }
  const result = await changeOnlineComputer({
    recordId: currentRunningRecord.value.id,
    targetComputerId: changeComputerForm.targetComputerId
  })
  Message.success(result?.message || '换机成功')
  await refreshCurrentMember()
  await loadData()
  return true
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

onMounted(() => {
  if (!currentMember.value) {
    router.push('/user/login')
    return
  }
  loadData()
  refreshCurrentMember()
  handleAlipayReturn()
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
