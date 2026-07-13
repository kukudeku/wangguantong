# 网管通 —— 电竞网吧运营管理系统（中文注释学习副本）

> 这是从原项目复制出的学习版本，仅增加中文注释和阅读文档，不修改业务逻辑。
> 初次阅读请先打开同目录的 [`代码阅读指南.md`](./代码阅读指南.md)。

这是一个课堂作业版全栈项目，代码尽量简单清晰，方便运行、演示和二次修改。

## 第一部分：数据库 SQL

SQL 文件路径：

- `database/wangguantong.sql`

包含内容：

- 创建 `wangguantong` 数据库
- 创建 `admin`、`member`、`computer`、`recharge_record`、`online_record` 五张表
- 新版还包含 `food_item`、`food_order`、`reservation_record` 三张表
- 插入管理员账号 `admin / 123456`
- 插入会员、电脑、充值记录、上机记录测试数据

如果你已经执行过旧版 SQL，不想清空旧数据，可以执行扩展脚本：

- `database/update_20260708_features.sql`
- `database/update_20260708_user_login.sql`

如果你不需要保留旧数据，直接重新执行 `database/wangguantong.sql` 最简单。

## 第二部分：后端 Spring Boot 完整代码

后端路径：

- `wangguantong-server`

主要文件：

- `pom.xml`
- `src/main/resources/application.yml`
- `src/main/java/com/chinasofti/wangguantong/WangguantongApplication.java`
- `common/Result.java`
- `config/CorsConfig.java`
- `controller/AdminController.java`
- `controller/DashboardController.java`
- `controller/MemberController.java`
- `controller/ComputerController.java`
- `controller/RechargeController.java`
- `controller/OnlineController.java`
- `controller/FoodController.java`
- `controller/ReservationController.java`
- `entity/Admin.java`
- `entity/Member.java`
- `entity/Computer.java`
- `entity/RechargeRecord.java`
- `entity/OnlineRecord.java`
- `entity/FoodItem.java`
- `entity/FoodOrder.java`
- `entity/ReservationRecord.java`
- `mapper/AdminMapper.java`
- `mapper/MemberMapper.java`
- `mapper/ComputerMapper.java`
- `mapper/RechargeRecordMapper.java`
- `mapper/OnlineRecordMapper.java`
- `mapper/FoodItemMapper.java`
- `mapper/FoodOrderMapper.java`
- `mapper/ReservationRecordMapper.java`
- `service/AdminService.java`
- `service/MemberService.java`
- `service/ComputerService.java`
- `service/RechargeRecordService.java`
- `service/OnlineRecordService.java`
- `service/FoodItemService.java`
- `service/FoodOrderService.java`
- `service/ReservationRecordService.java`
- `service/impl/AdminServiceImpl.java`
- `service/impl/MemberServiceImpl.java`
- `service/impl/ComputerServiceImpl.java`
- `service/impl/RechargeRecordServiceImpl.java`
- `service/impl/OnlineRecordServiceImpl.java`
- `service/impl/FoodItemServiceImpl.java`
- `service/impl/FoodOrderServiceImpl.java`
- `service/impl/ReservationRecordServiceImpl.java`

## 第三部分：前端 Arco Pro 完整代码

前端路径：

- `wangguantong-web`

主要文件：

- `package.json`
- `vite.config.js`
- `index.html`
- `src/main.js`
- `src/App.vue`
- `src/style.css`
- `src/utils/request.js`
- `src/router/index.js`
- `src/api/admin.js`
- `src/api/dashboard.js`
- `src/api/member.js`
- `src/api/computer.js`
- `src/api/recharge.js`
- `src/api/online.js`
- `src/api/food.js`
- `src/api/reservation.js`
- `src/views/login/index.vue`
- `src/views/user-login/index.vue`
- `src/views/user/index.vue`
- `src/views/dashboard/index.vue`
- `src/views/member/index.vue`
- `src/views/computer/index.vue`
- `src/views/recharge/index.vue`
- `src/views/online/index.vue`
- `src/views/food/index.vue`
- `src/views/reservation/index.vue`

## 第四部分：运行步骤

1. 创建 MySQL 数据库和表

   打开 MySQL 客户端，执行：

   ```sql
   source /Users/zhaoyuhan/wangguantong/database/wangguantong.sql;
   ```

   也可以复制 `database/wangguantong.sql` 的全部内容到 Navicat、DataGrip 或 MySQL 命令行中执行。

2. 修改后端数据库密码

   打开：

   ```text
   /Users/zhaoyuhan/wangguantong/wangguantong-server/src/main/resources/application.yml
   ```

   把：

   ```yaml
   password: 你的数据库密码
   ```

   改成你本机 MySQL 的 root 密码。

3. 启动 Spring Boot 后端

   ```bash
   cd /Users/zhaoyuhan/wangguantong/wangguantong-server
   mvn spring-boot:run
   ```

   后端地址：

   ```text
   http://localhost:8087
   ```

4. 安装前端依赖

   ```bash
   cd /Users/zhaoyuhan/wangguantong/wangguantong-web
   npm install
   ```

5. 启动前端

   ```bash
   npm run dev
   ```

   浏览器访问：

   ```text
   http://localhost:5173
   ```

6. 测试账号

   用户端测试账号：

   ```text
   身份证号：110101199001011234
   密码：123456
   ```

   管理员后台测试账号：

   ```text
   用户名：admin
   密码：123456
   ```

## 功能测试方法

- 用户端：访问 `/` 或 `/user`，未登录会进入用户登录页；输入 `110101199001011234 / 123456` 后进入自助服务台。
- 管理员后台：访问 `/admin`，未登录会进入后台登录页；输入 `admin / 123456` 后进入后台首页。
- 首页：查看会员总数、电脑总数、空闲电脑、使用中电脑、今日充值金额、今日上机次数。
- 会员管理：新增会员、编辑会员、删除会员、按姓名或手机号查询、查看余额、点击充值。
- 电脑管理：新增电脑、编辑电脑、删除电脑、按区域或状态查询，状态会用标签显示。
- 充值管理：选择会员，输入充值金额，确认后会员余额增加，并生成充值记录。
- 上机管理：选择正常会员和空闲电脑，点击上机；电脑状态会变为使用中，并生成进行中记录。
- 下机结算：对进行中的记录点击下机结算；系统按不足 1 小时算 1 小时扣费，并按会员级别打折，余额不足会提示“会员余额不足，请先充值”。
- 上机记录查询：按会员姓名、电脑编号、状态筛选记录，查看实时上机时长、当前消费、会员余额和余额提醒。
- 预约管理：选择会员和空闲电脑，点击预约锁定；电脑状态会变为“预约锁定”。可以取消预约，也可以从预约记录直接开始上机。
- 网吧点餐：维护商品，上架/下架商品；会员点餐会扣会员余额，余额不足会提示；散客点餐可以直接输入散客姓名；取消会员订单会退回余额。
- 会员级别：散客无折扣，普通会员不打折，黄金会员 9 折，钻石会员 8 折。
- 散客转会员：散客充值后，后端会自动把用户类型改为“会员”，会员级别改为“普通会员”。
- 用户端自助：用户登录后才能点餐、预约和自助上机。
