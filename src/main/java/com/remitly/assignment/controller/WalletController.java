package com.remitly.assignment.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.remitly.assignment.service.MarketService;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final MarketService marketService;

    public WalletController(MarketService marketService) {
        this.marketService = marketService;
    }

    @PostMapping("/{walletId}/stocks/{stockName}")
    ResponseEntity<Void> executeOperation(
            @PathVariable String walletId,
            @PathVariable String stockName,
            @RequestBody OperationRequest request) {

        if (request == null || request.type() == null) {
            return ResponseEntity.badRequest().build();
        }

        String operationType = request.type().trim().toLowerCase();
        MarketService.OperationStatus status;

        if ("buy".equals(operationType)) {
            status = marketService.buy(walletId, stockName);
        } else if ("sell".equals(operationType)) {
            status = marketService.sell(walletId, stockName);
        } else {
            return ResponseEntity.badRequest().build();
        }

        if (status == MarketService.OperationStatus.SUCCESS) {
            marketService.appendAuditLog(operationType, walletId, stockName);
            return ResponseEntity.ok().build();
        }

        if (status == MarketService.OperationStatus.UNKNOWN_STOCK) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/{walletId}")
    WalletState getWallet(@PathVariable String walletId) {
        List<StockItem> stocks = marketService.getWalletStocks(walletId).entrySet().stream()
                .map(entry -> new StockItem(entry.getKey(), entry.getValue()))
                .toList();

        return new WalletState(walletId, stocks);
    }

    @GetMapping("/{walletId}/stocks/{stockName}")
    double getWalletStockQuantity(@PathVariable String walletId, @PathVariable String stockName) {
        return marketService.getWalletStockQuantity(walletId, stockName);
    }

    record OperationRequest(String type) {
    }

    record WalletState(String id, List<StockItem> stocks) {
    }

    record StockItem(String name, double quantity) {
    }
}