package com.remitly.assignment.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.remitly.assignment.service.MarketService;

@RestController
@RequestMapping("/stocks")
public class StockController {

    private final MarketService marketService;

    public StockController(MarketService marketService) {
        this.marketService = marketService;
    }

    @PostMapping
    ResponseEntity<Void> setStocks(@RequestBody StocksState request) {
        Map<String, Double> bankStocks = new LinkedHashMap<>();
        if (request != null && request.stocks() != null) {
            for (StockItem item : request.stocks()) {
                bankStocks.put(item.name(), item.quantity());
            }
        }
        marketService.setBankStocks(bankStocks);

        return ResponseEntity.ok().build();
    }

    @GetMapping
    StocksState getStocks() {
        List<StockItem> stocks = marketService.getBankStocks().entrySet().stream()
                .map(entry -> new StockItem(entry.getKey(), entry.getValue()))
                .toList();

        return new StocksState(stocks);
    }

    record StocksState(List<StockItem> stocks) {
    }

    record StockItem(String name, double quantity) {
    }
}