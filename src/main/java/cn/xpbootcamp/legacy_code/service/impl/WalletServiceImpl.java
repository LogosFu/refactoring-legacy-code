package cn.xpbootcamp.legacy_code.service.impl;

import cn.xpbootcamp.legacy_code.domain.Transaction;
import cn.xpbootcamp.legacy_code.entity.User;
import cn.xpbootcamp.legacy_code.repository.UserRepository;
import cn.xpbootcamp.legacy_code.repository.UserRepositoryImpl;
import cn.xpbootcamp.legacy_code.service.WalletService;
import lombok.Data;

import java.util.UUID;
@Data
public class WalletServiceImpl implements WalletService {
    private UserRepository userRepository = new UserRepositoryImpl();

    public String moveMoney(Transaction transaction) {
        User buyer = userRepository.find(transaction.getBuyerId());
        if (buyer.getBalance() >= transaction.getAmount()) {
            User seller = userRepository.find(transaction.getSellerId());
            seller.setBalance(seller.getBalance() + transaction.getAmount());
            buyer.setBalance(buyer.getBalance() - transaction.getAmount());
            return UUID.randomUUID().toString() + transaction.getId();
        } else {
            return null;
        }
    }
}
