# 网管通宝塔部署文档

本文档适合把“网管通 —— 电竞网吧运营管理系统”部署到一台安装了宝塔面板的 Linux 服务器。

## 一、部署前准备

服务器建议：

- CentOS、Ubuntu、Debian 均可
- 内存建议 2GB 以上
- 已安装宝塔面板

宝塔软件商店安装：

- Nginx
- MySQL 8.0 或 MySQL 5.7
- Java 项目管理器
- Java 17

本项目目录：

```text
wangguantong
├── database
│   ├── wangguantong.sql
│   └── update_20260708_features.sql
├── wangguantong-server
└── wangguantong-web
```

## 二、创建 MySQL 数据库

1. 登录宝塔面板。
2. 打开“数据库”。
3. 点击“添加数据库”。
4. 填写：

```text
数据库名：wangguantong
用户名：wangguantong
密码：自己设置一个密码
访问权限：本地服务器
```

5. 创建完成后，进入 phpMyAdmin 或宝塔数据库管理工具。
6. 执行 SQL 文件：

```text
/Users/zhaoyuhan/wangguantong/database/wangguantong.sql
```

如果是本地打包后上传服务器，可以先在本地打开这个文件，复制全部 SQL 到宝塔数据库工具中执行。

如果服务器上已经有旧版数据库，不想清空旧数据，可以只执行：

```text
database/update_20260708_features.sql
```

新部署建议直接执行完整的 `wangguantong.sql`。

## 三、修改后端数据库配置

打开后端配置文件：

```text
wangguantong-server/src/main/resources/application.yml
```

修改数据库用户名和密码：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/wangguantong?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: wangguantong
    password: 你的数据库密码
```

端口保持：

```yaml
server:
  port: 8080
```

## 四、打包后端 Spring Boot

在本地或服务器进入后端目录：

```bash
cd wangguantong-server
mvn clean package -DskipTests
```

打包成功后会生成：

```text
wangguantong-server/target/wangguantong-server-1.0.0.jar
```

把这个 jar 上传到服务器，例如：

```text
/www/wwwroot/wangguantong/server/wangguantong-server-1.0.0.jar
```

## 五、宝塔启动后端

1. 打开宝塔“Java 项目管理器”。
2. 点击“添加 Java 项目”。
3. 填写：

```text
项目名称：wangguantong-server
项目类型：Spring Boot
项目 jar：/www/wwwroot/wangguantong/server/wangguantong-server-1.0.0.jar
运行端口：8080
JDK版本：Java 17
```

4. 启动项目。
5. 查看日志，确认没有数据库连接错误。

可以在服务器终端测试：

```bash
curl http://127.0.0.1:8080/dashboard/statistics
```

如果返回 JSON，说明后端启动成功。

## 六、打包前端 Vue

前端生产环境建议通过 Nginx 代理 `/api` 到后端 `8080`，这样浏览器只访问同一个域名，不需要暴露后端端口。

注意：本地开发不要创建 `.env.production` 来启动 `npm run dev`，本地默认会请求 `http://localhost:8080`。宝塔部署才需要 `.env.production`。

进入前端目录：

```bash
cd wangguantong-web
npm install
```

创建生产环境配置：

```text
wangguantong-web/.env.production
```

内容写：

```env
VITE_API_BASE_URL=/api
```

项目里也提供了两个示例文件：

```text
wangguantong-web/.env.development.example
wangguantong-web/.env.production.example
```

本地开发可以不用创建 `.env.development`，因为代码默认就是 `http://localhost:8080`。

然后打包：

```bash
npm run build
```

打包结果：

```text
wangguantong-web/dist
```

把 `dist` 目录里的所有文件上传到：

```text
/www/wwwroot/wangguantong/web
```

注意：上传的是 `dist` 目录里面的文件，不是把 `dist` 文件夹本身放进去也可以，但网站根目录要指到实际包含 `index.html` 的目录。

## 七、宝塔创建前端网站

