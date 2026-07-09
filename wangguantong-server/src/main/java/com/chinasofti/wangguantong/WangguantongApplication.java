package com.chinasofti.wangguantong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WangguantongApplication {

    public static void main(String[] args) {
        SpringApplication.run(WangguantongApplication.class, args);
    }
}
