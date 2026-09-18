import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { getAggregates, getSummary } from "../../integrations/api/analytics";
import { getTransactions } from "../../integrations/api/transactions";
import { queryKeys } from "./queryKeys";

export const useSummaryQuery = (from: string, to: string) => {
  return useQuery({
    queryKey: queryKeys.summary(from, to),
    queryFn: () => getSummary(from, to),
  });
};

export const useAggregatesQuery = (from: string, to: string) => {
  return useQuery({
    queryKey: queryKeys.aggregates(from, to),
    queryFn: () => getAggregates(from, to),
  });
};

export const useTransactionsQuery = (
  cursor: number | undefined,
  category: string,
  jobId?: string,
  size = 25,
  enabled = true,
) => {
  return useQuery({
    queryKey: queryKeys.transactions(cursor, category, jobId),
    queryFn: () => getTransactions(cursor, category, jobId, size),
    placeholderData: keepPreviousData,
    enabled,
  });
};
