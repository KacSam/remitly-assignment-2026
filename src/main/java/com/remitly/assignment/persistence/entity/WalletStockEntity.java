package com.remitly.assignment.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "wallet_stock")
public class WalletStockEntity {

    @EmbeddedId
    private WalletStockId id;

    @MapsId("walletId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private WalletEntity wallet;

    @Column(nullable = false)
    private double quantity;

    protected WalletStockEntity() {
    }

    public WalletStockEntity(WalletStockId id, WalletEntity wallet, double quantity) {
        this.id = id;
        this.wallet = wallet;
        this.quantity = quantity;
    }

    public WalletStockId getId() {
        return id;
    }

    public WalletEntity getWallet() {
        return wallet;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}
