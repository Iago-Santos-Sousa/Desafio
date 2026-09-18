export type JobStatus =
  | "RECEIVED"
  | "QUEUED"
  | "PROCESSING"
  | "COMPLETED"
  | "COMPLETED_WITH_ERRORS"
  | "FAILED";

export interface IngestionStatus {
  jobId: string;
  originalFilename: string;
  status: JobStatus;
  totalRows: number;
  processedRows: number;
  validRows: number;
  invalidRows: number;
  fileSizeBytes: number;
  createdAt: string;
  startedAt?: string;
  finishedAt?: string;
  updatedAt: string;
  errorSummary?: string;
}

export interface IngestionJobListItem {
  jobId: string;
  originalFilename: string;
  fileSizeBytes: number;
  status: JobStatus;
  createdAt: string;
  updatedAt: string;
}

export interface IngestionJobPage {
  items: IngestionJobListItem[];
  nextCursor: string | null;
}

export interface UploadAccepted {
  jobId: string;
  status: JobStatus;
  statusUrl: string;
}

export interface ApiProblem {
  status?: number;
  code?: string;
  title?: string;
  detail?: string;
  timestamp?: string;
  traceId?: string;
}

export interface Summary {
  transactionCount: number;
  totalAmount: number;
  categoryCount: number;
}

export interface Aggregate {
  month: string;
  category: string;
  totalAmount: number;
  transactionCount: number;
}

export interface CategoryPage {
  items: string[];
  nextCursor: string | null;
}

export interface Transaction {
  id: number;
  jobId: string;
  occurredAt: string;
  category: string;
  amount: number;
  description: string;
}

export interface TransactionPage {
  items: Transaction[];
  nextCursor: number | null;
}
