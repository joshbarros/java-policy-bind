package com.policybind.sanction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SanctionApplication {

    public static void main(String[] args) {
        SpringApplication.run(SanctionApplication.class, args);
    }
}
