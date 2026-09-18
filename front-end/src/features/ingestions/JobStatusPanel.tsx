import {
  Chip,
  CircularProgress,
  Paper,
  Stack,
  Typography,
  Button,
} from "@mui/material";
import type { IngestionStatus } from "../../types/api";
import { statusText } from "../../utils/ingestionStatus";
import { formatBytes, formatDateTime } from "../../utils/format";
import { useState } from "react";
import { JobTransactionsDialog } from "../transactions/JobTransactionsDialog";

export function JobStatusPanel({
  status,
  fetching,
}: {
  status: IngestionStatus;
  fetching: boolean;
}) {
  const [transactionsOpen, setTransactionsOpen] = useState(false);

  const color =
    status.status === "FAILED"
      ? "error"
      : status.status === "COMPLETED"
        ? "success"
        : "info";

  return (
    <Paper component="section" className="p-6" elevation={0}>
      <Stack spacing={2}>
        <Typography variant="h5" fontWeight={700}>
          Status da ingestão
        </Typography>
        <Stack
          direction="row"
          spacing={1}
          alignItems="center"
          aria-live="polite"
        >
          <Chip label={statusText[status.status]} color={color} />
          <Typography>
            {status.processedRows.toLocaleString("pt-BR")} linhas processadas ·{" "}
            {status.invalidRows.toLocaleString("pt-BR")} inválidas
          </Typography>
          {fetching && <CircularProgress size={16} />}
        </Stack>
        <Stack
          direction={{ xs: "column", sm: "row" }}
          spacing={2}
          flexWrap="wrap"
        >
          <Typography variant="body2">
            Arquivo: {status.originalFilename}
          </Typography>
          <Typography variant="body2">
            Tamanho: {formatBytes(status.fileSizeBytes)}
          </Typography>
          <Typography variant="body2">
            Total: {status.totalRows.toLocaleString("pt-BR")}
          </Typography>
          <Typography variant="body2">
            Válidas: {status.validRows.toLocaleString("pt-BR")}
          </Typography>
          <Typography variant="body2">
            Criado: {formatDateTime(status.createdAt)}
          </Typography>
          {status.startedAt && (
            <Typography variant="body2">
              Iniciado: {formatDateTime(status.startedAt)}
            </Typography>
          )}
          {status.finishedAt && (
            <Typography variant="body2">
              Finalizado: {formatDateTime(status.finishedAt)}
            </Typography>
          )}
        </Stack>
        <Button
          variant="outlined"
          onClick={() => setTransactionsOpen(true)}
          disabled={status.validRows === 0}
        >
          Visualizar transações
        </Button>
        {status.errorSummary && (
          <Typography color="error">{status.errorSummary}</Typography>
        )}
      </Stack>
      <JobTransactionsDialog
        jobId={status.jobId}
        open={transactionsOpen}
        onClose={() => setTransactionsOpen(false)}
      />
    </Paper>
  );
}
