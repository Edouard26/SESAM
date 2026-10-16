import { api } from "./client.ts";
import type {Page} from "../types/Page.ts";
import type {Room, RoomForm} from "../types/Room.ts";

export const roomApi = {
    list: (params?: Record<string, string | number | boolean | undefined>) =>
        api.get<Page<Room>>("/api/v1/rooms/", params),
    create: (body: RoomForm) =>
        api.post<Room>("/api/v1/rooms/", body),
    update: (id: number, body: Partial<RoomForm>) =>
        api.patch<Room>(`/api/v1/rooms/${id}/`, body),
    delete: (id: number) =>
        api.delete<void>(`/api/v1/rooms/${id}/`),
};