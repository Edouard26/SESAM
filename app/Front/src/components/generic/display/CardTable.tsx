import { Fragment } from "react";
import type { EntityCardConfig } from "./EntityCard.tsx";
import EntityCard from "./EntityCard.tsx";
import Loader from "../Loader.tsx";

type CardTableProps<T> = {
    data: T[];
    loading?: boolean;
    pagination: {
        currentPage: number;
        totalPages: number;
        onPageChange: (page: number) => void;
    };
    card: EntityCardConfig<T>;
};

export default function CardTable<T>({
                                         data,
                                         loading = false,
                                         pagination,
                                         card,
                                     }: CardTableProps<T>) {
    const { currentPage, totalPages, onPageChange } = pagination;

    const pages = Array.from(
        { length: totalPages },
        (_, index) => index,
    ).filter((page) => {
        if (totalPages <= 5) {
            return true;
        }

        return (
            page === 0 ||
            page === totalPages - 1 ||
            Math.abs(page - currentPage) <= 1
        );
    });

    return (
        <div className="position-relative" aria-busy={loading}>
            <div className={loading ? "opacity-50" : ""}>
                {data.length > 0 ? (
                    <div className="row row-cols-1 row-cols-md-2 row-cols-xl-3 g-4">
                        {data.map((item, index) => (
                            <div className="col" key={index}>
                                <EntityCard item={item} {...card} />
                            </div>
                        ))}
                    </div>
                ) : (
                    <div className="d-flex align-items-center justify-content-center text-center text-body-secondary py-5 w-100">
                        Aucun élément
                    </div>
                )}

                {totalPages > 0 && (
                    <div className="d-flex justify-content-end pt-4">
                        <nav aria-label="Pagination">
                            <ul className="pagination pagination-sm mb-0">
                                <li
                                    className={`page-item ${
                                        loading || currentPage === 0
                                            ? "disabled"
                                            : ""
                                    }`}
                                >
                                    <button
                                        type="button"
                                        className="page-link"
                                        disabled={
                                            loading || currentPage === 0
                                        }
                                        onClick={() =>
                                            onPageChange(currentPage - 1)
                                        }
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
                                                    currentPage === page
                                                        ? "active"
                                                        : ""
                                                }`}
                                            >
                                                <button
                                                    type="button"
                                                    className="page-link"
                                                    disabled={loading}
                                                    onClick={() =>
                                                        onPageChange(page)
                                                    }
                                                >
                                                    {page + 1}
                                                </button>
                                            </li>
                                        </Fragment>
                                    );
                                })}

                                <li
                                    className={`page-item ${
                                        loading ||
                                        currentPage >= totalPages - 1
                                            ? "disabled"
                                            : ""
                                    }`}
                                >
                                    <button
                                        type="button"
                                        className="page-link"
                                        disabled={
                                            loading ||
                                            currentPage >= totalPages - 1
                                        }
                                        onClick={() =>
                                            onPageChange(currentPage + 1)
                                        }
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
                <div className="position-absolute top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center bg-body bg-opacity-50 z-3">
                    <Loader text="Chargement..." />
                </div>
            )}
        </div>
    );
}