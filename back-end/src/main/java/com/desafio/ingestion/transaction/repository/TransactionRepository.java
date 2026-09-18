package com.desafio.ingestion.transaction.repository;

import com.desafio.ingestion.transaction.dto.TransactionDto;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionRepository {
  private final JdbcTemplate jdbc;

  public TransactionRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<TransactionDto> findPage(int limit, Long cursor, UUID jobId, String category) {
    StringBuilder sql =
        new StringBuilder(
            "SELECT id, ingestion_job_id, occurred_at, category, amount, description FROM"
                + " transaction_record WHERE 1=1");
    List<Object> args = new ArrayList<>();
    if (cursor != null) {
      sql.append(" AND id > ?");
      args.add(cursor);
    }
    if (jobId != null) {
      sql.append(" AND ingestion_job_id = ?");
      args.add(jobId);
    }
    if (category != null && !category.isBlank()) {
      sql.append(" AND category = ?");
      args.add(category.trim());
    }
    sql.append(" ORDER BY id LIMIT ?");
    args.add(limit + 1);
    return jdbc.query(
        sql.toString(),
        args.toArray(),
        (rs, rowNum) ->
            new TransactionDto(
                rs.getLong("id"), UUID.fromString(rs.getString("ingestion_job_id")),
                rs.getTimestamp("occurred_at").toInstant(), rs.getString("category"),
                rs.getBigDecimal("amount"), rs.getString("description")));
  }

  public List<String> findCategories(int limit, UUID jobId, String search, String cursor) {
    StringBuilder sql =
        new StringBuilder(
            "SELECT DISTINCT category FROM transaction_record WHERE ingestion_job_id = ?");
    List<Object> args = new ArrayList<>();
    args.add(jobId);
    if (search != null && !search.isBlank()) {
      sql.append(" AND lower(category) LIKE lower(?)");
      args.add(search.trim() + "%");
    }
    if (cursor != null && !cursor.isBlank()) {
      sql.append(" AND category > ?");
      args.add(cursor);
    }
    sql.append(" ORDER BY category LIMIT ?");
    args.add(limit + 1);
    return jdbc.query(sql.toString(), args.toArray(), (rs, rowNum) -> rs.getString("category"));
  }
}
