package cn.xpbootcamp.legacy_code.service;

import cn.xpbootcamp.legacy_code.domain.TransactionEntity;
import cn.xpbootcamp.legacy_code.entity.User;
import cn.xpbootcamp.legacy_code.repository.UserRepository;
import cn.xpbootcamp.legacy_code.repository.UserRepositoryImpl;
import lombok.Data;

import java.util.UUID;
@Data
public class WalletServiceImpl implements WalletService {
    private UserRepository userRepository = new UserRepositoryImpl();

    public String moveMoney(TransactionEntity transactionEntity) {
        User buyer = userRepository.find(transactionEntity.getBuyerId());
        if (buyer.getBalance() >= transactionEntity.getAmount()) {
            User seller = userRepository.find(transactionEntity.getSellerId());
            seller.setBalance(seller.getBalance() + transactionEntity.getAmount());
            buyer.setBalance(buyer.getBalance() - transactionEntity.getAmount());
            return UUID.randomUUID().toString() + transactionEntity.getId();
        } else {
            return null;
        }
    }
}
