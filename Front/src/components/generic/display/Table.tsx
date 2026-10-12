import { Fragment, useState } from "react";

type Column<T> = {
    key: keyof T;
    label: string;
};

type TableProps<T> = {
    columns: Column<T>[];
    data: T[];
    pageSize?: number;
};

export default function Table<T>({ columns, data, pageSize: initialPageSize = 10 }: TableProps<T>) {
    const [currentPage, setCurrentPage] = useState(1);
    const [pageSize, setPageSize] = useState(initialPageSize);

    const totalPages = Math.ceil(data.length / pageSize);
    const startIndex = (currentPage - 1) * pageSize;
    const currentData = data.slice(startIndex, startIndex + pageSize);

    const goToPage = (page: number) => {
        setCurrentPage(Math.max(1, Math.min(page, totalPages)));
    };

    const handlePageSizeChange = (newPageSize: number) => {
        setPageSize(newPageSize);
        setCurrentPage(1);
    };

    const pages = Array.from(
        { length: totalPages },
        (_, index) => index + 1,
    ).filter((page) => {
        if (totalPages <= 7) {
            return true;
        }

        return (
            page === 1 ||
            page === totalPages ||
            Math.abs(page - currentPage) <= 1
        );
    });

    return (
        <div>
            <div className="table-responsive">
                <table className="table table-hover align-middle mb-0">
                    <thead>
                    <tr>
                        {columns.map((column) => (
                            <th key={String(column.key)}>{column.label}</th>
                        ))}
                    </tr>
                    </thead>

                    <tbody>
                    {currentData.map((item, index) => (
                        <tr key={index}>
                            {columns.map((column) => (
                                <td key={String(column.key)}>
                                    {String(item[column.key])}
                                </td>
                            ))}
                        </tr>
                    ))}

                    {currentData.length === 0 && (
                        <tr>
                            <td
                                colSpan={columns.length}
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
                <div className="d-flex justify-content-between align-items-center mt-3">
                    <div className="d-flex align-items-center gap-2">
                        <span className="small text-body-secondary">
                            Éléments par page
                        </span>

                        <select
                            className="form-select form-select-sm"
                            style={{ width: "auto" }}
                            value={pageSize}
                            onChange={(event) =>
                                handlePageSizeChange(Number(event.target.value))
                            }
                        >
                            <option value={10}>10</option>
                            <option value={25}>25</option>
                            <option value={50}>50</option>
                            <option value={100}>100</option>
                        </select>
                    </div>

                    <nav aria-label="Pagination">
                        <ul className="pagination pagination-sm mb-0">
                            <li
                                className={`page-item ${
                                    currentPage === 1 ? "disabled" : ""
                                }`}
                            >
                                <button
                                    className="page-link"
                                    onClick={() => goToPage(currentPage - 1)}
                                    disabled={currentPage === 1}
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
                                                <span className="page-link">
                                                    ...
                                                </span>
                                            </li>
                                        )}

                                        <li
                                            className={`page-item ${
                                                currentPage === page ? "active" : ""
                                            }`}
                                        >
                                            <button
                                                className="page-link"
                                                onClick={() => goToPage(page)}
                                            >
                                                {page}
                                            </button>
                                        </li>
                                    </Fragment>
                                );
                            })}

                            <li
                                className={`page-item ${
                                    currentPage === totalPages ? "disabled" : ""
                                }`}
                            >
                                <button
                                    className="page-link"
                                    onClick={() => goToPage(currentPage + 1)}
                                    disabled={currentPage === totalPages}
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
    );
}
