import { api } from "./client.ts";
import type {Material} from "../types/Material.ts";

export const membresApi = {
    list: () => api.get<Material[]>("/api/materials"),
    create: (nom: string) => api.post<Material>("/api/materials", { nom }),
    delete: (id: number) => api.delete<void>(`/api/materials/${id}`),
};