1. 打开宝塔“网站”。
2. 点击“添加站点”。
3. 填写：

```text
域名：你的域名 或 服务器IP
根目录：/www/wwwroot/wangguantong/web
PHP版本：纯静态
```

4. 保存。

如果没有域名，可以先用服务器 IP 访问。

## 八、配置 Nginx 反向代理后端

进入宝塔网站设置：

1. 找到刚创建的网站。
2. 点击“设置”。
3. 点击“配置文件”。
4. 在 `server { ... }` 中加入以下配置：

```nginx
location /api/ {
    proxy_pass http://127.0.0.1:8080/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}

location / {
    try_files $uri $uri/ /index.html;
}
```

保存后重载 Nginx。

这样前端请求 `/api/admin/login` 时，会被 Nginx 转发到：

```text
http://127.0.0.1:8080/admin/login
```

## 九、放行端口

如果使用 Nginx 代理，外网只需要放行：

```text
80
443
```

后端 `8080` 可以不对外开放，只给服务器本机访问。

如果你想直接访问后端接口，需要在服务器安全组和宝塔防火墙里放行：

```text
8080
```

课堂演示建议使用 Nginx 代理方式，不直接暴露 8080。

## 十、访问系统

浏览器访问：

```text
http://你的域名
```

或：

```text
http://服务器IP
```

管理员账号：

```text
用户名：admin
密码：123456
```

## 十一、功能测试

登录：

- 输入 `admin / 123456`
- 登录后进入首页

首页：

- 查看会员总数、电脑总数、空闲电脑、使用中电脑
- 查看电脑座位图
- 不同状态显示不同颜色

会员管理：

- 新增会员
- 修改会员类型和会员级别
- 给散客充值，散客会自动变为普通会员

电脑管理：

- 新增电脑
- 修改电脑状态
- 查看空闲、使用中、预约锁定、维修中状态

预约管理：

- 选择会员和空闲电脑
- 点击预约锁定
- 电脑状态变成预约锁定
- 可以取消预约或预约上机

充值管理：

- 选择会员
- 输入充值金额
- 充值后余额增加

上机管理：

- 选择会员和空闲电脑上机
- 查看实时上机时长
- 查看当前消费和余额提醒
- 点击下机结算

网吧点餐：

- 新增商品
- 会员点餐会扣余额
- 余额不足会提示
- 散客点餐可直接下单
- 取消会员订单会退回余额

## 十二、常见问题

### 1. 登录提示服务器连接失败

检查前端 `.env.production` 是否配置：

```env
VITE_API_BASE_URL=/api
```

检查 Nginx 是否配置了：

```nginx
location /api/ {
    proxy_pass http://127.0.0.1:8080/;
}
```

检查后端是否已启动。

### 2. 后端启动失败，提示数据库连接失败

检查：

- MySQL 是否启动
- 数据库名是否是 `wangguantong`
- `application.yml` 的 username 和 password 是否正确
- SQL 是否已经执行

### 3. 页面刷新后 404

Vue 使用前端路由，需要 Nginx 配置：

```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

### 4. 前端还是请求 localhost:8080

说明前端打包时没有读取生产环境变量。

检查：

```text
wangguantong-web/.env.production
```

必须有：

```env
VITE_API_BASE_URL=/api
```

然后重新执行：

```bash
npm run build
```

再把新的 `dist` 上传到宝塔网站目录。

### 5. 宝塔 Java 项目能启动但接口访问 404

确认 jar 是最新打包的：

```bash
mvn clean package -DskipTests
```

确认上传的是：

```text
target/wangguantong-server-1.0.0.jar
```

不是 `.jar.original` 文件。

## 十三、推荐目录结构

服务器上建议这样放：

```text
/www/wwwroot/wangguantong
├── server
│   └── wangguantong-server-1.0.0.jar
└── web
    ├── index.html
    └── assets
```

数据库脚本可以临时上传，用完后删除或备份。
