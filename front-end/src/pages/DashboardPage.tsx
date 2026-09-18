import { Stack } from "@mui/material";
import { useState } from "react";
import { useSearchParams } from "react-router";
import { PageHeader } from "../components/PageHeader";
import { AnalyticsPanel } from "../features/dashboard/AnalyticsPanel";
import { ActiveIngestionsPanel } from "../features/ingestions/ActiveIngestionsPanel";
import { useActiveIngestionsQuery } from "../hooks/api/useIngestionQueries";
import {
  defaultDashboardDateRange,
  isValidDateRange,
} from "../utils/dateRange";

export function DashboardPage() {
  const [params, setParams] = useSearchParams();
  const defaults = useState(defaultDashboardDateRange)[0];
  const from = params.get("from") ?? defaults.from;
  const to = params.get("to") ?? defaults.to;
  const activeIngestions = useActiveIngestionsQuery();

  const updateRange = (nextFrom: string, nextTo: string) => {
    if (!isValidDateRange(nextFrom, nextTo)) return;
    setParams({ from: nextFrom, to: nextTo });
  };

  return (
    <Stack spacing={3}>
      <PageHeader
        title="DataPulse"
        description="Ingestão financeira em larga escala, sem travar sua tela."
      />
      <ActiveIngestionsPanel
        jobs={activeIngestions.data ?? []}
        loading={activeIngestions.isPending}
      />
      <AnalyticsPanel
        key={`${from}-${to}`}
        from={from}
        to={to}
        onRangeChange={updateRange}
      />
    </Stack>
  );
}
