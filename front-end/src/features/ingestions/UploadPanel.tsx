import { useState, type ChangeEvent } from "react";
import {
  Alert,
  Button,
  LinearProgress,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import { useNavigate } from "react-router";
import { useUploadCsvMutation } from "../../hooks/api/useIngestionQueries";
import { useToast } from "../../context/useToast";
import { ApiClientError } from "../../integrations/api/client";

export function UploadPanel() {
  const navigate = useNavigate();
  const [file, setFile] = useState<File>();
  const [progress, setProgress] = useState(0);
  const { showToast } = useToast();
  const upload = useUploadCsvMutation(setProgress);

  const onFile = (event: ChangeEvent<HTMLInputElement>) => {
    const selected = event.target.files?.[0];

    if (selected) {
      setFile(selected);
      upload.reset();
      setProgress(0);
    }
  };

  return (
    <Paper component="section" className="p-6" elevation={0}>
      <Stack spacing={2}>
        <Typography variant="h5" fontWeight={700}>
          Enviar arquivo CSV
        </Typography>
        <Stack
          direction={{ xs: "column", md: "row" }}
          spacing={2}
          alignItems={{ md: "center" }}
        >
          <Button component="label" variant="outlined">
            {file?.name ?? "Selecionar CSV"}
            <input
              hidden
              type="file"
              accept=".csv,text/csv"
              onChange={onFile}
            />
          </Button>
          <Button
            variant="contained"
            disabled={!file || upload.isPending}
            onClick={() =>
              file &&
              upload.mutate(file, {
                onSuccess: (data) => {
                  showToast({
                    message: "Upload aceito. Processamento iniciado.",
                    severity: "success",
                  });
                  navigate(`/ingestions/${data.jobId}`);
                },
                onError: (error) =>
                  showToast({
                    message:
                      error instanceof ApiClientError
                        ? (error.detail ?? error.message)
                        : "Falha no upload.",
                    severity: "error",
                    durationMs: 8000,
                  }),
              })
            }
          >
            {upload.isPending ? "Enviando…" : "Iniciar ingestão"}
          </Button>
          {upload.isError && (
            <Alert severity="error">
              Falha no upload. Verifique CSV e API.
            </Alert>
          )}
        </Stack>
        {upload.isPending && (
          <Stack spacing={1}>
            <Typography variant="body2">Upload: {progress}%</Typography>
            <LinearProgress variant="determinate" value={progress} />
          </Stack>
        )}
      </Stack>
    </Paper>
  );
}
