package com.remitly.assignment.persistence.repository;

import java.util.List;
import java.util.Optional;

import com.remitly.assignment.persistence.entity.WalletStockEntity;
import com.remitly.assignment.persistence.entity.WalletStockId;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletStockRepository extends JpaRepository<WalletStockEntity, WalletStockId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select walletStock
            from WalletStockEntity walletStock
            where walletStock.id.walletId = :walletId
              and walletStock.id.stockName = :stockName
            """)
    Optional<WalletStockEntity> findByWalletAndStockForUpdate(
            @Param("walletId") String walletId,
            @Param("stockName") String stockName);

    @Query("""
            select walletStock
            from WalletStockEntity walletStock
            where walletStock.id.walletId = :walletId
            order by walletStock.id.stockName
            """)
    List<WalletStockEntity> findAllByWalletIdOrderByStockName(@Param("walletId") String walletId);
}
