package com.desafio.ingestion.ingestion;

import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IngestionJobQueryRepository {
  private final JdbcTemplate jdbc;

  public IngestionJobQueryRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<IngestionJobListItem> findPage(int limit, IngestionCursorCodec.Cursor cursor) {
    String base =
        "SELECT id, original_filename, file_size_bytes, status, created_at, updated_at "
            + "FROM ingestion_job ";
    List<Object> args = new ArrayList<>();
    String sql;
    if (cursor == null) {
      sql = base + "ORDER BY created_at DESC, id DESC LIMIT ?";
    } else {
      sql =
          base
              + "WHERE created_at < ? OR (created_at = ? AND id < ?) "
              + "ORDER BY created_at DESC, id DESC LIMIT ?";
      args.add(java.sql.Timestamp.from(cursor.createdAt()));
      args.add(java.sql.Timestamp.from(cursor.createdAt()));
      args.add(cursor.jobId());
    }
    args.add(limit + 1);
    return jdbc.query(
        sql,
        args.toArray(),
        (rs, rowNum) ->
            new IngestionJobListItem(
                rs.getObject("id", java.util.UUID.class),
                rs.getString("original_filename"),
                rs.getLong("file_size_bytes"),
                JobStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()));
  }
}
