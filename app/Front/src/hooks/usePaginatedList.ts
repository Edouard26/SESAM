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
    initialSort?: string;
};

// Gère une liste paginée côté serveur : recherche, filtres, tri, page courante, chargement et erreurs
// Tout changement de recherche/filtre/tri revient à la première page ; reload() force un nouvel appel
// enabled=false vide la liste sans appeler l'API ; loadKey relance le chargement quand il change
export default function usePaginatedList<T>({ load, initialFilters, pageSize, errorMessage, enabled = true, loadKey, initialSort = ""}: UsePaginatedListOptions<T> ) {

    const { notify } = useNotification();

    // Refs pour garder les dernières valeurs sans relancer l'effet de chargement à chaque rendu
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
    const [sort, setSortState] = useState(initialSort);
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

        // Évite qu'une réponse obsolète écrase une plus récente (ou arrive après le démontage)
        let cancelled = false;

        const activeFilters = Object.fromEntries(
            Object.entries(filters).filter(
                ([, value]) => value.trim() !== "",
            ),
        );

        const params = { search, ...activeFilters, page, size: pageSize, ...(sort ? { sort } : {}) };

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
    }, [enabled, loadKey, search, filters, page, pageSize, errorMessage, reloadKey, sort]);

    function setSort(value: string) {
        setSortState(value);
        setPage(0);
    }

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

    return {search, filters, items, loading, page, totalPages, totalElements, sort, setSort, setPage, setSearch, handleFilterChange, resetFilters, reload };
}
