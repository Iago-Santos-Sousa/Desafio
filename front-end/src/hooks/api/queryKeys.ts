export const queryKeys = {
  activeIngestions: ["active-ingestions"] as const,

  ingestionJobs: (cursor?: string) =>
    ["ingestion-jobs", cursor ?? "first"] as const,

  ingestion: (jobId: string) => ["ingestion", jobId] as const,

  transactions: (
    cursor: number | undefined,
    category: string,
    jobId?: string,
  ) => ["transactions", cursor, category, jobId ?? "all"] as const,

  categories: (jobId: string, search: string) =>
    ["categories", jobId, search] as const,

  summary: (from: string, to: string) => ["summary", from, to] as const,

  aggregates: (from: string, to: string) => ["aggregates", from, to] as const,
};
