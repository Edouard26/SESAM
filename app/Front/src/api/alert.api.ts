import { api } from "./client.ts";
import type {Alert, AlertForm, AlertUpdateForm} from "../types/Alert.ts";
import type {Page} from "../types/Page.ts";
import {toRsqlParams} from "./utils/rsql.ts";

export const alertApi = {
    list: (params?: Record<string, string | number | boolean | undefined>) =>
        api.get<Page<Alert>>("/api/v1/alerts/", toRsqlParams(params)),
    get: (id: number) => api.get<Alert>(`/api/v1/alerts/${id}/`),
    create: (body: AlertForm) =>
        api.post<Alert>("/api/v1/alerts/", body),
    update: (id: number, body: AlertUpdateForm) =>
        api.patch<Alert>(`/api/v1/alerts/${id}/`, body),
    delete: (id: number) =>
        api.delete<void>(`/api/v1/alerts/${id}/`),
};