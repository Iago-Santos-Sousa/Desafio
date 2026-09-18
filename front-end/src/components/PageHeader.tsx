import { Typography } from "@mui/material";

export function PageHeader({
  title,
  description,
}: {
  title: string;
  description: string;
}) {
  return (
    <header>
      <Typography variant="h3" fontWeight={800} color="primary">
        {title}
      </Typography>
      <Typography color="text.secondary">{description}</Typography>
    </header>
  );
}
