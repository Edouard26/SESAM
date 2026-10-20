import type { ReactNode } from "react";
import type {Column} from "../../components/generic/display/Table.tsx";

const formatValue = (value: unknown): ReactNode => {
    if (value === undefined || value === null || value === "") return "—";
    if (value instanceof Date) return value.toLocaleDateString("fr-FR");
    return String(value);
};

// labels : un libellé par clé de T (le compilateur refuse un champ oublié), l'ordre des clés = l'ordre des colonnes
// overrides : rendu personnalisé pour certains champs seulement
export function createColumns<T extends object>(
    labels: Record<keyof T, string>,
    overrides: { [K in keyof T]?: (value: T[K], row: T) => ReactNode } = {},
): Column<T & { key: number }>[] {
    return (Object.keys(labels) as (keyof T & string)[]).map((field) => ({
        label: labels[field],
        render: (row: T & { key: number }) => {
            const custom = overrides[field];
            return custom ? custom(row[field], row) : formatValue(row[field]);
        },
    }));
}