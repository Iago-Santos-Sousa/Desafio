import { Bar } from "react-chartjs-2";
import {
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LinearScale,
  Tooltip,
} from "chart.js";
import { Paper, Stack, Typography } from "@mui/material";
import { formatCurrency } from "../../utils/format";
import { ErrorState, LoadingState } from "../../components/StateMessage";
import { Skeleton } from "@mui/material";
import { Button, TextField } from "@mui/material";
import { useState } from "react";
import {
  useAggregatesQuery,
  useSummaryQuery,
} from "../../hooks/api/useDashboardQueries";

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

export function AnalyticsPanel({
  from,
  to,
  onRangeChange,
}: {
  from: string;
  to: string;
  onRangeChange: (from: string, to: string) => void;
}) {
  const summary = useSummaryQuery(from, to);
  const aggregates = useAggregatesQuery(from, to);
  const [draftFrom, setDraftFrom] = useState(from);
  const [draftTo, setDraftTo] = useState(to);

  const rows = aggregates.data?.slice(-12) ?? [];

  const chart = {
    labels: rows.map((row) => `${row.month.slice(0, 7)} · ${row.category}`),
    datasets: [
      {
        label: "Valor total",
        data: rows.map((row) => row.totalAmount),
        backgroundColor: "#4f46e5",
        borderRadius: 8,
      },
    ],
  };

  return (
    <>
      {summary.isError && (
        <ErrorState message="NÃ£o foi possÃ­vel carregar resumo." />
      )}
      <Stack direction={{ xs: "column", md: "row" }} spacing={2}>
        {[
          ["Registros", summary.data?.transactionCount ?? 0],
          ["Valor total", formatCurrency(summary.data?.totalAmount ?? 0)],
          ["Categorias", summary.data?.categoryCount ?? 0],
        ].map(([label, value]) => (
          <Paper key={String(label)} className="p-5 flex-1" elevation={0}>
            <Typography color="text.secondary">{label}</Typography>
            {summary.isPending ? (
              <Skeleton variant="text" height={48} />
            ) : (
              <Typography variant="h4" fontWeight={800}>
                {typeof value === "number"
                  ? value.toLocaleString("pt-BR")
                  : value}
              </Typography>
            )}
          </Paper>
        ))}
      </Stack>
      <Paper component="section" className="p-5" elevation={0}>
        <Stack
          direction={{ xs: "column", sm: "row" }}
          justifyContent="space-between"
          alignItems={{ sm: "center" }}
          spacing={2}
          mb={2}
        >
          <Typography variant="h5" fontWeight={700}>
            Resumo mensal
          </Typography>
          <Stack direction={{ xs: "column", sm: "row" }} spacing={1}>
            <TextField
              type="date"
              size="small"
              label="Início"
              value={draftFrom}
              onChange={(event) => setDraftFrom(event.target.value)}
              InputLabelProps={{ shrink: true }}
            />
            <TextField
              type="date"
              size="small"
              label="Fim"
              value={draftTo}
              onChange={(event) => setDraftTo(event.target.value)}
              InputLabelProps={{ shrink: true }}
            />
            <Button
              variant="contained"
              onClick={() => onRangeChange(draftFrom, draftTo)}
              disabled={draftFrom > draftTo || !draftFrom || !draftTo}
            >
              Aplicar
            </Button>
          </Stack>
        </Stack>
        {aggregates.isPending ? (
          <LoadingState />
        ) : aggregates.isError ? (
          <ErrorState message="Não foi possível carregar agregados." />
        ) : rows.length ? (
          <Stack sx={{ height: 300 }}>
            <Bar
              data={chart}
              options={{
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
              }}
            />
          </Stack>
        ) : (
          <Typography color="text.secondary">
            Nenhum agregado disponível.
          </Typography>
        )}
      </Paper>
    </>
  );
}
