package com.remitly.assignment.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_log")
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(name = "wallet_id", nullable = false)
    private String walletId;

    @Column(name = "stock_name", nullable = false)
    private String stockName;

    protected AuditLogEntity() {
    }

    public AuditLogEntity(String type, String walletId, String stockName) {
        this.type = type;
        this.walletId = walletId;
        this.stockName = stockName;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getWalletId() {
        return walletId;
    }

    public String getStockName() {
        return stockName;
    }
}
