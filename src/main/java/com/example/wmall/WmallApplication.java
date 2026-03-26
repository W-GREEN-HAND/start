package com.example.wmall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.example.wmall.mapper")
@SpringBootApplication
public class WmallApplication {

    public static void main(String[] args) {
        SpringApplication.run(WmallApplication.class, args);
    }

}
