package com.kossentini.portfolio.repository;

import com.kossentini.portfolio.domain.Transaction;
import com.kossentini.portfolio.domain.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
            select t from Transaction t
            where t.portfolio.id = :portfolioId
            and (:type is null or t.type = :type)
            and (:from is null or t.transactionDate >= :from)
            and (:to is null or t.transactionDate <= :to)
            """)
    Page<Transaction> search(Long portfolioId, TransactionType type, Instant from, Instant to, Pageable pageable);
}
