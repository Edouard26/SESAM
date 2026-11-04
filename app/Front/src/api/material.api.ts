import { api } from "./client.ts";
import type {
    ImportMaterialsPayload,
    Material,
    MaterialDetail,
    MaterialAssignmentForm,
    MaterialForm
} from "../types/Material.ts";
import type {Page} from "../types/Page.ts";
import {toRsqlParams} from "./utils/rsql.ts";
import {toIsoDate} from "./utils/date.ts";

// salleId est traduit en "salle.id" pour le filtre RSQL ; "search" cherche dans n° de série, marque, modèle et nom
export const materialApi = {
    list: (params?: Record<string, string | number | boolean | undefined>) =>
        api.get<Page<Material>>("/api/v1/materials/", toRsqlParams(params, {
            fieldMap: {
                salleId: "salle.id",
                type: "typeMateriel",
            },
            orFields: {
                search: ["numeroSerie", "marque", "modele", "nom"],
            },
        })),
    get: (id: number) => api.get<MaterialDetail>(`/api/v1/materials/${id}/`),
    create: (body: MaterialForm) =>
        api.post<Material>("/api/v1/materials/", serialize(body)),
    update: (id: number, body: Partial<MaterialForm>) =>
        api.patch<Material>(`/api/v1/materials/${id}/`, serialize(body)),
    delete: (id: number) =>
        api.delete<void>(`/api/v1/materials/${id}/`),
    reinstall: (id: number) =>
        api.post<void>(`/api/v1/materials/${id}/reinstall/`),
    assignments: (body: MaterialAssignmentForm)=> api.post<void>("/api/v1/materials/assignments/", body),
    import: (body: ImportMaterialsPayload)=> api.post<void>("/api/v1/materials/import/", {
        ...body,
        materiels: body.materiels.map(serialize),
    })
};

function serialize<T extends Partial<MaterialForm>>(material: T) {
    return {
        ...material,
        ...(material.mac ? { mac: normalizeMac(material.mac) } : {}),
        ...(material.dateAjout ? { dateAjout: toIsoDate(material.dateAjout) } : {}),
        ...(material.dateFinGarantie ? { dateFinGarantie: toIsoDate(material.dateFinGarantie) } : {}),
    };
}

export function normalizeMac(mac: string): string {
    return mac.trim().toLowerCase().replaceAll(":", "-");
}