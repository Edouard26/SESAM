import { api } from "./client.ts";
import type {Page} from "../types/Page.ts";
import type {Room, RoomForm} from "../types/Room.ts";
import {toRsqlParams} from "./utils/rsql.ts";

// "search" cherche dans le nom de la salle ; updateRules remplace la liste des règles de la salle
export const roomApi = {
    list: (params?: Record<string, string | number | boolean | undefined>) =>
        api.get<Page<Room>>("/api/v1/rooms/", toRsqlParams(params, {
            orFields: { search: ["nom"] },
        })),
    get: (id: number) => api.get<Room>(`/api/v1/rooms/${id}/`),
    create: (body: RoomForm) =>
        api.post<Room>("/api/v1/rooms/", body),
    update: (id: number, body: Partial<RoomForm>) =>
        api.patch<Room>(`/api/v1/rooms/${id}/`, body),
    delete: (id: number) =>
        api.delete<void>(`/api/v1/rooms/${id}/`),
    reinstall: (id: number) =>
        api.post<void>(`/api/v1/rooms/${id}/reinstall/`),
    updateRules: (id: number, reglesId: number[]) =>
        api.patch<Room>(`/api/v1/rooms/${id}/rules/`, { reglesId }),
};
