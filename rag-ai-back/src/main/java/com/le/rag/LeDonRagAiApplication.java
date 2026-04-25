package com.le.rag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LeDonRagAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeDonRagAiApplication.class, args);
    }

}
