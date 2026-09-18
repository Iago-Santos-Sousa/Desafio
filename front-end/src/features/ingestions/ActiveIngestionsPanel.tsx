import {
  LinearProgress,
  Paper,
  Skeleton,
  Stack,
  Typography,
} from "@mui/material";
import type { IngestionStatus } from "../../types/api";

export function ActiveIngestionsPanel({
  jobs,
  loading,
}: {
  jobs: IngestionStatus[];
  loading: boolean;
}) {
  if (!loading && jobs.length === 0) return null;

  return (
    <Paper component="section" className="p-5" elevation={0}>
      <Typography variant="h5" fontWeight={700} mb={2}>
        Processamento em tempo real
      </Typography>
      {loading ? (
        <Stack spacing={1}>
          <Skeleton variant="text" width="60%" />
          <Skeleton variant="rounded" height={8} />
        </Stack>
      ) : (
        <Stack spacing={2}>
          {jobs.map((job) => (
            <Stack key={job.jobId} spacing={0.5}>
              <Typography variant="body2">
                {job.originalFilename} · {job.status}
              </Typography>
              <LinearProgress variant="indeterminate" />
              <Typography
                variant="caption"
                color="text.secondary"
                aria-live="polite"
              >
                {job.processedRows.toLocaleString("pt-BR")} linhas lidas ·{" "}
                {job.validRows.toLocaleString("pt-BR")} válidas ·{" "}
                {job.invalidRows.toLocaleString("pt-BR")} inválidas
              </Typography>
            </Stack>
          ))}
        </Stack>
      )}
    </Paper>
  );
}
