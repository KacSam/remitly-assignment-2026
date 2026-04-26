package com.remitly.assignment.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class WalletStockId implements Serializable {

    @Column(name = "wallet_id", nullable = false)
    private String walletId;

    @Column(name = "stock_name", nullable = false)
    private String stockName;

    protected WalletStockId() {
    }

    public WalletStockId(String walletId, String stockName) {
        this.walletId = walletId;
        this.stockName = stockName;
    }

    public String getWalletId() {
        return walletId;
    }

    public String getStockName() {
        return stockName;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WalletStockId that)) {
            return false;
        }
        return Objects.equals(walletId, that.walletId) && Objects.equals(stockName, that.stockName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(walletId, stockName);
    }
}
