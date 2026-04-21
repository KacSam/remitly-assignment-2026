package com.remitly.assignment;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stocks")
class StockController {

    private volatile StocksState state = new StocksState(List.of());

    @PostMapping
    ResponseEntity<Void> setStocks(@RequestBody StocksState request) {
        if (request == null || request.stocks() == null) {
            this.state = new StocksState(List.of());
        } else {
            this.state = request;
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping
    StocksState getStocks() {
        return state;
    }

    record StocksState(List<StockItem> stocks) {
    }

    record StockItem(String name, double quantity) {
    }
}