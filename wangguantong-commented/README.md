# 网管通中文注释学习副本（历史快照）

> 本目录是较早期核心功能的中文注释副本，不是当前主系统的完整镜像。它只覆盖会员、电脑、充值、上机、预约和基础点餐，不包含主系统后来加入的真实支付、优惠券签到、团购券、推广邀请和统一服务工单。

学习代码可先阅读 [代码阅读指南.md](代码阅读指南.md)。运行、部署、验收和答辩必须以仓库根目录代码及 [主系统文档](../docs/README.md) 为准。

## 快照范围

- 8 张基础表：`admin`、`member`、`computer`、`recharge_record`、`online_record`、`reservation_record`、`food_item`、`food_order`；
- 用户身份证号登录、注册、修改密码；
- 会员、电脑、充值、上机、预约、商品和基础订单；
- 上机首小时扣费、每 60 秒补扣差额、余额不足自动下机；
- 本地前端 `5173`、后端 `8087`，请求通过 `/api` 代理。

## 运行学习副本

学习副本使用自己的旧版数据库脚本。它会重建 `wangguantong` 数据库，请勿对保存有主系统数据的数据库执行：

```bash
mysql -uroot -p < wangguantong-commented/database/wangguantong.sql
```

修改 `wangguantong-commented/wangguantong-server/src/main/resources/application.yml` 的数据库密码，然后分别启动：

```bash
cd wangguantong-commented/wangguantong-server
mvn spring-boot:run
```

```bash
cd wangguantong-commented/wangguantong-web
npm install
npm run dev
```

访问：

- 用户端：`http://localhost:5173/`；
- 后台：`http://localhost:5173/admin`；
- 用户测试账号：`110101199001011234 / 123456`；
- 管理员：`admin / 123456`。

## 与当前主系统的关系

主系统位置为仓库根目录：

```text
wangguantong-server/
wangguantong-web/
database/
docs/
```

当前主系统基线是 18 张表、64 个接口，功能与部署说明见 [根 README](../README.md)。不要把本快照中的旧部署文档复制到生产环境。
