import { Fragment } from "react";
import Loader from "../Loader.tsx";
import type { ReactNode } from "react";

export type Column<T> = {
    key?: keyof T;
    label: string;
    render?: (item: T) => ReactNode;
};

type TableSelection<T> = {
    selected: T[];
    getId: (item: T) => string | number;
    onChange: (selected: T[]) => void;
    /**
     * Ensemble complet des éléments sélectionnables (toutes pages confondues).
     * Si fourni, la case d'en-tête sélectionne/désélectionne tous ces éléments.
     * Sinon, elle ne agit que sur la page affichée.
     */
    allItems?: T[];
};

type TableProps<T> = {
    columns: Column<T>[];
    data: T[];
    currentPage: number;
    totalPages: number;
    totalElements: number;
    onPageChange: (page: number) => void;
    loading?: boolean;
    selection?: TableSelection<T>;
};

export default function Table<T>({
                                     columns,
                                     data,
                                     currentPage,
                                     totalElements,
                                     totalPages,
                                     onPageChange,
                                     loading = false,
                                     selection,
                                 }: TableProps<T>) {
    const pages = Array.from({ length: totalPages }, (_, index) => index).filter((page) => {
        if (totalPages <= 5) {
            return true;
        }

        return page === 0 || page === totalPages - 1 || Math.abs(page - currentPage) <= 1;
    });

    const selectedIds = new Set(
        selection?.selected.map(selection.getId) ?? [],
    );

    const selectableItems = selection?.allItems ?? data;

    const allSelected =
        selection !== undefined &&
        selectableItems.length > 0 &&
        selectableItems.every((item) => selectedIds.has(selection.getId(item)));

    const someSelected =
        selection !== undefined &&
        !allSelected &&
        selectableItems.some((item) => selectedIds.has(selection.getId(item)));

    function toggleItem(item: T, checked: boolean) {
        if (!selection) return;

        const id = selection.getId(item);

        const selected = selection.selected.filter(
            (selectedItem) => selection.getId(selectedItem) !== id,
        );

        if (checked) {
            selected.push(item);
        }

        selection.onChange(selected);
    }

    function toggleAll(checked: boolean) {
        if (!selection) return;

        const ids = new Set(selectableItems.map(selection.getId));

        const selected = selection.selected.filter(
            (item) => !ids.has(selection.getId(item)),
        );

        if (checked) {
            selected.push(...selectableItems);
        }

        selection.onChange(selected);
    }

    return (
        <div className="position-relative border" aria-busy={loading}>
            <div className={loading ? "opacity-50" : ""}>
                <div className="table-responsive">
                    <table className="table table-hover align-middle mb-0">
                        <thead>
                        <tr>
                            {selection && (
                                <th scope="col" className="text-center">
                                    <input
                                        type="checkbox"
                                        className="form-check-input"
                                        aria-label={
                                            selection.allItems
                                                ? "Sélectionner tous les éléments"
                                                : "Sélectionner tous les éléments de cette page"
                                        }
                                        checked={allSelected}
                                        ref={(element) => {
                                            if (element) element.indeterminate = someSelected;
                                        }}
                                        onChange={(event) =>
                                            toggleAll(event.target.checked)
                                        }
                                    />
                                </th>
                            )}

                            {columns.map((column) => (
                                <th key={String(column.key ?? column.label)}>
                                    {column.label}
                                </th>
                            ))}
                        </tr>
                        </thead>

                        <tbody>
                        {data.map((item, index) => {
                            const id = selection?.getId(item);

                            return (
                                <tr key={selection ? id : index}>
                                    {selection && (
                                        <td className="text-center">
                                            <input
                                                type="checkbox"
                                                className="form-check-input"
                                                aria-label={`Sélectionner l'élément ${String(id)}`}
                                                checked={selectedIds.has(id!)}
                                                onChange={(event) =>
                                                    toggleItem(item, event.target.checked)
                                                }
                                            />
                                        </td>
                                    )}

                                    {columns.map((column) => (
                                        <td key={String(column.key ?? column.label)}>
                                            {column.render
                                                ? column.render(item)
                                                : column.key
                                                    ? String(item[column.key] ?? "")
                                                    : ""}
                                        </td>
                                    ))}
                                </tr>
                            );
                        })}

                        {data.length === 0 && (
                            <tr>
                                <td
                                    colSpan={columns.length + (selection ? 1 : 0)}
                                    className="text-center text-body-secondary py-4"
                                >
                                    Aucun élément
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </div>

                {totalPages > 0 && (
                    <div className="d-flex justify-content-between align-items-center p-3">
                        <span className="text-body-secondary small">{totalElements} éléments</span>
                        <nav aria-label="Pagination">
                            <ul className="pagination pagination-sm mb-0">
                                <li
                                    className={`page-item ${
                                        loading || currentPage === 0 ? "disabled" : ""
                                    }`}
                                >
                                    <button
                                        type="button"
                                        className="page-link"
                                        disabled={loading || currentPage === 0}
                                        onClick={() => onPageChange(currentPage - 1)}
                                        aria-label="Page précédente"
                                    >
                                        ‹
                                    </button>
                                </li>

                                {pages.map((page, index) => {
                                    const previousPage = pages[index - 1];
                                    const showEllipsis =
                                        previousPage !== undefined &&
                                        page - previousPage > 1;

                                    return (
                                        <Fragment key={page}>
                                            {showEllipsis && (
                                                <li className="page-item disabled">
                                                    <span className="page-link">...</span>
                                                </li>
                                            )}

                                            <li
                                                className={`page-item ${
                                                    currentPage === page ? "active" : ""
                                                }`}
                                            >
                                                <button
                                                    type="button"
                                                    className="page-link"
                                                    disabled={loading}
                                                    onClick={() => onPageChange(page)}
                                                >
                                                    {page + 1}
                                                </button>
                                            </li>
                                        </Fragment>
                                    );
                                })}

                                <li
                                    className={`page-item ${
                                        loading || currentPage >= totalPages - 1
                                            ? "disabled"
                                            : ""
                                    }`}
                                >
                                    <button
                                        type="button"
                                        className="page-link"
                                        disabled={loading || currentPage >= totalPages - 1}
                                        onClick={() => onPageChange(currentPage + 1)}
                                        aria-label="Page suivante"
                                    >
                                        ›
                                    </button>
                                </li>
                            </ul>
                        </nav>
                    </div>
                )}
            </div>

            {loading && (
                <div
                    className="position-absolute top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center bg-body bg-opacity-50"
                    style={{ zIndex: 10 }}
                >
                    <Loader text="Chargement..." />
                </div>
            )}
        </div>
    );
}