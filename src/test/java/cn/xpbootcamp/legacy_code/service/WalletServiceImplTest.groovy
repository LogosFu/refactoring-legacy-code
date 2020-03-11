package cn.xpbootcamp.legacy_code.service

import cn.xpbootcamp.legacy_code.WalletTransactionApplication
import cn.xpbootcamp.legacy_code.entity.User
import cn.xpbootcamp.legacy_code.enums.TransactionStatus
import cn.xpbootcamp.legacy_code.repository.UserRepository
import cn.xpbootcamp.legacy_code.service.impl.WalletServiceImpl
import org.apache.commons.lang3.StringUtils
import spock.lang.Specification

import java.time.ZoneId
import java.time.ZonedDateTime

class WalletServiceImplTest extends Specification {
    def id = 't_12345667'
    def buyerId = 1L
    def sellerId = 2L
    def productID = 3L
    def orderId = "2345"
    def amount = 100d
    WalletTransactionApplication transaction
    WalletService walletService

    void setup() {
        walletService = new WalletServiceImpl()
        walletService.setUserRepository(userRepository)
        transaction = WalletTransactionApplication.builder().sellerId(sellerId).buyerId(buyerId).id(id)
                .amount(amount).orderId(orderId).status(TransactionStatus.TO_BE_EXECUTED)
                .createdTimestamp(ZonedDateTime.now(ZoneId.systemDefault()).toEpochSecond() * 1000).build()
    }
    def userRepository = Mock(UserRepository);


    def "should return null when move money given user balance less than amount"() {
        given:
        userRepository.find(buyerId) >> User.builder().id(1L).balance(50d).build()
        when:
        def result = walletService.moveMoney(transaction)
        then:
        result == null
    }

    def "should move money from buyer to seller when moveMoney given buyer balance more than amount"() {
        given:
        def buyer = User.builder().id(buyerId).balance(100d).build()
        def seller = User.builder().id(sellerId).balance(0d).build()
        userRepository.find(buyerId)>> buyer
        userRepository.find(sellerId)>> seller
        when:
        def result = walletService.moveMoney(transaction)
        then:
        StringUtils.isNoneBlank(result)
        buyer.balance == 0
        seller.balance == 100
    }
}
