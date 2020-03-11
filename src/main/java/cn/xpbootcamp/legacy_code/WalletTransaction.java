package cn.xpbootcamp.legacy_code;

import cn.xpbootcamp.legacy_code.enums.STATUS;
import cn.xpbootcamp.legacy_code.service.LockService;
import cn.xpbootcamp.legacy_code.service.LockServiceImpl;
import cn.xpbootcamp.legacy_code.service.WalletService;
import cn.xpbootcamp.legacy_code.service.WalletServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.transaction.InvalidTransactionException;

@Data
@Builder
@AllArgsConstructor
public class WalletTransaction {
    final TransactionEntity transactionEntity;
    String walletTransactionId;
    private LockService lockService;
    private WalletService walletService;


    public WalletTransaction(String preAssignedId, Long buyerId, Long sellerId, Long productId, String orderId) {
        this.transactionEntity = new TransactionEntity(preAssignedId, buyerId, sellerId, productId, orderId);
        transactionEntity.initTransaction();
        this.lockService = new LockServiceImpl();
        this.walletService = new WalletServiceImpl();
    }

    public boolean execute() throws InvalidTransactionException {
        if (transactionEntity.buyerId == null || (transactionEntity.sellerId == null || transactionEntity.amount < 0.0)) {
            throw new InvalidTransactionException("This is an invalid transaction");
        }
        if (transactionEntity.status == STATUS.EXECUTED) return true;
        boolean isLocked = false;
        try {
            isLocked = lockService.lock(transactionEntity.id);

            // 锁定未成功，返回false
            if (!isLocked) {
                return false;
            }
            if (transactionEntity.status == STATUS.EXECUTED) return true; // double check
            long executionInvokedTimestamp = System.currentTimeMillis();
            // 交易超过20天
            if (executionInvokedTimestamp - transactionEntity.createdTimestamp > 1728000000) {
                this.transactionEntity.status = STATUS.EXPIRED;
                return false;
            }
            walletTransactionId = walletService.moveMoney(transactionEntity);
            if (walletTransactionId != null) {
                this.transactionEntity.status = STATUS.EXECUTED;
                return true;
            } else {
                this.transactionEntity.status = STATUS.FAILED;
                return false;
            }
        } finally {
            if (isLocked) {
                lockService.unlock(transactionEntity.id);
            }
        }
    }
}