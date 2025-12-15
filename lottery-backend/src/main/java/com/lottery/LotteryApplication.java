package com.lottery;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 年会抽奖系统 - 后端启动类
 */
@SpringBootApplication
@MapperScan("com.lottery.mapper")
public class LotteryApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(LotteryApplication.class, args);
        System.out.println("========================================");
        System.out.println("年会抽奖系统后端服务启动成功！");
        System.out.println("API 文档地址: http://localhost:8080/api/doc.html");
        System.out.println("========================================");
    }
}
