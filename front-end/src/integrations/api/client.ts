import axios, { AxiosError } from "axios";
import type { ApiProblem } from "../../types/api";

export class ApiClientError extends Error {
  readonly status?: number;
  readonly code?: string;
  readonly detail?: string;
  readonly traceId?: string;
  readonly method?: string;
  readonly url?: string;

  constructor(
    message: string,
    fields: Omit<ApiClientError, "name" | "message"> = {},
  ) {
    super(message);
    this.name = "ApiClientError";
    Object.assign(this, fields);
  }
}

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? "http://localhost:8080",
  timeout: 30000,
});

const startedAt = new WeakMap<object, number>();

function problemOf(value: unknown): ApiProblem {
  if (typeof value !== "object" || value === null) return {};

  const candidate = value as Record<string, unknown>;

  return {
    status: typeof candidate.status === "number" ? candidate.status : undefined,
    code: typeof candidate.code === "string" ? candidate.code : undefined,
    title: typeof candidate.title === "string" ? candidate.title : undefined,
    detail: typeof candidate.detail === "string" ? candidate.detail : undefined,
    timestamp:
      typeof candidate.timestamp === "string" ? candidate.timestamp : undefined,
    traceId:
      typeof candidate.traceId === "string" ? candidate.traceId : undefined,
  };
}

api.interceptors.request.use((config) => {
  startedAt.set(config, performance.now());
  return config;
});

api.interceptors.response.use(
  (response) => {
    const durationMs = Math.round(
      performance.now() - (startedAt.get(response.config) ?? performance.now()),
    );

    if (import.meta.env.DEV)
      console.info("[api.response]", {
        method: response.config.method?.toUpperCase(),
        url: response.config.url,
        status: response.status,
        durationMs,
      });

    return response;
  },
  (error: unknown) => {
    if (!axios.isAxiosError(error)) throw error;

    const axiosError = error as AxiosError<unknown>;
    const response = axiosError.response;
    const problem = problemOf(response?.data);
    const status = response?.status ?? problem.status;
    const method = axiosError.config?.method?.toUpperCase();
    const url = axiosError.config?.url;
    const traceId = response?.headers?.["x-trace-id"] ?? problem.traceId;

    const detail =
      problem.detail ??
      (axiosError.code === "ECONNABORTED"
        ? "Tempo limite excedido."
        : "Não foi possível comunicar com API.");

    const normalized = new ApiClientError(detail, {
      status,
      code: problem.code,
      detail,
      traceId,
      method,
      url,
    });

    console.error("[api.error]", {
      method,
      url,
      status,
      code: normalized.code,
      traceId,
      message: detail,
    });

    return Promise.reject(normalized);
  },
);
