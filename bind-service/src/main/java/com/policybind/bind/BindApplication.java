package com.policybind.bind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BindApplication {

    public static void main(String[] args) {
        SpringApplication.run(BindApplication.class, args);
    }
}
