package com.remitly.assignment.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.remitly.assignment.service.MarketService;

@RestController
@RequestMapping("/log")
public class AuditLogController {

    private final MarketService marketService;

    public AuditLogController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping
    AuditLogState getAuditLog() {
        List<AuditLogItem> items = marketService.getAuditLog().stream()
                .map(entry -> new AuditLogItem(entry.type(), entry.walletId(), entry.stockName()))
                .toList();

        return new AuditLogState(items);
    }

    record AuditLogState(List<AuditLogItem> log) {
    }

    record AuditLogItem(String type, String wallet_id, String stock_name) {
    }
}
