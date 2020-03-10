package cn.xpbootcamp.legacy_code.service;

import cn.xpbootcamp.legacy_code.WalletTransaction;
import cn.xpbootcamp.legacy_code.entity.User;
import cn.xpbootcamp.legacy_code.repository.UserRepository;
import cn.xpbootcamp.legacy_code.repository.UserRepositoryImpl;
import lombok.Data;

import java.util.UUID;
@Data
public class WalletServiceImpl implements WalletService {
    private UserRepository userRepository = new UserRepositoryImpl();

    public String moveMoney(WalletTransaction transaction) {
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
