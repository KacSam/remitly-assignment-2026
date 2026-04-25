package com.remitly.assignment.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chaos")
public class ChaosController {

    private final ConfigurableApplicationContext applicationContext;
    private final long shutdownDelayMs;

    public ChaosController(
            ConfigurableApplicationContext applicationContext,
            @Value("${chaos.shutdown-delay-ms:300}") long shutdownDelayMs) {
        this.applicationContext = applicationContext;
        this.shutdownDelayMs = shutdownDelayMs;
    }

    @PostMapping
    ResponseEntity<Void> triggerChaos() {
        Thread shutdownThread = new Thread(this::shutdownAfterDelay, "chaos-shutdown-thread");
        shutdownThread.setDaemon(true);
        shutdownThread.start();

        return ResponseEntity.ok().build();
    }

    private void shutdownAfterDelay() {
        try {
            Thread.sleep(shutdownDelayMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }

        applicationContext.close();
    }
}
