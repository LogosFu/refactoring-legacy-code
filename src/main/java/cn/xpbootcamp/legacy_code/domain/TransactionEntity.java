package cn.xpbootcamp.legacy_code.domain;

import cn.xpbootcamp.legacy_code.enums.STATUS;
import cn.xpbootcamp.legacy_code.utils.IdGenerator;
import javax.transaction.InvalidTransactionException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class TransactionEntity {
    String id;
    Long buyerId;
    Long sellerId;
    Long productId;
    String orderId;
    Long createdTimestamp;
    Double amount;
    STATUS status;

    public TransactionEntity(String preAssignedId, Long buyerId, Long sellerId, Long productId, String orderId) {
        this.id = getPreId(preAssignedId);
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.productId = productId;
        this.orderId = orderId;
        this.initTransaction();
    }

    protected String getPreId(String preAssignedId) {
        if (preAssignedId != null && !preAssignedId.isEmpty()) {
            return renderId(preAssignedId);
        } else {
            return renderId(IdGenerator.generateTransactionId());
        }
    }

    private String renderId(String preAssignedId) {
        if (!preAssignedId.startsWith("t_")) {
            preAssignedId = "t_" + preAssignedId;
        }
        return preAssignedId;
    }

    private void initTransaction() {
        status = STATUS.TO_BE_EXECUTED;
        createdTimestamp = System.currentTimeMillis();
    }

    public boolean isExpired() {
        boolean isExpired = System.currentTimeMillis() - getCreatedTimestamp() > 1728000000;
        if (isExpired) {
            status = STATUS.EXPIRED;
        }
        return isExpired;
    }

    public void checkParamValid()
        throws InvalidTransactionException {
        if (getBuyerId() == null || (getSellerId() == null
            || getAmount() < 0.0)) {
            throw new InvalidTransactionException("This is an invalid transaction");
        }
    }

    public boolean isSuccess() {
        return getStatus() == STATUS.EXECUTED;
    }

    public void checkMoveMoneyResult(String walletTransactionId) {
        if (walletTransactionId != null) {
            setStatus(STATUS.EXECUTED);
        } else {
            setStatus(STATUS.FAILED);
        }
    }

    public Boolean doubleCheck() {
        isExpired();
        return isSuccess(); // double check
    }
}