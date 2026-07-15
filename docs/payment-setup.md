# 微信支付与支付宝真实支付配置

> 文档基线：2026-07-15 当前主系统。支付查询与回调接口位于 `/payment`，余额充值发起接口为 `/recharge/payment`，后端端口为 `8087`。

项目点餐支持余额支付、微信支付、支付宝支付，余额充值支持微信支付和支付宝支付。余额支付无需额外配置；微信和支付宝默认关闭，只有配置真实商户资料后才会启用。

## 一、当前支付流程

- 微信支付：使用微信支付 API v3 的 Native 支付，网页显示二维码，顾客用微信扫码付款。
- 支付宝支付：使用支付宝电脑网站支付，网页打开支付宝收银台。
- 点餐第三方订单先保存为“待支付”，平台异步通知验签成功后才改为“已下单”；充值支付验签成功后才增加余额。
- 15 分钟未支付的订单会自动关闭，并归还订单占用的优惠券。
- 后台取消已支付订单时，按该商品实付金额原路退款。微信退款可能先显示“退款中”，回调成功后显示“已取消”。
- 商户私钥只从服务器密钥文件读取，不会保存在数据库、前端或 Git 中。

## 二、数据库升级

旧数据库保留数据升级时，应先确保已经执行优惠券/购物车批次升级，再执行支付升级：

```bash
mysql -u你的数据库用户 -p wangguantong < database/update_20260713_coupon_sign_in.sql
mysql -u你的数据库用户 -p wangguantong < database/update_20260714_food_payment.sql
```

脚本会创建：

- `payment_record`：一次购物车结算对应一条支付主记录。
- `payment_record.business_type`：区分“点餐”和“余额充值”，两种业务共用同一套验签、查单和超时关闭能力。
- `payment_refund`：后台逐条取消商品时对应一条退款记录。

新部署直接执行 `database/wangguantong.sql`，其中已经包含这两张表。

`food_order` 的业务状态包括 `待支付`、`已下单`、`退款中`、`已完成`、`已取消`；`payment_record` 的支付状态包括 `待支付`、`已支付`、`已关闭`、`部分退款`、`退款中`、`已退款`。页面展示时不要把订单状态和支付状态混为一列含义。

## 三、开通微信支付

先在微信支付商户平台完成以下配置：

1. 开通 Native 支付产品。
2. 准备商户号和已绑定的 AppID。
3. 设置 API v3 密钥。
4. 下载商户 API 证书，保存商户私钥 `apiclient_key.pem`，并记录证书序列号。

微信回调必须是公网可访问的 HTTPS 地址：

```text
支付回调：https://你的域名/api/payment/notify/wechat
退款回调：https://你的域名/api/payment/notify/wechat/refund
```

本机 `localhost` 不能接收微信回调。正式联调应部署到带 HTTPS 域名的服务器，或使用可靠的 HTTPS 内网穿透工具。

## 四、开通支付宝支付

先在支付宝开放平台完成以下配置：

1. 创建网页/移动应用并开通“电脑网站支付”。
2. 使用 RSA2 密钥模式，准备应用私钥和支付宝公钥。
3. 应用上线并完成签约后使用正式网关。

支付宝回调与跳转地址：

```text
异步回调：https://你的域名/api/payment/notify/alipay
支付返回：https://你的域名/user
```

`ALIPAY_PUBLIC_KEY_PATH` 必须指向“支付宝公钥”，不能误填应用公钥。沙箱测试时，应用编号、密钥、账号和网关必须全部使用支付宝沙箱环境的资料。

## 五、服务器密钥目录

密钥文件应放在网站静态目录以外，例如：

```text
/www/server/wangguantong-secrets/
├── wechat_apiclient_key.pem
├── alipay_app_private_key.pem
└── alipay_public_key.pem
```

Linux 上限制读取权限：

```bash
chmod 700 /www/server/wangguantong-secrets
chmod 600 /www/server/wangguantong-secrets/*.pem
```

不要把密钥放进 `wangguantong-web/dist`，不要上传到 Git，也不要把私钥粘贴到聊天或截图中。

## 六、后端环境变量

在宝塔 Java 项目的“环境变量”中配置以下内容。示例中的值必须替换为商户平台提供的真实资料：

```env
PAYMENT_ORDER_EXPIRE_MINUTES=15

WECHAT_PAY_ENABLED=true
WECHAT_PAY_MCH_ID=你的微信商户号
WECHAT_PAY_APP_ID=已绑定的AppID
WECHAT_PAY_MERCHANT_SERIAL_NUMBER=商户API证书序列号
WECHAT_PAY_PRIVATE_KEY_PATH=/www/server/wangguantong-secrets/wechat_apiclient_key.pem
WECHAT_PAY_API_V3_KEY=你的32位APIv3密钥
WECHAT_PAY_NOTIFY_URL=https://你的域名/api/payment/notify/wechat
WECHAT_PAY_REFUND_NOTIFY_URL=https://你的域名/api/payment/notify/wechat/refund

ALIPAY_ENABLED=true
ALIPAY_APP_ID=你的支付宝应用ID
ALIPAY_SELLER_ID=你的支付宝卖家ID
ALIPAY_PRIVATE_KEY_PATH=/www/server/wangguantong-secrets/alipay_app_private_key.pem
ALIPAY_PUBLIC_KEY_PATH=/www/server/wangguantong-secrets/alipay_public_key.pem
ALIPAY_NOTIFY_URL=https://你的域名/api/payment/notify/alipay
ALIPAY_RETURN_URL=https://你的域名/user
ALIPAY_GATEWAY_URL=https://openapi.alipay.com/gateway.do
```

如果暂时只开通一个平台，另一个平台的 `ENABLED` 保持 `false`。未启用的平台被选择时，系统会提示“尚未配置”，不会生成错误订单。

修改环境变量后必须重启 Spring Boot 后端。

## 七、宝塔与 Nginx

生产前端继续使用：

```env
VITE_API_BASE_URL=/api
```

Nginx 配置保持：

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

支付回调地址带 `/api`，Nginx 转发给后端时会去掉 `/api/` 前缀，因此后端收到的路径正好是 `/payment/notify/...`。

## 八、上线验证

1. 使用金额很小的商品和充值金额分别发起微信、支付宝付款。
2. 确认付款前订单状态为“待支付”。
3. 付款后等待页面自动显示“支付成功”，点餐订单变为“已下单”，充值金额只增加一次。
4. 在后台取消订单，确认支付平台账单出现原路退款。
5. 检查 `payment_record` 的平台交易号和支付状态。
6. 检查 `payment_refund` 的退款单号和退款状态。
7. 创建一笔订单但不付款，等待配置的过期时间，确认订单关闭且已占用优惠券恢复为可使用。

回调验收必须同时确认：请求到达 Nginx、Nginx 去掉 `/api` 前缀、后端验签成功、数据库状态只更新一次。浏览器跳回 `/user` 只表示页面返回，不代表支付宝异步通知已经成功处理。

真实支付最终能否成功还取决于商户产品是否开通、应用是否上线、证书是否有效、回调域名是否为 HTTPS，以及服务器能否访问微信和支付宝网关。
