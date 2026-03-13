package com.placsp.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class PlacspMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlacspMonitorApplication.class, args);
    }
}
