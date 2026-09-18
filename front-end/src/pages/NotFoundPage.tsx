import { Button, Stack, Typography } from "@mui/material";
import { Link } from "react-router";

export function NotFoundPage() {
  return (
    <Stack spacing={2} alignItems="flex-start">
      <Typography variant="h4">Página não encontrada</Typography>
      <Button component={Link} to="/dashboard" variant="contained">
        Ir ao dashboard
      </Button>
    </Stack>
  );
}
