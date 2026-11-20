package com.poolsafety.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    // Reloj UTC inyectable para poder controlar el tiempo en las pruebas
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
