import { api } from "./client.ts";
import type { Stats } from "../types/Stats.ts";

export const statsApi = {
    get: () => api.get<Stats>("/api/v1/stats/"),
};
