const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

export interface Radiologist {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  specialization: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface RadiologistRequest {
  name: string;
  email: string;
  phone?: string;
  specialization?: string;
  active?: boolean;
}

export interface RadiologistPage {
  content: Radiologist[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface ApiError {
  status?: number;
  error?: string;
  message?: string;
  timestamp?: string;
  path?: string;
  errors?: Record<string, string>;
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let errorData: ApiError = {};

    try {
      errorData = await response.json();
    } catch {
      // Ignore JSON parsing failure
    }

    throw new Error(
      errorData.message || "Something went wrong. Please try again."
    );
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json();
}

export async function getRadiologists(
  search = "",
  page = 0,
  size = 10,
  sortBy = "name",
  direction = "asc"
): Promise<RadiologistPage> {
  const params = new URLSearchParams();

  if (search.trim()) {
    params.append("search", search.trim());
  }

  params.append("page", page.toString());
  params.append("size", size.toString());
  params.append("sortBy", sortBy);
  params.append("direction", direction);

  const response = await fetch(
    `${API_BASE_URL}/radiologists?${params.toString()}`
  );

  return handleResponse<RadiologistPage>(response);
}

export async function getRadiologistById(
  id: number
): Promise<Radiologist> {
  const response = await fetch(
    `${API_BASE_URL}/radiologists/${id}`
  );

  return handleResponse<Radiologist>(response);
}

export async function createRadiologist(
  request: RadiologistRequest
): Promise<Radiologist> {
  const response = await fetch(`${API_BASE_URL}/radiologists`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(request),
  });

  return handleResponse<Radiologist>(response);
}

export async function updateRadiologist(
  id: number,
  request: RadiologistRequest
): Promise<Radiologist> {
  const response = await fetch(
    `${API_BASE_URL}/radiologists/${id}`,
    {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    }
  );

  return handleResponse<Radiologist>(response);
}

export async function deleteRadiologist(id: number): Promise<void> {
  const response = await fetch(
    `${API_BASE_URL}/radiologists/${id}`,
    {
      method: "DELETE",
    }
  );

  await handleResponse<void>(response);
}