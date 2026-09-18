import { Stack } from "@mui/material";
import { useState } from "react";
import { useSearchParams } from "react-router";
import { PageHeader } from "../components/PageHeader";
import { AnalyticsPanel } from "../features/dashboard/AnalyticsPanel";
import { JobsPanel } from "../features/ingestions/JobsPanel";
import { useIngestionJobsQuery } from "../hooks/api/useIngestionQueries";
import {
  defaultDashboardDateRange,
  isValidDateRange,
} from "../utils/dateRange";

export function DashboardPage() {
  const [params, setParams] = useSearchParams();
  const defaults = useState(defaultDashboardDateRange)[0];
  const from = params.get("from") ?? defaults.from;
  const to = params.get("to") ?? defaults.to;
  const [cursor, setCursor] = useState<string>();
  const [history, setHistory] = useState<string[]>([]);
  const jobs = useIngestionJobsQuery(cursor);

  const updateRange = (nextFrom: string, nextTo: string) => {
    if (!isValidDateRange(nextFrom, nextTo)) return;
    setParams({ from: nextFrom, to: nextTo });
  };

  const next = () => {
    if (jobs.data?.nextCursor) {
      setHistory((old) => [...old, cursor ?? ""]);
      setCursor(jobs.data.nextCursor);
    }
  };

  const previous = () => {
    const old = [...history];
    const value = old.pop();
    setHistory(old);
    setCursor(value || undefined);
  };

  return (
    <Stack spacing={3}>
      <PageHeader
        title="DataPulse"
        description="Ingestão financeira em larga escala, sem travar sua tela."
      />
      <JobsPanel
        jobs={jobs.data?.items ?? []}
        loading={jobs.isPending}
        error={jobs.isError}
        hasNext={Boolean(jobs.data?.nextCursor)}
        hasPrevious={history.length > 0}
        onNext={next}
        onPrevious={previous}
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
