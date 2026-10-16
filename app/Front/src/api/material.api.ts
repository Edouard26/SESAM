import { api } from "./client.ts";
import type {Material, MaterialAssignmentForm, MaterialForm} from "../types/Material.ts";
import type {Page} from "../types/Page.ts";

export const materialApi = {
    list: (params?: Record<string, string | number | boolean | undefined>) =>
        api.get<Page<Material>>("/api/v1/materials/", params),
    create: (body: MaterialForm) =>
        api.post<Material>("/api/v1/materials/", body),
    update: (id: number, body: Partial<MaterialForm>) =>
        api.patch<Material>(`/api/v1/materials/${id}/`, body),
    delete: (id: number) =>
        api.delete<void>(`/api/v1/materials/${id}/`),
    assignments: (body: MaterialAssignmentForm)=> api.post<void>("/api/v1/materials/assignments/", body)
};