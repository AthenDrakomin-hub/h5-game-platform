package com.h5;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.h5.mapper")
public class H5Application {
    public static void main(String[] args) {
        SpringApplication.run(H5Application.class, args);
        System.out.println("""
                ====================================================
                  H5 Game Backend 启动成功!
                  API 前缀: /api
                  端口: 8888
                  文档: /api/wap/home/config
                ====================================================
                """);
    }
}
