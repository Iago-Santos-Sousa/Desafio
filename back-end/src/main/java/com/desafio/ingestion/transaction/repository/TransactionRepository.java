package com.desafio.ingestion.transaction.repository;

import com.desafio.ingestion.transaction.entity.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository
    extends JpaRepository<TransactionRecord, Long>, TransactionRepositoryCustom {}
