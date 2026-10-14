import { api } from "./client.ts";
import type {User} from "../types/User.ts";

export type LoginResponse = {
    accessToken: string;
    expireIn: number;
    userDto: User;
};


export const authApi = {
    login: (email: string, password: string) =>
        api.post<LoginResponse>("/api/v1/auth/login/", { email, password }, false),
    me: () => api.get<User>("/api/v1/auth/me/"),
};