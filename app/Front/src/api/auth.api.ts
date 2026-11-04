import { api } from "./client.ts";
import type {User} from "../types/User.ts";

export type LoginResponse = {
    accessToken: string;
    expireIn: number;
    userDto: User;
};


// Routes publiques (auth=false) : pas de token envoyé ; refresh s'appuie sur le cookie de session (credentials: include)
export const authApi = {
    login: (email: string, password: string) =>
        api.post<LoginResponse>("/api/v1/auth/login/", { email, password }, false),
    refresh: () => api.post<LoginResponse>("/api/v1/auth/refresh/", undefined, false),
};
