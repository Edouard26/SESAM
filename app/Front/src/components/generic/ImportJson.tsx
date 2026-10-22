import { useMemo, useState } from "react";
import type { ChangeEvent } from "react";
import Filters from "./Filters.tsx";
import Table, { type Column } from "./display/Table.tsx";
import useNotification from "../../hooks/useNotification.ts";

export type ValidationResult<T> = { data?: T; errors: string[] };


type ImportJsonProps<T> = {
    validate: (raw: unknown) => ValidationResult<T>;
    onSelectionChange: (items: T[]) => void;
    // Colonnes affichées : reçoivent directement l'objet validé (T)
    columns: Column<T & { key: number }>[];
};

const pageSize = 5;

export default function ImportJson<T>({ validate, onSelectionChange, columns }: ImportJsonProps<T>) {
    const { notify } = useNotification();
    const [sourceItems, setSourceItems] = useState<unknown[]>([]);
    const [fileName, setFileName] = useState("");
    const [fileError, setFileError] = useState("");
    const [search, setSearch] = useState("");
    const [currentPage, setCurrentPage] = useState(0);
    const [selectedRows, setSelectedRows] = useState<(T & { key: number })[]>([]);
    const [showErrors, setShowErrors] = useState(false);

    const rows = useMemo(() => sourceItems.map((raw, key) => ({ key, ...validate(raw) })), [sourceItems, validate]);

    // Seuls les éléments valides sont conservés
    const validRows = useMemo(
        () => rows.flatMap((row) =>
            row.errors.length === 0 && row.data !== undefined ? [{ ...row.data, key: row.key }] : []
        ),
        [rows]
    );
    const invalidRows = useMemo(() => rows.filter((row) => row.errors.length > 0), [rows]);
    const invalidCount = invalidRows.length;

    const errorGroups = useMemo(() => {
        const groups = new Map<string, number[]>();
        for (const row of invalidRows) {
            for (const error of row.errors) {
                groups.set(error, [...(groups.get(error) ?? []), row.key + 1]);
            }
        }
        return [...groups].map(([message, lines]) => ({ message, lines }));
    }, [invalidRows]);

    function formatRanges(numbers: number[]): string {
        const ranges: string[] = [];
        let start = numbers[0];
        for (let i = 1; i <= numbers.length; i++) {
            if (numbers[i] !== numbers[i - 1] + 1) {
                const end = numbers[i - 1];
                ranges.push(start === end ? `${start}` : `${start}-${end}`);
                start = numbers[i];
            }
        }
        return ranges.join(", ");
    }

    const filteredRows = useMemo(() => {
        const query = search.trim().toLowerCase();
        if (!query) return validRows;
        return validRows.filter((row) => JSON.stringify(row).toLowerCase().includes(query));
    }, [validRows, search]);

    const totalPages = Math.max(1, Math.ceil(filteredRows.length / pageSize));
    const safePage = Math.min(currentPage, totalPages - 1);
    const pageRows = filteredRows.slice(safePage * pageSize, (safePage + 1) * pageSize);

    function updateSelection(next: (T & { key: number })[]) {
        setSelectedRows(next);
        onSelectionChange(next.map((row) => {
            const data: Partial<typeof row> = { ...row };
            delete data.key;
            return data as unknown as T;
        }));
    }

    async function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
        const input = event.currentTarget;
        const file = input.files?.[0];
        if (!file) return;

        setFileName(file.name);
        setFileError("");
        setSourceItems([]);
        setSearch("");
        setCurrentPage(0);
        updateSelection([]);

        try {
            const parsed: unknown = JSON.parse(await file.text());
            if (!Array.isArray(parsed)) {
                setFileError("Le fichier JSON doit contenir un tableau d'éléments.");
            } else if (parsed.length === 0) {
                setFileError("Le fichier ne contient aucun élément.");
                notify("Le fichier ne contient aucun élément", "danger");
            } else {
                setSourceItems(parsed);
                if (parsed.every((item) => validate(item).errors.length > 0)) {
                    notify("Aucun élément valide dans le fichier", "danger");
                }
            }
        } catch {
            setFileError("Le fichier sélectionné ne contient pas un JSON valide.");
            notify("Le fichier sélectionné ne contient pas un JSON valide.");
        } finally {
            input.value = "";
        }
    }

    return (
        <div className="d-flex flex-column gap-3">
            <div>
                <span className="form-label d-block">
                    Source de données (fichier JSON) <span className="text-danger">*</span>
                </span>
                <label className="input-group">
                    <span className="btn btn-outline-secondary">Choisir un fichier</span>
                    <span className="form-control text-truncate">
                        {fileName ||
                            <span className="text-body-secondary">Aucun fichier sélectionné</span>
                        }
                    </span>
                    <input type="file" hidden accept=".json,application/json" onChange={handleFileChange} />
                </label>
            </div>

            {fileError && <div className="alert alert-danger mb-0" role="alert">{fileError}</div>}

            {sourceItems.length > 0 && (
                <>
                    <div className="d-flex flex-wrap align-items-center gap-3 small">
                        <span>{validRows.length} élément(s) valide(s)</span>
                        {invalidCount > 0 && (
                            <>
                                <span className="text-danger">{invalidCount} ignoré(s) (invalides)</span>
                                {validRows.length > 0 && (
                                    <button
                                        type="button"
                                        className="btn btn-link btn-sm p-0"
                                        onClick={() => setShowErrors((current) => !current)}
                                    >
                                        {showErrors ? "Masquer les erreurs" : "Voir les erreurs"}
                                    </button>
                                )}
                            </>
                        )}
                    </div>
                    {invalidCount > 0 && (showErrors || validRows.length === 0) && (
                        <ul className="small mb-0 ps-3 overflow-auto" style={{ maxHeight: "12rem" }}>
                            {errorGroups.map(({ message, lines }) => (
                                <li key={message}>
                                    <strong>{lines.length > 1 ? "Éléments" : "Élément"} {formatRanges(lines)}</strong> : {message}
                                </li>
                            ))}
                        </ul>
                    )}
                    {validRows.length !== 0 && (
                        <>
                            <Filters
                                embedded
                                search={search}
                                searchPlaceholder="Rechercher dans les données..."
                                onSearchChange={(value) => { setSearch(value); setCurrentPage(0); }}
                                selects={[]}
                                values={{}}
                                onFilterChange={() => {}}
                                onReset={() => { setSearch(""); setCurrentPage(0); }}
                            />
                            <Table
                                columns={columns}
                                data={pageRows}
                                currentPage={safePage}
                                totalPages={totalPages}
                                totalElements={filteredRows.length}
                                onPageChange={setCurrentPage}
                                loading={false}
                                selection={{
                                    selected: selectedRows,
                                    getId: (row) => row.key,
                                    onChange: updateSelection,
                                    allItems: filteredRows,
                                }}
                            />
                        </>
                    )}
                </>
            )}
        </div>
    );
}