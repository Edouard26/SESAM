import { api } from "./client.ts";
import type {Material} from "../types/Material.ts";
import type {Page} from "../types/Page.ts";

export const materialApi = {
    list: (params?: Record<string, string | number | boolean | undefined>) => api.get<Page<Material>>("/api/materials", params),
    create: (nom: string) => api.post<Material>("/api/materials", { nom }),
    delete: (id: number) => api.delete<void>(`/api/materials/${id}`),
};