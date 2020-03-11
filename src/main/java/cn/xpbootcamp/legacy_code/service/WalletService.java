package cn.xpbootcamp.legacy_code.service;

import cn.xpbootcamp.legacy_code.domain.TransactionEntity;

public interface WalletService {
    String moveMoney(TransactionEntity transactionEntity);
}
