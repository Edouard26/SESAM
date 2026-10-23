import { api } from "./client.ts";
import type {Page} from "../types/Page.ts";
import {toRsqlParams} from "./utils/rsql.ts";
import type {Building, BuildingForm} from "../types/Building.ts";

export const buildingApi = {
    list: (params?: Record<string, string | number | boolean | undefined>) =>
        api.get<Page<Building>>("/api/v1/buildings/", toRsqlParams(params, {
            orFields: { search: ["nom"] },
        })),
    get: (id: number) => api.get<Building>(`/api/v1/buildings/${id}/`),
    create: (body: BuildingForm) =>
        api.post<Building>("/api/v1/buildings/", body),
    update: (id: number, body: Partial<BuildingForm>) =>
        api.patch<Building>(`/api/v1/buildings/${id}/`, body),
    delete: (id: number) =>
        api.delete<void>(`/api/v1/buildings/${id}/`),
};