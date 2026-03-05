package com.gydl.djbh;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 网络安全等级保护测评项目管理系统 启动类
 */
@SpringBootApplication
@MapperScan("com.gydl.djbh.mapper")
@EnableCaching
@EnableAsync
@EnableScheduling
public class DjbhApplication {
    public static void main(String[] args) {
        SpringApplication.run(DjbhApplication.class, args);
    }
}
