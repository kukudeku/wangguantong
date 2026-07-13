package com.chinasofti.wangguantong.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 从 application.yml 和环境变量读取支付配置。
 * 私钥配置项保存的是服务器文件路径，不是私钥正文。
 */
@Component
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {

    private int orderExpireMinutes = 15;
    private Wechat wechat = new Wechat();
    private Alipay alipay = new Alipay();

    public int getOrderExpireMinutes() {
        return orderExpireMinutes;
    }

    public void setOrderExpireMinutes(int orderExpireMinutes) {
        this.orderExpireMinutes = orderExpireMinutes;
    }

    public Wechat getWechat() {
        return wechat;
    }

    public void setWechat(Wechat wechat) {
        this.wechat = wechat;
    }

    public Alipay getAlipay() {
        return alipay;
    }

    public void setAlipay(Alipay alipay) {
        this.alipay = alipay;
    }

    public static class Wechat {
        private boolean enabled;
        private String merchantId;
        private String appId;
        private String merchantSerialNumber;
        private String privateKeyPath;
        private String apiV3Key;
        private String notifyUrl;
        private String refundNotifyUrl;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getMerchantId() { return merchantId; }
        public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
        public String getAppId() { return appId; }
        public void setAppId(String appId) { this.appId = appId; }
        public String getMerchantSerialNumber() { return merchantSerialNumber; }
        public void setMerchantSerialNumber(String merchantSerialNumber) { this.merchantSerialNumber = merchantSerialNumber; }
        public String getPrivateKeyPath() { return privateKeyPath; }
        public void setPrivateKeyPath(String privateKeyPath) { this.privateKeyPath = privateKeyPath; }
        public String getApiV3Key() { return apiV3Key; }
        public void setApiV3Key(String apiV3Key) { this.apiV3Key = apiV3Key; }
        public String getNotifyUrl() { return notifyUrl; }
        public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
        public String getRefundNotifyUrl() { return refundNotifyUrl; }
        public void setRefundNotifyUrl(String refundNotifyUrl) { this.refundNotifyUrl = refundNotifyUrl; }
    }

    public static class Alipay {
        private boolean enabled;
        private String appId;
        private String sellerId;
        private String privateKeyPath;
        private String publicKeyPath;
        private String notifyUrl;
        private String returnUrl;
        private String gatewayUrl = "https://openapi.alipay.com/gateway.do";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getAppId() { return appId; }
        public void setAppId(String appId) { this.appId = appId; }
        public String getSellerId() { return sellerId; }
        public void setSellerId(String sellerId) { this.sellerId = sellerId; }
        public String getPrivateKeyPath() { return privateKeyPath; }
        public void setPrivateKeyPath(String privateKeyPath) { this.privateKeyPath = privateKeyPath; }
        public String getPublicKeyPath() { return publicKeyPath; }
        public void setPublicKeyPath(String publicKeyPath) { this.publicKeyPath = publicKeyPath; }
        public String getNotifyUrl() { return notifyUrl; }
        public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
        public String getReturnUrl() { return returnUrl; }
        public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
        public String getGatewayUrl() { return gatewayUrl; }
        public void setGatewayUrl(String gatewayUrl) { this.gatewayUrl = gatewayUrl; }
    }
}
