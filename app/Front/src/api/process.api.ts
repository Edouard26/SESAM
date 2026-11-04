import { api } from "./client.ts";
import type { Page } from "../types/Page.ts";
import type { Processus } from "../types/Processus.ts";

// Les paramètres sont envoyés tels quels (pas de filtre RSQL) : ex. uniteCentralId
export const processApi = {
    list: (params?: Record<string, string | number | boolean | undefined>) =>
        api.get<Page<Processus>>("/api/v1/process/", params),
};
