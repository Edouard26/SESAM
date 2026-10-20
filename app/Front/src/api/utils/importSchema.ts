import type {ValidationResult} from "../../components/generic/ImportJson.tsx";


type Parsed<T> = { value: T } | { error: string };

export type Field<T> = {
    optional?: boolean;
    parse: (value: unknown) => Parsed<T>;
};

// Une entrée obligatoire par clé de T : le compilateur refuse un champ oublié ou d'un mauvais type
export type Schema<T> = { [K in keyof T]-?: Field<T[K]> };

export const text = (): Field<string> => ({
    parse: (v) => typeof v === "string" ? { value: v } : { error: "doit être une chaîne de caractères" },
});

export const email = (): Field<string> => ({
    parse: (v) => typeof v === "string" && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)
        ? { value: v }
        : { error: "doit être une adresse email valide" },
});

export const date = (): Field<Date> => ({
    parse: (v) => {
        const d = new Date(String(v));
        return Number.isNaN(d.getTime()) ? { error: "doit être une date valide" } : { value: d };
    },
});

export const pastDate = (): Field<Date> => ({
    parse: (v) => {
        const result = date().parse(v);
        if ("error" in result) return result;
        return result.value > new Date() ? { error: "ne peut pas être dans le futur" } : result;
    },
});

export const positiveNumber = (): Field<number> => ({
    parse: (v) => Number.isFinite(Number(v)) && Number(v) > 0
        ? { value: Number(v) }
        : { error: "doit être un nombre positif" },
});

export const oneOf = <T extends string>(values: readonly T[]): Field<T> => ({
    parse: (v) => typeof v === "string" && (values as readonly string[]).includes(v)
        ? { value: v as T }
        : { error: `doit être l'une des valeurs : ${values.join(", ")}` },
});

export const optional = <T,>(field: Field<T>): Field<T | undefined> => ({
    optional: true,
    parse: field.parse,
});

const isEmpty = (v: unknown) => v === undefined || v === null || v === "";

// check : règles entre plusieurs champs (ex. dateFin >= dateDebut), renvoie un message d'erreur ou undefined
export function createValidator<T extends object>(
    schema: Schema<T>,
    check?: (data: T) => string | undefined,
) {
    const keys = Object.keys(schema) as (keyof T & string)[];

    return (raw: unknown): ValidationResult<T> => {
        if (!raw || typeof raw !== "object" || Array.isArray(raw)) {
            return { errors: ["L'élément doit être un objet JSON."] };
        }

        const input = raw as Record<string, unknown>;

        const missing = keys.filter((key) => !schema[key].optional && isEmpty(input[key]));
        if (missing.length > 0) {
            return { errors: [`Champs obligatoires manquants : ${missing.join(", ")}.`] };
        }

        const errors: string[] = [];
        const data: Partial<T> = {};

        for (const key of keys) {
            if (isEmpty(input[key])) continue; // champ optionnel absent
            const result = schema[key].parse(input[key]);
            if ("error" in result) errors.push(`${key} ${result.error}.`);
            else data[key] = result.value as T[typeof key];
        }

        if (errors.length > 0) return { errors };

        const crossError = check?.(data as T);
        return crossError ? { errors: [crossError] } : { errors: [], data: data as T };
    };
}