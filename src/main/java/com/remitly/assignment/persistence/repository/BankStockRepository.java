package com.remitly.assignment.persistence.repository;

import java.util.List;
import java.util.Optional;

import com.remitly.assignment.persistence.entity.BankStockEntity;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BankStockRepository extends JpaRepository<BankStockEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select stock from BankStockEntity stock where stock.name = :name")
    Optional<BankStockEntity> findByNameForUpdate(@Param("name") String name);

    List<BankStockEntity> findAllByOrderByNameAsc();
}
