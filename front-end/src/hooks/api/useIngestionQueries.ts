import {
  keepPreviousData,
  useInfiniteQuery,
  useMutation,
  useQuery,
} from "@tanstack/react-query";
import {
  getActiveIngestions,
  getIngestionJobs,
  getIngestion,
  uploadCsv,
} from "../../integrations/api/ingestions";
import { getCategories } from "../../integrations/api/transactions";
import { queryKeys } from "./queryKeys";

export const useUploadCsvMutation = (onProgress: (value: number) => void) => {
  return useMutation({
    mutationFn: (file: File) => uploadCsv(file, onProgress),
  });
};

export const useIngestionQuery = (jobId: string | undefined) => {
  return useQuery({
    queryKey: queryKeys.ingestion(jobId ?? ""),
    queryFn: () => getIngestion(jobId as string),
    enabled: Boolean(jobId),
    refetchInterval: (query) => {
      const status = query.state.data?.status;

      return status === "COMPLETED" ||
        status === "COMPLETED_WITH_ERRORS" ||
        status === "FAILED"
        ? false
        : status === "PROCESSING"
          ? 2000
          : 1000;
    },
  });
};

export const useActiveIngestionsQuery = () => {
  return useQuery({
    queryKey: queryKeys.activeIngestions,
    queryFn: () => getActiveIngestions(),
    refetchInterval: 2000,
  });
};

export const useIngestionJobsQuery = (cursor?: string) => {
  return useQuery({
    queryKey: queryKeys.ingestionJobs(cursor),
    queryFn: () => getIngestionJobs(cursor),
    placeholderData: keepPreviousData,
    refetchInterval: (query) => {
      const active = query.state.data?.items.some((job) =>
        ["RECEIVED", "QUEUED", "PROCESSING"].includes(job.status),
      );
      return active ? 2000 : false;
    },
  });
};

export const useJobCategoriesQuery = (
  jobId: string,
  search: string,
  enabled: boolean,
) => {
  return useInfiniteQuery({
    queryKey: queryKeys.categories(jobId, search),
    queryFn: ({ pageParam }) => getCategories(jobId, search, pageParam),
    initialPageParam: undefined as string | undefined,
    getNextPageParam: (lastPage) => lastPage.nextCursor ?? undefined,
    enabled,
  });
};
