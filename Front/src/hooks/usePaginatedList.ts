import { useEffect, useRef, useState } from "react";
import useNotification from "./useNotification.ts";
import type {Page} from "../types/Page.ts";

type UsePaginatedListOptions<T, TParams extends object> = {
    load: (params: TParams) => Promise<Page<T>>;
    initialFilters: Record<string, string>;
    pageSize: number;
    errorMessage: string;
};

export default function usePaginatedList<T, TParams extends object>({
                                                                        load,
                                                                        initialFilters,
                                                                        pageSize,
                                                                        errorMessage,
                                                                    }: UsePaginatedListOptions<T, TParams>) {
    const { notify } = useNotification();

    const initialFiltersRef = useRef(initialFilters);
    const loadRef = useRef(load);
    const notifyRef = useRef(notify);

    const [search, setSearchState] = useState("");
    const [filters, setFilters] = useState<Record<string, string>>(
        () => ({ ...initialFilters }),
    );
    const [items, setItems] = useState<T[]>([]);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [reloadKey, setReloadKey] = useState(0);

    useEffect(() => {
        loadRef.current = load;
    }, [load]);

    useEffect(() => {
        notifyRef.current = notify;
    }, [notify]);

    useEffect(() => {
        let cancelled = false;

        const activeFilters = Object.fromEntries(
            Object.entries(filters).filter(
                ([, value]) => value.trim() !== "",
            ),
        );

        const params = {
            search,
            ...activeFilters,
            page,
            size: pageSize,
        } as TParams;

        setLoading(true);

        loadRef.current(params)
            .then((data) => {
                if (cancelled) return;

                setItems(data.content);
                setTotalPages(data.totalPages);
            })
            .catch(() => {
                if (cancelled) return;

                notifyRef.current(errorMessage, "danger", 10000);
                setItems([]);
                setTotalPages(0);
            })
            .finally(() => {
                if (!cancelled) {
                    setLoading(false);
                }
            });

        return () => {
            cancelled = true;
        };
    }, [search, filters, page, pageSize, errorMessage, reloadKey]);

    function setSearch(value: string) {
        setSearchState(value);
        setPage(0);
    }

    function handleFilterChange(name: string, value: string) {
        setFilters((previous) => ({
            ...previous,
            [name]: value,
        }));

        setPage(0);
    }

    function resetFilters() {
        setSearchState("");
        setFilters({ ...initialFiltersRef.current });
        setPage(0);
    }

    function reload() {
        setReloadKey((previous) => previous + 1);
    }

    return {
        search,
        filters,
        items,
        loading,
        page,
        totalPages,
        setPage,
        setSearch,
        handleFilterChange,
        resetFilters,
        reload,
    };
}