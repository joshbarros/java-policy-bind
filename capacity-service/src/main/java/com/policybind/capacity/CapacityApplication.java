package com.policybind.capacity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CapacityApplication {

    public static void main(String[] args) {
        SpringApplication.run(CapacityApplication.class, args);
    }
}
