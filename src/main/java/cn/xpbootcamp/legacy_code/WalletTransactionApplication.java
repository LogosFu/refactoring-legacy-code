package cn.xpbootcamp.legacy_code;

import cn.xpbootcamp.legacy_code.domain.Transaction;
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
public class WalletTransactionApplication {
    final Transaction transaction;
    String walletTransactionId;
    private LockService lockService;
    private WalletService walletService;
    private boolean isLocked;


    public WalletTransactionApplication(String preAssignedId, Long buyerId, Long sellerId, Long productId, String orderId) {
        this.transaction = new Transaction(preAssignedId, buyerId, sellerId, productId, orderId);
        this.lockService = new LockServiceImpl();
        this.walletService = new WalletServiceImpl();
    }

    public boolean execute() throws InvalidTransactionException {
        transaction.checkParamValid();
        if (transaction.isSuccess()) {
            return true;
        }
        isLocked = false;
        try {
            return toMoveMoney();
        } finally {
            if (isLocked) {
                lockService.unlock(transaction.getId());
            }
        }
    }

    protected boolean toMoveMoney() {
        isLocked = lockService.lock(transaction.getId());
        if (preCheck() != CheckResult.NO_RESULT) {
            return preCheck().isSuccess();
        }
        walletTransactionId = walletService.moveMoney(transaction);
        transaction.checkMoveMoneyResult(walletTransactionId);
        return transaction.isSuccess();
    }

    protected CheckResult preCheck() {
        if (!isLocked || transaction.isExpired()) {
            return CheckResult.FAILED;
        }

        if (transaction.isSuccess()) {
            return CheckResult.SUCCESS;
        }
        return CheckResult.NO_RESULT;
    }

}