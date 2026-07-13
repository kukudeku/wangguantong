package com.chinasofti.wangguantong.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 前后端分离项目的跨域配置。
 *
 * <p>本地开发时前端通常运行在 5173 端口，后端运行在 8080 端口。
 * 浏览器会把“端口不同”视为不同来源，因此必须在后端明确允许跨域请求。</p>
 */
@Configuration
public class CorsConfig {

    /**
     * 创建跨域过滤器，并让它对所有 URL 生效。
     *
     * <p>当前配置为了课堂演示允许任意来源、请求头和请求方法。
     * 正式生产系统通常应把 allowedOrigin 限制为自己的前端域名。</p>
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许来自任意域名的请求，例如 localhost:5173。
        config.addAllowedOriginPattern("*");
        // 允许前端携带 Content-Type 等请求头。
        config.addAllowedHeader("*");
        // 允许 GET、POST、PUT、DELETE 等请求方式。
        config.addAllowedMethod("*");
        // 允许浏览器携带身份信息。虽然当前项目未使用 Cookie，保留后不影响演示。
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // /** 表示后端的所有接口路径都采用上面的跨域规则。
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
