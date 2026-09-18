package com.desafio.ingestion.ingestion;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IngestionJobRepository extends JpaRepository<IngestionJob, UUID> {
  List<IngestionJob> findByStatusInOrderByCreatedAtAsc(
      Collection<JobStatus> statuses, org.springframework.data.domain.Pageable pageable);

  @Modifying
  @Query(
      "update IngestionJob j set j.status=:status, j.processedRows=:processed, j.validRows=:valid,"
          + " j.invalidRows=:invalid, j.updatedAt=CURRENT_TIMESTAMP where j.id=:id")
  int updateProgress(
      @Param("id") UUID id,
      @Param("status") JobStatus status,
      @Param("processed") long processed,
      @Param("valid") long valid,
      @Param("invalid") long invalid);
}
