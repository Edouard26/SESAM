import { api } from "./client.ts";
import type {ImportUsersPayload, User, UserForm} from "../types/User.ts";
import type {Page} from "../types/Page.ts";
import {toRsqlParams} from "./utils/rsql.ts";
import type {MaterialAssignmentUserForm} from "../types/Material.ts";

export const userApi = {
    list: (params?: Record<string, string | number | boolean | undefined>) =>
        api.get<Page<User>>("/api/v1/users/", toRsqlParams(params, {
            orFields: { search: ["nid", "email", "nom", "prenom"] },
        })),
    get: (id: number) => api.get<User>(`/api/v1/users/${id}/`),
    create: (body: UserForm) =>
        api.post<User>("/api/v1/users/", body),
    update: (id: number, body: Partial<UserForm>) =>
        api.patch<User>(`/api/v1/users/${id}/`, body),
    delete: (id: number) =>
        api.delete<void>(`/api/v1/users/${id}/`),
    import: (body: ImportUsersPayload)=> api.post<void>("/api/v1/users/import/", body),
    assignments: (body: MaterialAssignmentUserForm)=> api.post<void>("/api/v1/users/assignments/", body),
};