package com.aicc.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AiccBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiccBackendApplication.class, args);
    }
}
