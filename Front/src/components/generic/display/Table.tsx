import { Fragment } from "react";
import Loader from "../Loader.tsx";
import type { ReactNode } from "react";

export type Column<T> = {
    key?: keyof T;
    label: string;
    render?: (item: T) => ReactNode;
};

type TableProps<T> = {
    columns: Column<T>[];
    data: T[];
    currentPage: number;
    totalPages: number;
    onPageChange: (page: number) => void;
    loading?: boolean;
};

export default function Table<T>({ columns, data, currentPage, totalPages, onPageChange, loading = false }: TableProps<T>) {
    const pages = Array.from({ length: totalPages }, (_, index) => index,).filter((page) => {
        if (totalPages <= 5) {
            return true;
        }
        return (page === 0 || page === totalPages - 1 || Math.abs(page - currentPage) <= 1);
    });

    return (
        <div className="position-relative border" aria-busy={loading}>
            <div className={loading ? "opacity-50" : ""}>
                <div className="table-responsive">
                    <table className="table table-hover align-middle mb-0" >
                        <thead>
                            <tr>
                                {columns.map((column) => (
                                    <th key={String(column.key ?? column.label)}>
                                        {column.label}
                                    </th>
                                ))}
                            </tr>
                        </thead>

                        <tbody>
                            {data.map((item, index) => (
                                <tr key={index}>
                                    {columns.map((column) => (
                                        <td key={String(column.key ?? column.label)}>
                                            {column.render ? column.render(item) : column.key
                                                ? String(item[column.key] ?? "")
                                                : ""}
                                        </td>
                                    ))}
                                </tr>
                            ))}

                            {data.length === 0 && (
                                <tr>
                                    <td colSpan={columns.length} className="text-center text-body-secondary py-4">
                                        Aucun élément
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>

                {totalPages > 0 && (
                    <div className="d-flex justify-content-end p-3">
                        <nav aria-label="Pagination">
                            <ul className="pagination pagination-sm mb-0">
                                <li className={`page-item ${ loading || currentPage === 0 ? "disabled" : "" }`}>
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
                                    const showEllipsis = previousPage !== undefined && page - previousPage > 1;
                                    return (
                                        <Fragment key={page}>
                                            {showEllipsis && (
                                                <li className="page-item disabled">
                                                <span className="page-link">
                                                    ...
                                                </span>
                                                </li>
                                            )}

                                            <li className={`page-item ${currentPage === page ? "active" : ""}`} >
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

                                <li className={`page-item ${ loading || currentPage >= totalPages - 1 ? "disabled" : "" }`}>
                                    <button
                                        type="button"
                                        className="page-link"
                                        disabled={
                                            loading || currentPage >= totalPages - 1
                                        }
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