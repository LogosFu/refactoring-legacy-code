package cn.xpbootcamp.legacy_code

import cn.xpbootcamp.legacy_code.enums.STATUS
import cn.xpbootcamp.legacy_code.service.LockService
import cn.xpbootcamp.legacy_code.service.WalletService
import spock.lang.Specification

import javax.transaction.InvalidTransactionException
import java.time.ZoneId
import java.time.ZonedDateTime

class WalletTransactionTest extends Specification {
    def id = 't_12345667'
    def buyerId = 1L
    def sellerId = 2L
    def productID = 3L
    def orderId = "2345"
    def amount = 100d
    WalletTransaction transaction
    def lockService = Mock(LockService)
    def walletService = Mock(WalletService)

    void setup() {
        transaction = WalletTransaction.builder().sellerId(sellerId).buyerId(buyerId).id(id)
                .amount(amount).orderId(orderId).status(STATUS.TO_BE_EXECUTED)
                .lockService(lockService)
                 .walletService(walletService)
                .createdTimestamp(ZonedDateTime.now(ZoneId.systemDefault()).toEpochSecond() * 1000).build()
    }

    def "should return WalletTransaction with id and states is TO_BE_EXECUTED when new walletTransaction"() {
        given:
        def preId = ""
        when:
        def transaction = new WalletTransaction(preId, buyerId, sellerId, productID, orderId);
        then:
        transaction.status == STATUS.TO_BE_EXECUTED
        transaction.id.startsWith("t_")
    }

    def "should throw InvalidTransactionException when execute given WalletTransaction buyerId is null"() {
        given:
        transaction.buyerId = null
        when:
        transaction.execute();
        then:
        thrown(InvalidTransactionException)
    }

    def "should throw InvalidTransactionException when execute given WalletTransaction sell Id is null"() {
        given:
        transaction.buyerId = null
        when:
        transaction.execute()
        then:
        thrown(InvalidTransactionException)
    }

    def "should throw InvalidTransactionException when execute given transaction amount low than 0"() {
        given:
        transaction.amount = -1
        when:
        transaction.execute()
        then:
        thrown(InvalidTransactionException)
    }

    def "should return true when execute given transaction status is EXECUTED"() {
        given:
        transaction.status = STATUS.EXECUTED
        when:
        def result = transaction.execute()
        then:
        result
    }

    def "should return false when execute given transaction id is lock"() {
        given:
        lockService.lock(id) >> false
        walletService.moveMoney(id, buyerId, sellerId, amount) >> 'abc'
        when:
        def result = transaction.execute()
        then:
        !result
    }

    def "should return false and status to EXPIRED when execute given transaction more than 20 days"() {
        given:
        transaction.createdTimestamp = ZonedDateTime.now(ZoneId.systemDefault()).minusDays(20).minusMinutes(1).toEpochSecond() * 1000
        walletService.moveMoney(id, buyerId, sellerId, amount) >> 'abc'
        lockService.lock(id) >> true
        when:
        def result = transaction.execute()
        then:
        !result
        transaction.status == STATUS.EXPIRED
        1*lockService.unlock(id)
    }

    def "should return false and status to FAILED when execute given walletService return null"() {
        given:
        walletService.moveMoney(id, buyerId, sellerId, amount) >> null
        lockService.lock(id) >> true
        when:
        def result = transaction.execute()
        then:
        !result
        transaction.status == STATUS.FAILED
    }

    def "should return true and status to EXECUTED when execute given walletService return abc"() {
        given:
        walletService.moveMoney(id, buyerId, sellerId, amount) >> 'abc'
        lockService.lock(id) >> true
        when:
        def result = transaction.execute()
        then:
        result
        transaction.status == STATUS.EXECUTED
    }
}
