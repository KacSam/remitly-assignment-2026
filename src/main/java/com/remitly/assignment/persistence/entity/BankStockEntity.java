package com.remitly.assignment.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bank_stock")
public class BankStockEntity {

    @Id
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double quantity;

    protected BankStockEntity() {
    }

    public BankStockEntity(String name, double quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}
