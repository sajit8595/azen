package com.meridiantrust.sentinel.repository;

import com.meridiantrust.sentinel.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByAccountIdOrderByTxnTimestampDesc(String accountId);

    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId " +
           "AND t.txnTimestamp BETWEEN :from AND :to ORDER BY t.txnTimestamp")
    List<Transaction> findInWindow(@Param("accountId") String accountId,
                                   @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to);

    @Query("SELECT t FROM Transaction t WHERE t.accountId IN " +
           "(SELECT a.id FROM Account a WHERE a.customerId = :customerId) " +
           "ORDER BY t.txnTimestamp DESC")
    List<Transaction> findByCustomerId(@Param("customerId") String customerId);
}
