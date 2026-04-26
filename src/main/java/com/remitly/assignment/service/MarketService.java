package com.remitly.assignment.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.remitly.assignment.persistence.entity.AuditLogEntity;
import com.remitly.assignment.persistence.entity.BankStockEntity;
import com.remitly.assignment.persistence.entity.WalletEntity;
import com.remitly.assignment.persistence.entity.WalletStockEntity;
import com.remitly.assignment.persistence.entity.WalletStockId;
import com.remitly.assignment.persistence.repository.AuditLogRepository;
import com.remitly.assignment.persistence.repository.BankStockRepository;
import com.remitly.assignment.persistence.repository.WalletRepository;
import com.remitly.assignment.persistence.repository.WalletStockRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketService {

    private static final int MAX_AUDIT_LOG_SIZE = 10_000;

    private final BankStockRepository bankStockRepository;
    private final WalletRepository walletRepository;
    private final WalletStockRepository walletStockRepository;
    private final AuditLogRepository auditLogRepository;

    public MarketService(
            BankStockRepository bankStockRepository,
            WalletRepository walletRepository,
            WalletStockRepository walletStockRepository,
            AuditLogRepository auditLogRepository) {
        this.bankStockRepository = bankStockRepository;
        this.walletRepository = walletRepository;
        this.walletStockRepository = walletStockRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public enum OperationStatus {
        SUCCESS,
        UNKNOWN_STOCK,
        NO_BANK_STOCK,
        NO_WALLET_STOCK
    }

    @Transactional
    public void setBankStocks(Map<String, Double> newBankState) {
        bankStockRepository.deleteAllInBatch();
        if (newBankState == null) {
            return;
        }

        for (Map.Entry<String, Double> entry : newBankState.entrySet()) {
            bankStockRepository.save(new BankStockEntity(entry.getKey(), entry.getValue()));
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Double> getBankStocks() {
        Map<String, Double> response = new LinkedHashMap<>();
        for (BankStockEntity stock : bankStockRepository.findAllByOrderByNameAsc()) {
            response.put(stock.getName(), stock.getQuantity());
        }
        return response;
    }

    @Transactional
    public OperationStatus buy(String walletId, String stockName) {
        Optional<BankStockEntity> bankStockCandidate = bankStockRepository.findByNameForUpdate(stockName);
        if (bankStockCandidate.isEmpty()) {
            return OperationStatus.UNKNOWN_STOCK;
        }

        BankStockEntity bankStock = bankStockCandidate.get();
        if (bankStock.getQuantity() < 1.0) {
            return OperationStatus.NO_BANK_STOCK;
        }

        WalletEntity wallet = walletRepository.findById(walletId)
                .orElseGet(() -> walletRepository.save(new WalletEntity(walletId)));

        Optional<WalletStockEntity> walletStockCandidate = walletStockRepository.findByWalletAndStockForUpdate(walletId, stockName);
        if (walletStockCandidate.isPresent()) {
            WalletStockEntity walletStock = walletStockCandidate.get();
            walletStock.setQuantity(walletStock.getQuantity() + 1.0);
        } else {
            walletStockRepository.save(new WalletStockEntity(new WalletStockId(walletId, stockName), wallet, 1.0));
        }

        bankStock.setQuantity(bankStock.getQuantity() - 1.0);
        return OperationStatus.SUCCESS;
    }

    @Transactional
    public OperationStatus sell(String walletId, String stockName) {
        Optional<BankStockEntity> bankStockCandidate = bankStockRepository.findByNameForUpdate(stockName);
        if (bankStockCandidate.isEmpty()) {
            return OperationStatus.UNKNOWN_STOCK;
        }

        Optional<WalletStockEntity> walletStockCandidate = walletStockRepository.findByWalletAndStockForUpdate(walletId, stockName);
        if (walletStockCandidate.isEmpty() || walletStockCandidate.get().getQuantity() < 1.0) {
            return OperationStatus.NO_WALLET_STOCK;
        }

        WalletStockEntity walletStock = walletStockCandidate.get();
        if (walletStock.getQuantity() == 1.0) {
            walletStockRepository.delete(walletStock);
        } else {
            walletStock.setQuantity(walletStock.getQuantity() - 1.0);
        }

        BankStockEntity bankStock = bankStockCandidate.get();
        bankStock.setQuantity(bankStock.getQuantity() + 1.0);
        return OperationStatus.SUCCESS;
    }

    @Transactional(readOnly = true)
    public Map<String, Double> getWalletStocks(String walletId) {
        Map<String, Double> response = new LinkedHashMap<>();
        for (WalletStockEntity stock : walletStockRepository.findAllByWalletIdOrderByStockName(walletId)) {
            response.put(stock.getId().getStockName(), stock.getQuantity());
        }
        return response;
    }

    @Transactional(readOnly = true)
    public double getWalletStockQuantity(String walletId, String stockName) {
        return walletStockRepository.findById(new WalletStockId(walletId, stockName))
                .map(WalletStockEntity::getQuantity)
                .orElse(0.0);
    }

    @Transactional
    public void appendAuditLog(String type, String walletId, String stockName) {
        if (auditLogRepository.count() >= MAX_AUDIT_LOG_SIZE) {
            auditLogRepository.findFirstByOrderByIdAsc()
                    .ifPresent(oldestEntry -> auditLogRepository.deleteById(oldestEntry.getId()));
        }

        auditLogRepository.save(new AuditLogEntity(type, walletId, stockName));
    }

    @Transactional(readOnly = true)
    public List<AuditLogEntry> getAuditLog() {
        return auditLogRepository.findAllByOrderByIdAsc().stream()
                .map(logEntry -> new AuditLogEntry(logEntry.getType(), logEntry.getWalletId(), logEntry.getStockName()))
                .toList();
    }

    public record AuditLogEntry(String type, String walletId, String stockName) {
    }
}