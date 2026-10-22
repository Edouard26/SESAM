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

function quote(value: string): string {
    return `"${value.replace(/\\/g, "\\\\").replace(/"/g, '\\"')}"`;
}

function escapeValue(value: string): string {
    return /[\s"'();,=!~<>]/.test(value) ? quote(value) : value;
}

// Recherche partielle : == avec jokers. Les jokers doivent être DANS les guillemets.
function likeValue(value: string): string {
    return quote(`*${value}*`);
}

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
                return `(${group.map((f) => `${f}==${likeValue(raw)}`).join(",")})`;
            }

            if (likeFields.includes(key)) {
                return `${field}==${likeValue(raw)}`;
            }

            return `${field}==${escapeValue(raw)}`;
        });

    return parts.length > 0 ? parts.join(";") : undefined;
}

export function toRsqlParams(
    params: Record<string, FilterValue> = {},
    options?: RsqlOptions,
    passthrough: string[] = ["page", "size", "sort", "salleId", "selectedStageId", "excludeSalleId"],
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