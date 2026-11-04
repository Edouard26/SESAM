type FilterValue = string | number | boolean | undefined;

type RsqlOptions = {
    /** Correspondance nom du filtre UI -> propriété Java (ex: { room: "salle.id" }) */
    fieldMap?: Record<string, string>;
    /** Filtres en recherche partielle ("contient") */
    likeFields?: string[];
    /** Un filtre UI qui cherche dans plusieurs champs (OU, "contient").
     *  Ex: { search: ["nom", "numeroSerie", "marque"] } */
    orFields?: Record<string, string[]>;
    nullFields?: string[];
};

// Entoure la valeur de guillemets en échappant \ et " pour la syntaxe RSQL
function quote(value: string): string {
    return `"${value.replace(/\\/g, "\\\\").replace(/"/g, '\\"')}"`;
}

// Ne met des guillemets que si la valeur contient un caractère réservé RSQL
function escapeValue(value: string): string {
    return /[\s"'();,=!~<>]/.test(value) ? quote(value) : value;
}

// Recherche partielle insensible à la casse
function likeValue(value: string): string {
    return quote(value);
}

// Construit la chaîne de filtre RSQL (conditions séparées par ";" = ET) à partir des filtres de l'UI
// Les filtres vides sont ignorés ; renvoie undefined s'il ne reste aucune condition
export function buildRsql(
    filters: Record<string, FilterValue>,
    {
        fieldMap = {},
        likeFields = [],

        orFields = {},
        nullFields = [],
    }: RsqlOptions = {},
): string | undefined {
    const parts = Object.entries(filters)
        .filter(([, v]) => v !== undefined && String(v).trim() !== "")
        .map(([key, v]) => {
            const field = fieldMap[key] ?? key;

            if (nullFields.includes(key)) {
                return `${field}==null`;
            }

            const raw = String(v).trim();
            const group = orFields[key];

            if (group && group.length > 0) {
                return `(${group.map((f) => `${f}=ilike=${likeValue(raw)}`).join(",")})`;
            }

            if (likeFields.includes(key)) {
                return `${field}=ilike=${likeValue(raw)}`;
            }

            return `${field}==${escapeValue(raw)}`;
        });

    return parts.length > 0 ? parts.join(";") : undefined;
}

// Sépare les paramètres à envoyer tels quels (pagination, tri, ids spécifiques : passthrough)
// de ceux qui deviennent le paramètre "filter" RSQL
export function toRsqlParams(
    params: Record<string, FilterValue> = {},
    options?: RsqlOptions,
    passthrough: string[] = ["page", "size", "sort", "salleId", "selectedStageId", "excludeSalleId", "batimentId", "excludeStagiaire", "roomFromUserId"],
): Record<string, FilterValue> {
    const result: Record<string, FilterValue> = {};
    const filters: Record<string, FilterValue> = {};

    for (const [key, value] of Object.entries(params)) {
        if (passthrough.includes(key)) result[key] = value;
        else filters[key] = value;
    }

    const filter = buildRsql(filters, options);
    if (filter) result.filter = filter;

    return result;
}