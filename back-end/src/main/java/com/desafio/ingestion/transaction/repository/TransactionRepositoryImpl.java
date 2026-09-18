package com.desafio.ingestion.transaction.repository;

import com.desafio.ingestion.ingestion.IngestionJob;
import com.desafio.ingestion.transaction.dto.TransactionDto;
import com.desafio.ingestion.transaction.entity.TransactionRecord;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class TransactionRepositoryImpl implements TransactionRepositoryCustom {
  private final EntityManager entityManager;

  public TransactionRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<TransactionDto> findPage(int limit, Long cursor, UUID jobId, String category) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<TransactionDto> query = builder.createQuery(TransactionDto.class);
    Root<TransactionRecord> transaction = query.from(TransactionRecord.class);
    Join<TransactionRecord, IngestionJob> job = transaction.join("ingestionJob", JoinType.INNER);
    List<Predicate> predicates = new ArrayList<>();

    if (cursor != null) {
      predicates.add(builder.greaterThan(transaction.<Long>get("id"), cursor));
    }

    if (jobId != null) {
      predicates.add(builder.equal(job.<UUID>get("id"), jobId));
    }

    if (category != null && !category.isBlank()) {
      predicates.add(builder.equal(transaction.<String>get("category"), category.trim()));
    }

    query
        .select(
            builder.construct(
                TransactionDto.class,
                transaction.get("id"),
                job.get("id"),
                transaction.get("occurredAt"),
                transaction.get("category"),
                transaction.get("amount"),
                transaction.get("description")))
        .where(predicates.toArray(Predicate[]::new))
        .orderBy(builder.asc(transaction.get("id")));

    return entityManager.createQuery(query).setMaxResults(limit + 1).getResultList();
  }

  @Override
  public List<String> findCategories(int limit, UUID jobId, String search, String cursor) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<String> query = builder.createQuery(String.class);
    Root<TransactionRecord> transaction = query.from(TransactionRecord.class);
    Join<TransactionRecord, IngestionJob> job = transaction.join("ingestionJob", JoinType.INNER);
    List<Predicate> predicates = new ArrayList<>();

    predicates.add(builder.equal(job.<UUID>get("id"), jobId));

    if (search != null && !search.isBlank()) {
      String prefix = search.trim().toLowerCase(Locale.ROOT) + "%";
      predicates.add(builder.like(builder.lower(transaction.get("category")), prefix));
    }
    
    if (cursor != null && !cursor.isBlank()) {
      predicates.add(builder.greaterThan(transaction.<String>get("category"), cursor));
    }

    query
        .select(transaction.get("category"))
        .distinct(true)
        .where(predicates.toArray(Predicate[]::new))
        .orderBy(builder.asc(transaction.get("category")));

    return entityManager.createQuery(query).setMaxResults(limit + 1).getResultList();
  }
}
