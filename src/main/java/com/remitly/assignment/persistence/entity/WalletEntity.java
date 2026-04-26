package com.remitly.assignment.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "wallet")
public class WalletEntity {

    @Id
    @Column(nullable = false)
    private String id;

    protected WalletEntity() {
    }

    public WalletEntity(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
