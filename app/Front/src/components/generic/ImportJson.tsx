import { useMemo, useState } from "react";
import type { ChangeEvent } from "react";
import Filters from "./Filters.tsx";
import Table, { type Column } from "./display/Table.tsx";

export type ValidationResult<T> = { data?: T; errors: string[] };


type ImportJsonProps<T> = {
    validate: (raw: unknown) => ValidationResult<T>;
    onSelectionChange: (items: T[]) => void;
    // Colonnes affichées : reçoivent directement l'objet validé (T)
    columns: Column<T & { key: number }>[];
};

const pageSize = 10;

export default function ImportJson<T>({ validate, onSelectionChange, columns }: ImportJsonProps<T>) {
    const [sourceItems, setSourceItems] = useState<unknown[]>([]);
    const [fileName, setFileName] = useState("");
    const [fileError, setFileError] = useState("");
    const [search, setSearch] = useState("");
    const [currentPage, setCurrentPage] = useState(0);
    const [selectedRows, setSelectedRows] = useState<(T & { key: number })[]>([]);

    const rows = useMemo(() => sourceItems.map((raw, key) => ({ key, ...validate(raw) })), [sourceItems, validate]);

    // Seuls les éléments valides sont conservés
    const validRows = useMemo(
        () => rows.flatMap((row) =>
            row.errors.length === 0 && row.data !== undefined ? [{ ...row.data, key: row.key }] : []
        ),
        [rows]
    );
    const invalidCount = rows.length - validRows.length;

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
        // On retire la clé technique avant de remonter les données
        onSelectionChange(next.map(({ key: _key, ...data }) => data as unknown as T));
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
            } else {
                setSourceItems(parsed);
            }
        } catch {
            setFileError("Le fichier sélectionné ne contient pas un JSON valide.");
        } finally {
            input.value = "";
        }
    }

    return (
        <div className="d-flex flex-column gap-3">
            <div>
                <label htmlFor="import-file" className="form-label">
                    Source de données (fichier JSON) <span className="text-danger">*</span>
                </label>
                <input
                    id="import-file"
                    type="file"
                    className="form-control"
                    accept=".json,application/json"
                    onChange={handleFileChange}
                />
                {fileName && <div className="form-text">Fichier sélectionné : {fileName}</div>}
            </div>

            {fileError && <div className="alert alert-danger mb-0" role="alert">{fileError}</div>}

            {sourceItems.length > 0 && (
                <>
                    <div className="d-flex flex-wrap gap-3 small">
                        <span>{validRows.length} élément(s) valide(s)</span>
                        {invalidCount > 0 && (
                            <span className="text-danger">{invalidCount} ignoré(s) (invalides)</span>
                        )}
                    </div>
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
        </div>
    );
}