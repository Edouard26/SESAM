import { api } from "./client.ts";
import type { Master, MasterForm } from "../types/Master.ts";
import type { Page } from "../types/Page.ts";
import {toRsqlParams} from "./utils/rsql.ts";

export const masterApi = {
    list: (params?: Record<string, string | number | boolean | undefined>) =>
        api.get<Page<Master>>("/api/v1/masters/", toRsqlParams(params, {
            orFields: { search: ["nom"] },
        })),
    get: (id: number) =>
        api.get<Master>(`/api/v1/masters/${id}/`),

    create: (body: MasterForm) =>
        api.post<Master>("/api/v1/masters/", body),
};