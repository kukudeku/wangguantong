# 网管通宝塔部署文档

本文适用于当前网管通主系统：Vue 静态站点由 Nginx 提供，Spring Boot JAR 监听 `8087`，MySQL 数据库名为 `wangguantong`。

## 1. 部署准备

宝塔软件商店安装：

- Nginx；
- MySQL 8（推荐）；
- Java 项目管理器；
- Java 17。

服务器建议至少 2 GB 内存。真实支付环境还需要已备案或可用的 HTTPS 域名、商户资料和独立密钥目录。

## 2. 创建数据库

在宝塔创建数据库：

```text
数据库名：wangguantong
用户名：wangguantong
访问权限：本地服务器
```

全新安装导入 `database/wangguantong.sql`。该脚本会重建数据库，不能用于保留生产数据的升级。

已有数据库先备份，再按 [增量升级表](local-and-baota.md#已有数据库升级) 执行缺失脚本。当前完整系统需要包含 18 张表；最后一项服务中心升级 `update_20260714_service_center.sql` 只执行一次。

## 3. 配置并打包后端

修改 `wangguantong-server/src/main/resources/application.yml` 中的数据库配置，端口保持 `8087`：

```yaml
server:
  port: 8087

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/wangguantong?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: wangguantong
    password: 你的数据库密码
```

打包：

```bash
cd wangguantong-server
mvn clean package -DskipTests
```

上传：

```text
target/wangguantong-server-1.0.0.jar
→ /www/wwwroot/wangguantong/server/wangguantong-server-1.0.0.jar
```

不要上传 `.jar.original`。

## 4. 创建宝塔 Java 项目

```text
项目名称：wangguantong-server
项目类型：Spring Boot
项目 JAR：/www/wwwroot/wangguantong/server/wangguantong-server-1.0.0.jar
运行端口：8087
JDK：Java 17
```

启动后验证：

```bash
curl http://127.0.0.1:8087/dashboard/statistics
```

返回 `code: 200` 的 JSON 说明后端、数据库和定时任务已正常启动。

## 5. 构建前端

构建前创建 `wangguantong-web/.env.production`：

```env
VITE_API_BASE_URL=/api
```

执行：

```bash
cd wangguantong-web
npm install
npm run build
```

将 `dist` 中的内容上传到：

```text
/www/wwwroot/wangguantong/web
```

站点根目录必须直接包含 `index.html` 和 `assets/`。

## 6. 创建静态站点与 Nginx 代理

宝塔网站根目录设置为 `/www/wwwroot/wangguantong/web`，PHP 选择纯静态。站点配置加入：

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

保存并重载 Nginx。`proxy_pass` 末尾的 `/` 会把 `/api/admin/login` 正确转为后端 `/admin/login`。

外网只需开放 `80` 和 `443`；`8087` 建议只允许本机访问。

## 7. HTTPS 与真实支付

余额支付无需额外配置。微信和支付宝默认关闭。启用真实支付时：

1. 为站点配置有效 HTTPS 证书；
2. 将密钥放在网站静态目录以外；
3. 在 Java 项目环境变量中配置商户号、应用编号、密钥路径和回调 URL；
4. 按 [payment-setup.md](payment-setup.md) 完成微信 Native、支付宝电脑网站支付和退款验证；
5. 重启 Java 项目。

## 8. 验收清单

### 基础入口

- 用户端：`https://你的域名/`；
- 管理员后台：`https://你的域名/admin`；
- 测试用户：`110101199001011234 / 123456`；
- 管理员：`admin / 123456`。

### 功能抽查

- 用户登录、机位上机、预约和上机记录；
- 点餐、优惠券、余额支付；已配置商户时再测试微信和支付宝；
- 每日签到、邀请好友、团购券 `WGT-TEST-50`；
- 呼叫网管、故障报修、后台服务工单处理；
- 后台会员、电脑、上机、预约、点餐、营销活动页面；
- 取消余额订单回余额；取消第三方已支付订单进入退款流程。

## 9. 常见问题

### 页面刷新 404

确认存在：

```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

### 接口 404

确认前端构建变量为 `/api`，且 `proxy_pass http://127.0.0.1:8087/;` 末尾有 `/`。

### 接口 502 或 503

检查 Java 项目是否启动、日志是否存在数据库错误，以及：

```bash
curl http://127.0.0.1:8087/dashboard/statistics
```

### 新功能报表或字段不存在

说明 JAR 与数据库结构版本不一致。备份数据库后补执行缺失的增量脚本，再重启 Java 项目。

### 微信/支付宝提示未配置

这是默认行为。检查对应 `*_ENABLED=true`、全部商户参数、密钥文件权限和 HTTPS 回调地址；只配置其中一个平台时，另一个保持关闭。

## 10. 推荐目录

```text
/www/wwwroot/wangguantong
├── server
│   └── wangguantong-server-1.0.0.jar
└── web
    ├── index.html
    └── assets

/www/server/wangguantong-secrets
├── wechat_apiclient_key.pem
├── alipay_app_private_key.pem
└── alipay_public_key.pem
```

密钥目录不要放进网站根目录、前端 `dist` 或 Git 仓库。
