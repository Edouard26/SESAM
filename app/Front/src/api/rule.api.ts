import { api } from "./client.ts";
import type { Page } from "../types/Page.ts";
import type { Rule } from "../types/Rule.ts";
import type { RuleForm } from "../types/RuleForm.ts";
import { toRsqlParams } from "./utils/rsql.ts";

export const ruleApi = {
    list: (params?: Record<string, string | number | boolean | undefined>) =>
        api.get<Page<Rule>>("/api/v1/rules/", toRsqlParams(params, {
            orFields: { search: ["nom", "processusNom"] },
        })),
    get: (id: number) => api.get<Rule>(`/api/v1/rules/${id}/`),
    create: (body: RuleForm) => api.post<Rule>("/api/v1/rules/", body),
    update: (id: number, body: RuleForm) => api.patch<Rule>(`/api/v1/rules/${id}/`, body),
    delete: (id: number) => api.delete<void>(`/api/v1/rules/${id}/`),
};
