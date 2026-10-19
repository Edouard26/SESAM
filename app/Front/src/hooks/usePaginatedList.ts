import { useEffect, useRef, useState } from "react";
import useNotification from "./useNotification.ts";
import type {Page} from "../types/Page.ts";

type QueryParams = Record<
    string,
    string | number | boolean | undefined
>;

type UsePaginatedListOptions<T> = {
    load: (params?: QueryParams) => Promise<Page<T>>;
    initialFilters: Record<string, string>;
    pageSize: number;
    errorMessage: string;
    enabled?: boolean;
    loadKey?: string | number;
};

export default function usePaginatedList<T>({ load, initialFilters, pageSize, errorMessage, enabled = true, loadKey}: UsePaginatedListOptions<T> ) {

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
    const [totalElements, setTotalElements] = useState(0);
    const [reloadKey, setReloadKey] = useState(0);

    useEffect(() => {
        loadRef.current = load;
    }, [load]);

    useEffect(() => {
        notifyRef.current = notify;
    }, [notify]);

    useEffect(() => {
        if (!enabled) {
            setItems([]);
            setTotalPages(0);
            setTotalElements(0);
            setLoading(false);
            return;
        }

        let cancelled = false;

        const activeFilters = Object.fromEntries(
            Object.entries(filters).filter(
                ([, value]) => value.trim() !== "",
            ),
        );

        const params = { search, ...activeFilters, page, size: pageSize,};

        setLoading(true);

        loadRef.current(params)
            .then((data) => {
                if (cancelled) return;

                setItems(data.content);
                setTotalPages(data.totalPages);
                setTotalElements(data.totalElements)
            })
            .catch(() => {
                if (cancelled) return;

                notifyRef.current(errorMessage, "danger", 10000);
                setItems([]);
                setTotalPages(0);
                setTotalElements(0);
            })
            .finally(() => {
                if (!cancelled) setLoading(false);
            });

        return () => {
            cancelled = true;
        };
    }, [enabled, loadKey, search, filters, page, pageSize, errorMessage, reloadKey]);

    function setSearch(value: string) {
        setSearchState(value);
        setPage(0);
    }

    function handleFilterChange(name: string, value: string) {
        setFilters((previous) => ({ ...previous, [name]: value,}));
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

    return {search, filters, items, loading, page, totalPages, totalElements, setPage, setSearch, handleFilterChange, resetFilters, reload };
}