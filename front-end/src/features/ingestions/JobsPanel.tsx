import {
  Button,
  Chip,
  LinearProgress,
  Paper,
  Skeleton,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import { Link } from "react-router";
import type { IngestionJobListItem } from "../../types/api";
import { formatBytes, formatDateTime } from "../../utils/format";
import { statusText } from "../../utils/ingestionStatus";

const activeStatuses = new Set(["RECEIVED", "QUEUED", "PROCESSING"]);

export function JobsPanel({
  jobs,
  loading,
  hasNext,
  hasPrevious,
  onNext,
  onPrevious,
  error,
}: {
  jobs: IngestionJobListItem[];
  loading: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
  onNext: () => void;
  onPrevious: () => void;
  error: boolean;
}) {
  return (
    <Paper component="section" className="p-5" elevation={0}>
      <Typography variant="h5" fontWeight={700} mb={2}>
        Jobs processados
      </Typography>
      {error && (
        <Typography color="error" sx={{ py: 2 }}>
          Não foi possível carregar jobs.
        </Typography>
      )}
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>Arquivo</TableCell>
            <TableCell>Tamanho</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Criado em</TableCell>
            <TableCell align="right">Ações</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {loading
            ? Array.from({ length: 5 }, (_, index) => (
                <TableRow key={`job-skeleton-${index}`}>
                  {Array.from({ length: 5 }, (_, cell) => (
                    <TableCell key={cell}>
                      <Skeleton />
                    </TableCell>
                  ))}
                </TableRow>
              ))
            : jobs.map((job) => {
                const active = activeStatuses.has(job.status);
                return (
                  <TableRow key={job.jobId} hover>
                    <TableCell>{job.originalFilename}</TableCell>
                    <TableCell>{formatBytes(job.fileSizeBytes)}</TableCell>
                    <TableCell>
                      <Stack spacing={0.5} minWidth={130}>
                        <Chip
                          size="small"
                          label={statusText[job.status]}
                          color={
                            job.status === "FAILED"
                              ? "error"
                              : job.status === "COMPLETED"
                                ? "success"
                                : "info"
                          }
                        />
                        {active && (
                          <LinearProgress aria-label="Job em processamento" />
                        )}
                      </Stack>
                    </TableCell>
                    <TableCell>{formatDateTime(job.createdAt)}</TableCell>
                    <TableCell align="right">
                      <Button
                        component={Link}
                        to={`/ingestions/${job.jobId}`}
                        size="small"
                        variant="outlined"
                      >
                        Ver detalhes
                      </Button>
                    </TableCell>
                  </TableRow>
                );
              })}
        </TableBody>
      </Table>
      {!loading && jobs.length === 0 && (
        <Typography color="text.secondary" sx={{ py: 3 }}>
          Nenhum job encontrado.
        </Typography>
      )}
      <Stack direction="row" justifyContent="flex-end" spacing={1} mt={2}>
        <Button onClick={onPrevious} disabled={!hasPrevious}>
          Anterior
        </Button>
        <Button onClick={onNext} variant="outlined" disabled={!hasNext}>
          Próxima
        </Button>
      </Stack>
    </Paper>
  );
}
