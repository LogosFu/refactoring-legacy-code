package cn.xpbootcamp.legacy_code

import cn.xpbootcamp.legacy_code.domain.Transaction
import cn.xpbootcamp.legacy_code.enums.TransactionStatus
import cn.xpbootcamp.legacy_code.service.LockService
import cn.xpbootcamp.legacy_code.service.WalletService
import spock.lang.Specification

import javax.transaction.InvalidTransactionException
import java.time.ZoneId
import java.time.ZonedDateTime

class WalletTransactionApplicationTest extends Specification {
    def id = 't_12345667'
    def buyerId = 1L
    def sellerId = 2L
    def productID = 3L
    def orderId = "2345"
    def amount = 100d
    WalletTransactionApplication transaction
    def lockService = Mock(LockService)
    def walletService = Mock(WalletService)
    Transaction transactionEntity

    void setup() {
        transactionEntity = Transaction.builder().sellerId(sellerId).buyerId(buyerId).id(id)
                .amount(amount).orderId(orderId).transactionStatus(TransactionStatus.TO_BE_EXECUTED)
                .createdTimestamp(ZonedDateTime.now(ZoneId.systemDefault()).toEpochSecond() * 1000).build();
        transaction = WalletTransactionApplication.builder()
                .transaction(transactionEntity)
                .lockService(lockService)
                .walletService(walletService)
                .build()
    }

    def "should return WalletTransaction with id and states is TO_BE_EXECUTED when new walletTransaction"() {
        given:
        def preId = ""
        when:
        def transaction = new WalletTransactionApplication(preId, buyerId, sellerId, productID, orderId);
        then:
        transactionEntity.getTransactionStatus() == TransactionStatus.TO_BE_EXECUTED
        transactionEntity.id.startsWith("t_")
    }

    def "should throw InvalidTransactionException when execute given WalletTransaction buyerId is null"() {
        given:
        transactionEntity.buyerId = null
        when:
        transaction.execute();
        then:
        thrown(InvalidTransactionException)
    }

    def "should throw InvalidTransactionException when execute given WalletTransaction sell Id is null"() {
        given:
        transactionEntity.buyerId = null
        when:
        transaction.execute()
        then:
        thrown(InvalidTransactionException)
    }

    def "should throw InvalidTransactionException when execute given transaction amount low than 0"() {
        given:
        transactionEntity.amount = -1
        when:
        transaction.execute()
        then:
        thrown(InvalidTransactionException)
    }

    def "should return true when execute given transaction status is EXECUTED"() {
        given:
        transactionEntity.setTransactionStatus(TransactionStatus.EXECUTED)
        when:
        def result = transaction.execute()
        then:
        result
    }

    def "should return false when execute given transaction id is lock"() {
        given:
        lockService.lock(id) >> false
        walletService.moveMoney(transactionEntity) >> 'abc'
        when:
        def result = transaction.execute()
        then:
        !result
    }

    def "should return false and status to EXPIRED when execute given transaction more than 20 days"() {
        given:
        transactionEntity.createdTimestamp = ZonedDateTime.now(ZoneId.systemDefault()).minusDays(20).minusMinutes(1).toEpochSecond() * 1000
        walletService.moveMoney(transactionEntity) >> 'abc'
        lockService.lock(id) >> true
        when:
        def result = transaction.execute()
        then:
        !result
        transactionEntity.getTransactionStatus() == TransactionStatus.EXPIRED
        1*lockService.unlock(id)
    }

    def "should return false and status to FAILED when execute given walletService return null"() {
        given:
        walletService.moveMoney(transactionEntity) >> null
        lockService.lock(id) >> true
        when:
        def result = transaction.execute()
        then:
        !result
        transactionEntity.getTransactionStatus()== TransactionStatus.FAILED
    }

    def "should return true and status to EXECUTED when execute given walletService return abc"() {
        given:
        walletService.moveMoney(transactionEntity) >> 'abc'
        lockService.lock(id) >> true
        when:
        def result = transaction.execute()
        then:
        result
        transactionEntity.getTransactionStatus() == TransactionStatus.EXECUTED
    }

    def "should return true and status to EXECUTED when execute given walletService return abc and time is in 20days"() {
        given:
        walletService.moveMoney(transactionEntity) >> 'abc'
        transactionEntity.createdTimestamp = ZonedDateTime.now(ZoneId.systemDefault()).minusDays(20).plusMinutes(1).toEpochSecond() * 1000
        lockService.lock(id) >> true
        when:
        def result = transaction.execute()
        then:
        result
        transactionEntity.getTransactionStatus() == TransactionStatus.EXECUTED
    }
}
