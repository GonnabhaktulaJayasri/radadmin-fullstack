const API_URL = `${import.meta.env.VITE_API_BASE_URL}/cases`;

export type CaseStatus =
  | "INCOMPLETE"
  | "REPORT_NOT_FOUND"
  | "REPORT_NOT_VERIFIED"
  | "REPORT_COMPLETED"
  | "REPORT_SAVED";

export interface CaseRecord {
  id: number;
  accessionNumber: string;
  patientName: string;
  patientDob: string | null;
  patientGender: string | null;
  history: string | null;
  technique: string | null;
  bodyPartId: number;
  bodyPartName: string;
  modality: string | null;
  radiologistId: number;
  radiologistName: string;
  status: CaseStatus;
  report: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CaseRequest {
  accessionNumber: string;
  patientName: string;
  patientDob: string | null;
  patientGender: string;
  history: string;
  technique: string;
  bodyPartId: number;
  modality: string;
  radiologistId: number;
  status: CaseStatus;
  report: string;
}

export interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

async function request<T>(
  url: string,
  options?: RequestInit,
): Promise<T> {
  const response = await fetch(url, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...options?.headers,
    },
  });

  if (!response.ok) {
    let message = `Request failed (${response.status})`;

    try {
      const error = await response.json();
      message = error.message || message;
    } catch {
      // Keep the default message if the response isn't JSON.
    }

    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export function getCases(params: {
  search?: string;
  status?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  direction?: string;
}) {
  const query = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== "") {
      query.set(key, String(value));
    }
  });

  return request<PageResult<CaseRecord>>(
    `${API_URL}?${query.toString()}`,
  );
}

export function getCase(id: number) {
  return request<CaseRecord>(`${API_URL}/${id}`);
}

export function createCase(data: CaseRequest) {
  return request<CaseRecord>(API_URL, {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export function updateCase(id: number, data: CaseRequest) {
  return request<CaseRecord>(`${API_URL}/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

export function deleteCase(id: number) {
  return request<void>(`${API_URL}/${id}`, {
    method: "DELETE",
  });
}