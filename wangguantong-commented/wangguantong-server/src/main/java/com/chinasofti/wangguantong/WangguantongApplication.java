package com.chinasofti.wangguantong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 网管通后端程序的启动入口。
 *
 * <p>{@link SpringBootApplication} 会自动完成三件事：</p>
 * <ol>
 *     <li>把当前类标记为 Spring Boot 应用；</li>
 *     <li>扫描当前包及子包中的 Controller、Service 等组件；</li>
 *     <li>根据 pom.xml 中的依赖自动配置 Spring MVC、数据库等功能。</li>
 * </ol>
 *
 * <p>{@link EnableScheduling} 用于开启定时任务。项目中的实时上机扣费、
 * 余额不足自动下机、预约超时自动取消，都依赖这个注解。</p>
 */
@SpringBootApplication
@EnableScheduling
public class WangguantongApplication {

    /**
     * Java 程序的主入口。运行此方法后，内置 Tomcat 默认监听 application.yml
     * 中配置的 8080 端口。
     */
    public static void main(String[] args) {
        SpringApplication.run(WangguantongApplication.class, args);
    }
}
