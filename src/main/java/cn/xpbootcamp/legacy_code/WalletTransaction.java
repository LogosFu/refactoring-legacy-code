package cn.xpbootcamp.legacy_code;

import cn.xpbootcamp.legacy_code.domain.TransactionEntity;
import cn.xpbootcamp.legacy_code.enums.CheckResult;
import cn.xpbootcamp.legacy_code.service.LockService;
import cn.xpbootcamp.legacy_code.service.LockServiceImpl;
import cn.xpbootcamp.legacy_code.service.WalletService;
import cn.xpbootcamp.legacy_code.service.WalletServiceImpl;
import javax.transaction.InvalidTransactionException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class WalletTransaction {
    final TransactionEntity transactionEntity;
    String walletTransactionId;
    private LockService lockService;
    private WalletService walletService;
    private boolean isLocked;


    public WalletTransaction(String preAssignedId, Long buyerId, Long sellerId, Long productId, String orderId) {
        this.transactionEntity = new TransactionEntity(preAssignedId, buyerId, sellerId, productId, orderId);
        this.lockService = new LockServiceImpl();
        this.walletService = new WalletServiceImpl();
    }

    public boolean execute() throws InvalidTransactionException {
        transactionEntity.checkParamValid();
        if (transactionEntity.isSuccess()) {
            return true;
        }
        isLocked = false;
        try {
            return toMoveMoney();
        } finally {
            if (isLocked) {
                lockService.unlock(transactionEntity.getId());
            }
        }
    }

    protected boolean toMoveMoney() {
        isLocked = lockService.lock(transactionEntity.getId());
        if (preCheck() != CheckResult.NO_RESULT) {
            return preCheck().isSuccess();
        }
        walletTransactionId = walletService.moveMoney(transactionEntity);
        transactionEntity.checkMoveMoneyResult(walletTransactionId);
        return transactionEntity.isSuccess();
    }

    protected CheckResult preCheck() {
        if (!isLocked || transactionEntity.isExpired()) {
            return CheckResult.FAILED;
        }

        if (transactionEntity.isSuccess()) {
            return CheckResult.SUCCESS;
        }
        return CheckResult.NO_RESULT;
    }

}