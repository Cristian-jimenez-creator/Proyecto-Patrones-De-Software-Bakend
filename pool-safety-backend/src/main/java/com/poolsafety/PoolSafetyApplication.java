package com.poolsafety;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PoolSafetyApplication {

    public static void main(String[] args) {
        // Toda la aplicacion trabaja en UTC
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(PoolSafetyApplication.class, args);
    }
}
