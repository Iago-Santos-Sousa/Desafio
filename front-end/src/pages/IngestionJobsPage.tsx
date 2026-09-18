import { Stack } from "@mui/material";
import { PageHeader } from "../components/PageHeader";
import { JobsPanel } from "../features/ingestions/JobsPanel";
import { useIngestionJobsQuery } from "../hooks/api/useIngestionQueries";
import { useCursorPagination } from "../hooks/useCursorPagination";

export function IngestionJobsPage() {
  const { cursor, hasPrevious, next, previous } =
    useCursorPagination<string>();
  const jobs = useIngestionJobsQuery(cursor);

  return (
    <Stack spacing={3}>
      <PageHeader
        title="Jobs processados"
        description="Consulte ingestões aceitas e acompanhe seus detalhes."
      />
      <JobsPanel
        jobs={jobs.data?.items ?? []}
        loading={jobs.isPending}
        error={jobs.isError}
        hasNext={Boolean(jobs.data?.nextCursor)}
        hasPrevious={hasPrevious}
        onNext={() => next(jobs.data?.nextCursor)}
        onPrevious={previous}
      />
    </Stack>
  );
}
