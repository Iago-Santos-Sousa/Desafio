import type { CategoryPage, TransactionPage } from "../../types/api";
import { api } from "./client";

export const getTransactions = async (
  cursor?: number,
  category?: string,
  jobId?: string,
  size = 25,
): Promise<TransactionPage> => {
  const response = await api.get<TransactionPage>("/api/v1/transactions", {
    params: { cursor, category: category || undefined, jobId, size },
  });

  return response.data;
};

export const getCategories = async (
  jobId: string,
  search: string,
  cursor?: string,
  size = 5,
): Promise<CategoryPage> => {
  const response = await api.get<CategoryPage>(
    "/api/v1/transactions/categories",
    {
      params: { jobId, search: search || undefined, cursor, size },
    },
  );

  return response.data;
};
