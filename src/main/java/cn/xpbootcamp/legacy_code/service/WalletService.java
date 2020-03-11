package cn.xpbootcamp.legacy_code.service;

import cn.xpbootcamp.legacy_code.domain.Transaction;

public interface WalletService {
    String moveMoney(Transaction transaction);
}
