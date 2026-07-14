# 网管通——电竞网吧运营管理系统

网管通是一个前后端分离的电竞网吧运营管理系统，包含顾客自助端、管理员后台、Spring Boot 后端和 MySQL 数据库。当前版本覆盖机位上机、预约、点餐支付、会员营销、服务工单和经营管理等完整演示链路。

## 当前系统能力

用户端入口为 `/`（自动跳转 `/user`），支持：

- 身份证号注册、登录、修改密码；注册时可填写邀请码；
- 机位大厅、当前上机、我的预约和上机记录；
- 自助点餐、购物车批量下单、优惠券抵扣，以及余额/微信 Native/支付宝电脑网站支付；
- 每日签到、我的优惠券、邀请好友和邀请奖励记录；
- 呼叫网管、故障报修和服务记录；
- 余额明细、团购券核销和点餐订单查询。

管理员入口为 `/admin`，支持：

- 首页经营统计和机位状态概览；
- 会员、余额充值、电脑、预约、上机、点餐订单和商品维护；
- 服务工单受理、完成和取消，故障报修与电脑“维修中”状态联动；
- 团购券、优惠券模板、签到奖励规则和推广计划管理；
- 点餐支付状态查询，以及已支付订单的余额退款或第三方原路退款。

当前后端包含 14 个 Controller、63 个 HTTP 接口和 18 张数据库表。微信与支付宝默认关闭；未配置商户资料时，余额支付和其他功能仍可正常使用。

## 技术栈与端口

| 模块 | 技术 | 默认地址 |
|---|---|---|
| 前端 | Vue 3、Vue Router、Axios、Vite 5、Arco Design Vue | `http://localhost:5173` |
| 后端 | Java 17、Spring Boot 3.3.5、MyBatis-Plus 3.5.7 | `http://localhost:8087` |
| 数据库 | MySQL 8，数据库名 `wangguantong` | `localhost:3306` |

前端默认请求 `/api`。本地由 Vite 去掉 `/api` 前缀后代理到 `8087`；生产环境由 Nginx按同样规则反向代理。

## 快速启动

### 1. 初始化数据库

全新安装直接执行完整脚本：

```bash
mysql -uroot -p < database/wangguantong.sql
```

完整脚本会重建 `wangguantong` 数据库。已有数据库需要保留数据时不要执行完整脚本，请先备份并按 [数据库升级说明](docs/local-and-baota.md#已有数据库升级) 选择增量脚本。

### 2. 配置并启动后端

修改 `wangguantong-server/src/main/resources/application.yml` 中的数据库账号和密码，然后执行：

```bash
cd wangguantong-server
mvn spring-boot:run
```

### 3. 启动前端

```bash
cd wangguantong-web
npm install
npm run dev
```

访问：

- 用户端：`http://localhost:5173/`
- 管理员后台：`http://localhost:5173/admin`

测试账号：

| 入口 | 账号 | 密码 |
|---|---|---|
| 用户端 | 身份证号 `110101199001011234` | `123456` |
| 管理员后台 | `admin` | `123456` |

测试团购券 `WGT-TEST-50` 可在用户端“账户服务 → 团购验券”中核销一次。

## 构建与测试

```bash
cd wangguantong-server
mvn test

cd ../wangguantong-web
npm run build
```

## 数据库文件

- `database/wangguantong.sql`：当前完整结构、基础数据和演示数据；
- `database/update_20260708_*.sql`：早期会员、点餐和预约功能升级；
- `database/update_20260713_coupon_sign_in.sql`：优惠券与连续签到；
- `database/update_20260713_voucher_change_repair.sql`：团购券、上机分段字段和报修；
- `database/update_20260714_food_payment.sql`：微信/支付宝支付与退款记录；
- `database/update_20260714_promotion.sql`：邀请码和推广奖励；
- `database/update_20260714_service_center.sql`：统一服务工单字段。

## 文档

完整文档索引见 [docs/README.md](docs/README.md)。常用入口：

- [需求规格说明书](docs/需求规格说明书.md)
- [本地与宝塔运行说明](docs/local-and-baota.md)
- [宝塔部署文档](docs/baota-deploy.md)
- [真实支付配置](docs/payment-setup.md)
- [项目答辩稿](docs/项目答辩稿.md)
- `docs/API接口设计说明书.docx`
- `docs/数据库设计书.xlsx`
- `docs/项目报告.docx`
- `docs/原型设计书.html`

`wangguantong-commented/` 是较早期核心功能的注释学习副本，不是当前主系统的完整镜像；运行、部署和功能验收以根目录代码与 `docs/` 为准。

## 当前安全边界

项目仍定位为课程演示系统：密码尚未散列，页面登录状态保存在 `localStorage`，后端没有统一鉴权和细粒度授权。公开生产部署前应至少补充 Spring Security、密码散列、服务端会话或 JWT、接口权限、并发控制、审计日志、HTTPS 和密钥轮换。
