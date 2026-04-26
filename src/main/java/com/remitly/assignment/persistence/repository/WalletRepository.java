package com.remitly.assignment.persistence.repository;

import com.remitly.assignment.persistence.entity.WalletEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<WalletEntity, String> {
}
