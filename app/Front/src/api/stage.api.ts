import { api } from "./client.ts";
import type {ImportStagesPayload, Stage, StageForm} from "../types/Stage.ts";
import type {Page} from "../types/Page.ts";
import {toRsqlParams} from "./utils/rsql.ts";

export const stageApi = {
    list: (params?: Record<string, string | number | boolean | undefined>) =>
        api.get<Page<Stage>>("/api/v1/stages/", toRsqlParams(params, {
            orFields: { search: ["code"] },
        })),
    get: (id: number) => api.get<Stage>(`/api/v1/stages/${id}/`),
    create: (body: StageForm) =>
        api.post<Stage>("/api/v1/stages/", body),
    update: (id: number, body: Partial<StageForm>) =>
        api.patch<Stage>(`/api/v1/stages/${id}/`, body),
    delete: (id: number) =>
        api.delete<void>(`/api/v1/stages/${id}/`),
    import: (body: ImportStagesPayload)=> api.post<void>("/api/v1/stages/import/", body)
};