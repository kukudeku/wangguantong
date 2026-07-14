# 本地与宝塔双环境运行说明

## 环境基线

| 场景 | 前端 | API 转发 | 后端 |
|---|---|---|---|
| 本地开发 | Vite `http://localhost:5173` | `/api/*` → `http://localhost:8087/*` | Spring Boot `8087` |
| 宝塔生产 | Nginx 静态站点 | `/api/*` → `http://127.0.0.1:8087/*` | Java 项目 `8087` |

访问入口：

- 用户端：`/` 或 `/user`；未登录进入 `/user/login`；
- 管理员后台：`/admin`；未登录进入 `/admin/login`。

前端 `src/utils/request.js` 使用：

```js
baseURL: import.meta.env.VITE_API_BASE_URL || '/api'
```

建议本地和生产都使用 `/api`，由对应代理去掉 `/api` 前缀。不要在生产构建中写 `localhost:8087`，因为浏览器的 `localhost` 指访问者自己的电脑。

## 本地运行

### 1. 初始化 MySQL

全新安装：

```bash
mysql -uroot -p < database/wangguantong.sql
```

该脚本会删除并重建 `wangguantong` 数据库，仅适合全新安装或允许清空数据的环境。

### 2. 配置后端

修改 `wangguantong-server/src/main/resources/application.yml`：

```yaml
server:
  port: 8087

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/wangguantong?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的本地MySQL密码
```

真实支付默认关闭，无需填写商户密钥即可启动。

### 3. 启动后端和前端

```bash
cd wangguantong-server
mvn spring-boot:run
```

另开终端：

```bash
cd wangguantong-web
npm install
npm run dev
```

本地可以不创建 `.env.development`。如需显式配置，将 `.env.development.example` 复制为 `.env.development`，保持：

```env
VITE_API_BASE_URL=/api
```

### 4. 本地验证

```bash
curl http://127.0.0.1:8087/dashboard/statistics
```

浏览器访问 `http://localhost:5173`，分别验证用户端和 `/admin`。

## 已有数据库升级

升级前必须备份数据库。不要执行完整 `wangguantong.sql`，也不要在不清楚当前结构时盲目重复执行全部脚本。

| 脚本 | 适用能力 | 可重复性说明 |
|---|---|---|
| `update_20260708_features.sql` | 会员等级、点餐、预约 | 早期基础升级 |
| `update_20260708_user_login.sql` | 用户账号、密码、身份证号 | 带字段存在判断 |
| `update_20260708_member_id_card.sql` | 单独补身份证号字段 | 带字段存在判断 |
| `update_20260708_food_category.sql` | 商品分类 | 带字段存在判断 |
| `update_20260713_coupon_sign_in.sql` | 优惠券、签到、购物车优惠字段 | 可重复建表，字段带判断 |
| `update_20260713_voucher_change_repair.sql` | 团购券、上机分段字段、报修 | 字段带判断 |
| `update_20260714_food_payment.sql` | 第三方支付主记录和退款记录 | 可重复执行 |
| `update_20260714_promotion.sql` | 邀请码、推广规则和奖励记录 | 可重复执行 |
| `update_20260714_service_center.sql` | 报修扩展为统一服务工单 | 只执行一次 |

从早期版本逐步升级时，建议按表中顺序执行；已经具备对应字段或表的环境只执行缺失项。最后执行服务中心脚本并重启后端。

示例：

```bash
mysql -u你的用户 -p wangguantong < database/update_20260713_coupon_sign_in.sql
mysql -u你的用户 -p wangguantong < database/update_20260713_voucher_change_repair.sql
mysql -u你的用户 -p wangguantong < database/update_20260714_food_payment.sql
mysql -u你的用户 -p wangguantong < database/update_20260714_promotion.sql
mysql -u你的用户 -p wangguantong < database/update_20260714_service_center.sql
```

## 宝塔生产运行速查

### 1. 构建

```bash
cd wangguantong-server
mvn clean package -DskipTests

cd ../wangguantong-web
npm install
npm run build
```

产物：

- 后端：`wangguantong-server/target/wangguantong-server-1.0.0.jar`；
- 前端：`wangguantong-web/dist/`。

### 2. 前端生产变量

构建前创建 `wangguantong-web/.env.production`：

```env
VITE_API_BASE_URL=/api
```

### 3. Nginx

```nginx
location /api/ {
    proxy_pass http://127.0.0.1:8087/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}

location / {
    try_files $uri $uri/ /index.html;
}
```

`proxy_pass` 末尾的 `/` 不能遗漏，否则后端可能收到带 `/api` 的错误路径。

### 4. 生产验证

```bash
curl http://127.0.0.1:8087/dashboard/statistics
curl https://你的域名/api/dashboard/statistics
```

第一条验证 Java 服务，第二条验证 HTTPS 与 Nginx 代理。页面刷新返回 404 时检查 `try_files`；接口 502/503 时检查 Java 项目和 `8087` 监听状态。

## 支付说明

余额支付无需配置。微信 Native 与支付宝电脑网站支付的环境变量、密钥和回调地址见 [payment-setup.md](payment-setup.md)。未启用的平台被选择时，后端会返回未配置提示，不会影响其他业务。
