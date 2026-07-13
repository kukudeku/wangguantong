# 本地和宝塔双环境运行说明

本项目现在支持两种运行方式：

| 场景 | 前端启动方式 | 接口转发 | 说明 |
| --- | --- | --- | --- |
| 本地开发 | `npm run dev` | Vite `/api` → `localhost:8087` | 适合自己电脑写代码、课堂演示 |
| 宝塔部署 | `npm run build` | Nginx `/api` → `127.0.0.1:8087` | 适合服务器 Nginx 反向代理 |

访问规则：

```text
默认用户系统：/
用户系统页面：/user
用户登录页面：/user/login
管理员后台：/admin
管理员登录页面：/admin/login
```

前端请求地址在这里配置：

```text
wangguantong-web/src/utils/request.js
```

代码逻辑是：

```js
baseURL: import.meta.env.VITE_API_BASE_URL || '/api'
```

意思是：

- 如果配置了 `VITE_API_BASE_URL`，就用配置的地址。
- 如果没有配置，就默认用 `/api`，本地由 Vite 转发到 `8087`，宝塔由 Nginx 转发到 `8087`。

## 一、本地运行方式

本地运行时，不需要创建 `.env.production`。

### 1. 启动 MySQL

本地 MySQL 中执行：

```sql
source /Users/zhaoyuhan/wangguantong/database/wangguantong.sql;
```

或者把 SQL 内容复制到 Navicat、DataGrip、phpMyAdmin 中执行。

### 2. 修改后端数据库密码

打开：

```text
/Users/zhaoyuhan/wangguantong/wangguantong-server/src/main/resources/application.yml
```

本地可以这样配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/wangguantong?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的本地MySQL密码
```

### 3. 启动后端

```bash
cd /Users/zhaoyuhan/wangguantong/wangguantong-server
mvn spring-boot:run
```

后端地址：

```text
http://localhost:8087
```

### 4. 启动前端

```bash
cd /Users/zhaoyuhan/wangguantong/wangguantong-web
npm install
npm run dev
```

前端地址：

```text
http://localhost:5173
```

本地访问：

```text
用户系统：http://localhost:5173/
管理员后台：http://localhost:5173/admin
```

本地开发时，前端会请求：

```text
http://localhost:8087
```

## 二、宝塔部署方式

宝塔部署时，前端不能继续请求 `localhost:8087`。

原因是：用户浏览器里的 `localhost` 指的是用户自己的电脑，不是你的服务器。

所以宝塔部署要使用：

```env
VITE_API_BASE_URL=/api
```

然后用 Nginx 把 `/api` 转发到服务器本机的 `8087`。

### 1. 创建生产环境配置

在前端目录创建：

```text
wangguantong-web/.env.production
```

内容：

```env
VITE_API_BASE_URL=/api
```

项目里已经提供了示例文件：

```text
wangguantong-web/.env.production.example
```

可以参考它。

### 2. 打包前端

```bash
cd wangguantong-web
npm install
npm run build
```

生成：

```text
wangguantong-web/dist
```

上传 `dist` 里的文件到宝塔网站目录，例如：

```text
/www/wwwroot/wangguantong/web
```

### 3. 宝塔 Nginx 配置

在宝塔网站配置文件中加入：

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

这样浏览器访问：

```text
http://你的域名/api/admin/login
```

实际会转发到：

```text
http://127.0.0.1:8087/admin/login
```

### 4. 宝塔启动后端

后端仍然监听：

```text
8087
```

在宝塔 Java 项目管理器中添加：

```text
项目类型：Spring Boot
运行端口：8087
JDK版本：Java 17
jar文件：wangguantong-server-1.0.0.jar
```

## 三、不要混用的地方

### 本地开发使用 Vite 代理

项目已经在 `vite.config.js` 中配置 `/api` 代理到 `http://localhost:8087`。

所以本地建议：

```env
VITE_API_BASE_URL=/api
```

也可以不创建 `.env.development`，直接使用代码默认值 `/api`。

### 宝塔部署不要写死 `localhost:8087`

宝塔部署如果前端打包成：

```env
VITE_API_BASE_URL=/api
```

用户浏览器会请求用户自己电脑的 `localhost:8087`，肯定失败。

所以宝塔必须使用：

```env
VITE_API_BASE_URL=/api
```

## 四、推荐文件配置

本地开发：

```text
不需要 .env 文件
```

或者创建：

```text
wangguantong-web/.env.development
```

内容：

```env
VITE_API_BASE_URL=http://localhost:8087
```

宝塔部署：

```text
wangguantong-web/.env.production
```

内容：

```env
VITE_API_BASE_URL=/api
```

## 五、验证方式

本地验证：

```bash
cd wangguantong-server
mvn spring-boot:run
```

```bash
cd wangguantong-web
npm run dev
```

访问：

```text
http://localhost:5173
```

宝塔验证：

```bash
curl http://127.0.0.1:8087/dashboard/statistics
```

浏览器访问：

```text
http://你的域名
```

登录：

```text
用户端：110101199001011234 / 123456
管理员后台：admin / 123456
```

## 六、一句话总结

本地：

```text
用户端 localhost:5173/ -> Vite /api -> 后端 localhost:8087
后台 localhost:5173/admin -> Vite /api -> 后端 localhost:8087
```

宝塔：

```text
浏览器访问域名 -> Nginx /api -> 后端 127.0.0.1:8087
```

## 七、真实支付说明

余额支付无需配置。微信支付和支付宝支付需要商户平台资料、公网 HTTPS 回调地址及服务器密钥文件，完整步骤见：

```text
docs/payment-setup.md
```

已有数据库还要执行：

```text
database/update_20260714_food_payment.sql
database/update_20260714_promotion.sql
```

未配置商户资料时，后端仍可正常启动，余额支付和其他系统功能不受影响。

## 八、推广计划数据库升级

新安装项目直接执行 `database/wangguantong.sql`，其中已经包含推广规则、邀请码和邀请记录表。

本地或宝塔已有数据库需要先执行：

```text
database/update_20260714_promotion.sql
```

再重启 Spring Boot 后端。升级脚本可以重复执行，会保留原有会员数据，并自动为已有会员生成唯一邀请码。默认规则为邀请人奖励 `10 元`、新用户奖励 `5 元`，管理员可在“营销活动 > 推广计划”中修改或停用。
