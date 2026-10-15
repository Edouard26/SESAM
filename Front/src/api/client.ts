export const BASE_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";
export const TOKEN_KEY = import.meta.env.VITE_TOKEN_KEY ?? "jwt";


export class ApiError extends Error {
    status: number;
    constructor(status: number, message: string) {
        super(message);
        this.status = status;
    }
}

type RequestOptions = {
    method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
    body?: unknown;
    // false pour les routes publiques (ex : login) : pas de token, et un 401 n'y signifie pas "session expirée"
    auth?: boolean;
    params?: Record<string, string | number | boolean | undefined>;
};

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
    const { method = "GET", body, auth = true, params } = options;
    const token = localStorage.getItem(TOKEN_KEY);

    const url = new URL(`${BASE_URL}${path}`);

    Object.entries(params ?? {}).forEach(([key, value]) => {
        if (value !== undefined) {
            url.searchParams.set(key, String(value));
        }
    });

    const res = await fetch(url, {
        method,
        headers: {
            ...(body !== undefined ? { "Content-Type": "application/json" } : {}),
            ...(auth && token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: body !== undefined ? JSON.stringify(body) : undefined,
    });

    if (res.status === 401 && auth) {
        //localStorage.removeItem(TOKEN_KEY);
        //window.location.assign(routes.login);
        throw new ApiError(401, "Non autorisé");
    }

    if (!res.ok) {
        const data = await res.json().catch(() => null);
        throw new ApiError(res.status, data?.message ?? `Erreur ${res.status}`);
    }

    if (res.status === 204 || res.headers.get("content-length") === "0") return undefined as T; // pour les réponses vide
    return res.json() as Promise<T>;
}

export const api = {
    get: <T>(path: string, params?: RequestOptions["params"]) => request<T>(path, { params }),
    post: <T>(path: string, body?: unknown, auth = true) => request<T>(path, { method: "POST", body, auth }),
    patch: <T>(path: string, body?: unknown) => request<T>(path, { method: "PATCH", body }),
    delete: <T>(path: string) => request<T>(path, { method: "DELETE" }),
};