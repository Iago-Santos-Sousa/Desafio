import { Button, Stack } from "@mui/material";
import { useEffect, useRef } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { Link, useParams } from "react-router";
import { PageHeader } from "../components/PageHeader";
import { JobStatusPanel } from "../features/ingestions/JobStatusPanel";
import { terminalStatuses } from "../utils/ingestionStatus";
import { ErrorState, LoadingState } from "../components/StateMessage";
import { useIngestionQuery } from "../hooks/api/useIngestionQueries";
import { useToast } from "../context/useToast";

export function IngestionStatusPage() {
  const { jobId } = useParams<{ jobId: string }>();
  const queryClient = useQueryClient();
  const { showToast } = useToast();
  const status = useIngestionQuery(jobId);
  const previousStatus = useRef(status.data?.status);

  useEffect(() => {
    if (status.data && terminalStatuses.includes(status.data.status)) {
      void queryClient.invalidateQueries({ queryKey: ["summary"] });
      void queryClient.invalidateQueries({ queryKey: ["aggregates"] });
      void queryClient.invalidateQueries({ queryKey: ["transactions"] });
      void queryClient.invalidateQueries({ queryKey: ["ingestion-jobs"] });
      void queryClient.invalidateQueries({ queryKey: ["active-ingestions"] });

      if (previousStatus.current !== status.data.status)
        showToast({
          message:
            status.data.errorSummary ??
            (status.data.status === "FAILED"
              ? "Ingestão falhou."
              : status.data.status === "COMPLETED_WITH_ERRORS"
                ? "Ingestão concluída com erros."
                : "Ingestão concluída."),
          severity:
            status.data.status === "FAILED"
              ? "error"
              : status.data.status === "COMPLETED_WITH_ERRORS"
                ? "warning"
                : "success",
          durationMs: status.data.status === "FAILED" ? 8000 : 5000,
        });
    }
    previousStatus.current = status.data?.status;
  }, [queryClient, showToast, status.data]);

  return (
    <Stack spacing={3}>
      <PageHeader title="Acompanhamento" description={`Job ${jobId ?? ""}`} />
      {status.isLoading && <LoadingState />}
      {status.isError && (
        <ErrorState message="Não foi possível consultar ingestão." />
      )}
      {status.data && (
        <JobStatusPanel status={status.data} fetching={status.isFetching} />
      )}
      <Button component={Link} to="/dashboard" variant="outlined">
        Voltar ao dashboard
      </Button>
    </Stack>
  );
}
