import {
  Alert,
  CircularProgress,
  Stack,
  Typography,
  Button,
} from "@mui/material";
import { FileText, List, Rows3, XCircle } from "lucide-react";
import type { IngestionStatus } from "../../types/api";
import { formatBytes, formatDateTime } from "../../utils/format";
import { JobTransactionsDialog } from "../transactions/JobTransactionsDialog";
import { useState } from "react";
import { MetricCard } from "../../components/ui/MetricCard";
import { SectionCard } from "../../components/ui/SectionCard";
import { StatusBadge } from "../../components/ui/StatusBadge";

export function JobStatusPanel({
  status,
  fetching,
}: {
  status: IngestionStatus;
  fetching: boolean;
}) {
  const [transactionsOpen, setTransactionsOpen] = useState(false);
  return (
    <SectionCard
      title="Status da ingestão"
      icon={<FileText size={22} aria-hidden="true" />}
    >
      <Stack spacing={3}>
        <Stack
          direction="row"
          spacing={1.5}
          alignItems="center"
          aria-live="polite"
        >
          <StatusBadge status={status.status} />
          <Typography variant="body2" color="text.secondary">
            {status.processedRows.toLocaleString("pt-BR")} linhas processadas ·{" "}
            {status.invalidRows.toLocaleString("pt-BR")} inválidas
          </Typography>
          {fetching ? (
            <CircularProgress size={16} aria-label="Atualizando status" />
          ) : null}
        </Stack>
        <Stack direction={{ xs: "column", md: "row" }} gap={2}>
          <MetricCard
            label="Total de linhas"
            value={status.totalRows.toLocaleString("pt-BR")}
            icon={<Rows3 size={22} />}
            tone="primary"
          />
          <MetricCard
            label="Linhas válidas"
            value={status.validRows.toLocaleString("pt-BR")}
            icon={<Rows3 size={22} />}
            tone="success"
          />
          <MetricCard
            label="Linhas inválidas"
            value={status.invalidRows.toLocaleString("pt-BR")}
            icon={<XCircle size={22} />}
            tone="warning"
          />
        </Stack>
        <Stack direction={{ xs: "column", sm: "row" }} gap={2} flexWrap="wrap">
          <Typography variant="body2">
            Arquivo: {status.originalFilename}
          </Typography>
          <Typography variant="body2">
            Tamanho: {formatBytes(status.fileSizeBytes)}
          </Typography>
          <Typography variant="body2">
            Criado: {formatDateTime(status.createdAt)}
          </Typography>
          {status.startedAt ? (
            <Typography variant="body2">
              Iniciado: {formatDateTime(status.startedAt)}
            </Typography>
          ) : null}
          {status.finishedAt ? (
            <Typography variant="body2">
              Finalizado: {formatDateTime(status.finishedAt)}
            </Typography>
          ) : null}
        </Stack>
        <Button
          variant="outlined"
          onClick={() => setTransactionsOpen(true)}
          disabled={status.validRows === 0}
          startIcon={<List size={18} aria-hidden="true" />}
        >
          Visualizar transações
        </Button>
        {status.errorSummary ? (
          <Alert severity="error">{status.errorSummary}</Alert>
        ) : null}
      </Stack>
      <JobTransactionsDialog
        jobId={status.jobId}
        open={transactionsOpen}
        onClose={() => setTransactionsOpen(false)}
      />
    </SectionCard>
  );
}
