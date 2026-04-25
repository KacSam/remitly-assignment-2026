package com.remitly.assignment.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class MarketService {

    private static final int MAX_AUDIT_LOG_SIZE = 10_000;

    public enum OperationStatus {
        SUCCESS,
        UNKNOWN_STOCK,
        NO_BANK_STOCK,
        NO_WALLET_STOCK
    }

    private final Map<String, Double> bankStocks = new LinkedHashMap<>();
    private final Map<String, Map<String, Double>> wallets = new LinkedHashMap<>();
    private final List<AuditLogEntry> auditLog = new ArrayList<>();

    public synchronized void setBankStocks(Map<String, Double> newBankState) {
        bankStocks.clear();
        if (newBankState != null) {
            bankStocks.putAll(newBankState);
        }
    }

    public synchronized Map<String, Double> getBankStocks() {
        return new LinkedHashMap<>(bankStocks);
    }

    public synchronized OperationStatus buy(String walletId, String stockName) {
        if (!bankStocks.containsKey(stockName)) {
            return OperationStatus.UNKNOWN_STOCK;
        }

        double bankQuantity = bankStocks.getOrDefault(stockName, 0.0);
        if (bankQuantity < 1.0) {
            return OperationStatus.NO_BANK_STOCK;
        }

        Map<String, Double> wallet = getOrCreateWallet(walletId);
        double walletQuantity = wallet.getOrDefault(stockName, 0.0);
        wallet.put(stockName, walletQuantity + 1.0);
        bankStocks.put(stockName, bankQuantity - 1.0);

        return OperationStatus.SUCCESS;
    }

    public synchronized OperationStatus sell(String walletId, String stockName) {
        if (!bankStocks.containsKey(stockName)) {
            return OperationStatus.UNKNOWN_STOCK;
        }

        Map<String, Double> wallet = getOrCreateWallet(walletId);
        double walletQuantity = wallet.getOrDefault(stockName, 0.0);
        if (walletQuantity < 1.0) {
            return OperationStatus.NO_WALLET_STOCK;
        }

        if (walletQuantity == 1.0) {
            wallet.remove(stockName);
        } else {
            wallet.put(stockName, walletQuantity - 1.0);
        }

        double bankQuantity = bankStocks.getOrDefault(stockName, 0.0);
        bankStocks.put(stockName, bankQuantity + 1.0);

        return OperationStatus.SUCCESS;
    }

    public synchronized Map<String, Double> getWalletStocks(String walletId) {
        Map<String, Double> wallet = wallets.get(walletId);
        if (wallet == null) {
            return new LinkedHashMap<>();
        }

        return new LinkedHashMap<>(wallet);
    }

    public synchronized double getWalletStockQuantity(String walletId, String stockName) {
        Map<String, Double> wallet = wallets.get(walletId);
        if (wallet == null) {
            return 0.0;
        }

        return wallet.getOrDefault(stockName, 0.0);
    }

    public synchronized void appendAuditLog(String type, String walletId, String stockName) {
        if (auditLog.size() >= MAX_AUDIT_LOG_SIZE) {
            auditLog.removeFirst();
        }

        auditLog.add(new AuditLogEntry(type, walletId, stockName));
    }

    public synchronized List<AuditLogEntry> getAuditLog() {
        return new ArrayList<>(auditLog);
    }

    private Map<String, Double> getOrCreateWallet(String walletId) {
        return wallets.computeIfAbsent(walletId, ignored -> new LinkedHashMap<>());
    }

    public record AuditLogEntry(String type, String walletId, String stockName) {
    }
}