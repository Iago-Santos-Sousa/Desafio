import { Stack } from "@mui/material";
import { PageHeader } from "../components/PageHeader";
import { UploadPanel } from "../features/ingestions/UploadPanel";

export function NewIngestionPage() {
  return (
    <Stack spacing={3}>
      <PageHeader
        title="Nova ingestão"
        description="Envie CSV para processamento assíncrono."
      />
      <UploadPanel />
    </Stack>
  );
}
