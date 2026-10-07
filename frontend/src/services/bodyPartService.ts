
const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api";

export interface BodyPart {
    id: number;
    name: string;
    active: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface BodyPartRequest {
    name: string;
    active: boolean;
}

export interface PageResponse<T> {
    content: T[];
    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
    numberOfElements: number;
}

interface ApiError {
    message?: string;
}

async function request<T>(
    path: string,
    options?: RequestInit,
): Promise<T> {
    const response = await fetch(`${API_BASE_URL}${path}`, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...options?.headers,
        },
    });

    if (!response.ok) {
        const error = (await response.json().catch(() => ({}))) as ApiError;
        throw new Error(error.message ?? `Request failed (${response.status})`);
    }

    if (response.status === 204) {
        return undefined as T;
    }

    return response.json() as Promise<T>;
}

export function getBodyParts(params: {
    search?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    direction?: "asc" | "desc";
} = {}): Promise<PageResponse<BodyPart>> {
    const query = new URLSearchParams({
        page: String(params.page ?? 0),
        size: String(params.size ?? 10),
        sortBy: params.sortBy ?? "name",
        direction: params.direction ?? "asc",
    });

    if (params.search?.trim()) {
        query.set("search", params.search.trim());
    }

    return request<PageResponse<BodyPart>>(
        `/body-parts?${query.toString()}`,
    );
}

export function createBodyPart(
    body: BodyPartRequest,
): Promise<BodyPart> {
    return request<BodyPart>("/body-parts", {
        method: "POST",
        body: JSON.stringify(body),
    });
}

export function updateBodyPart(
    id: number,
    body: BodyPartRequest,
): Promise<BodyPart> {
    return request<BodyPart>(`/body-parts/${id}`, {
        method: "PUT",
        body: JSON.stringify(body),
    });
}

export function deleteBodyPart(id: number): Promise<void> {
    return request<void>(`/body-parts/${id}`, {
        method: "DELETE",
    });
}
