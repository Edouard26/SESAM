
import { useEffect, useId, useRef, useState } from "react";

type MultiSelectProps<TOption, TValue extends string | number> = {
    searchOptions: (query: string) => Promise<{ content: TOption[] }>;
    getOptionValue: (option: TOption) => TValue;
    getOptionLabel: (option: TOption) => string;
    value: TValue[];
    onChange: (value: TValue[]) => void;
    selectedOptions?: TOption[];
    maxOptions?: number;
    maxSelected?: number;
    placeholder?: string;
    disabled?: boolean;
};

// Liste déroulante à choix multiples avec recherche côté serveur (searchOptions) ; les valeurs sélectionnées
// sont conservées dans un cache pour afficher leur libellé même quand elles ne sont plus dans les résultats
export default function MultiSelect<TOption, TValue extends string | number>({
                                                                                 searchOptions,
                                                                                 getOptionValue,
                                                                                 getOptionLabel,
                                                                                 value,
                                                                                 onChange,
                                                                                 selectedOptions = [],
                                                                                 disabled = false,
                                                                                 maxOptions = 10,
                                                                                 maxSelected,
                                                                                 placeholder = "Sélectionner des éléments",
                                                                             }: MultiSelectProps<TOption, TValue>) {
    const [open, setOpen] = useState(false);
    const [search, setSearch] = useState("");
    const [options, setOptions] = useState<TOption[]>([]);
    const [optionCache, setOptionCache] = useState<Map<TValue, TOption>>(new Map());
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(false);

    const root = useRef<HTMLDivElement>(null);
    const searchInput = useRef<HTMLInputElement>(null);
    const searchOptionsRef = useRef(searchOptions);
    const getOptionValueRef = useRef(getOptionValue);
    const menuId = useId();

    useEffect(() => {
        searchOptionsRef.current = searchOptions;
        getOptionValueRef.current = getOptionValue;
    }, [searchOptions, getOptionValue]);

    useEffect(() => {
        if (open) searchInput.current?.focus();
    }, [open]);

    // Recharge les options à l'ouverture et à chaque changement de recherche
    useEffect(() => {
        if (!open) return;

        let active = true;

        setLoading(true);
        setError(false);
        setOptions([]);

        async function loadOptions() {
            try {
                const result = await searchOptions(search);

                if (!active) return;

                setOptions(result.content);

                setOptionCache(previous => {
                    const next = new Map(previous);

                    result.content.forEach(option => {
                        next.set(getOptionValue(option), option);
                    });

                    return next;
                });
            } catch {
                if (!active) return;

                setOptions([]);
                setError(true);
            } finally {
                if (active) setLoading(false);
            }
        }

        void loadOptions();

        return () => {
            active = false;
        };
    }, [open, search, searchOptions, getOptionValue]);

    // Fermeture en cliquant à l'extérieur
    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (root.current && !root.current.contains(event.target as Node)) {
                setOpen(false);
                setSearch("");
            }
        }

        document.addEventListener("mousedown", handleClickOutside);

        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, []);

    // Fermeture au clavier
    useEffect(() => {
        function handleEscape(event: KeyboardEvent) {
            if (event.key === "Escape") {
                setOpen(false);
                setSearch("");
            }
        }

        document.addEventListener("keydown", handleEscape);

        return () => {
            document.removeEventListener("keydown", handleEscape);
        };
    }, []);

    // Synchronisation du cache avec les options sélectionnées
    useEffect(() => {
        if (selectedOptions.length === 0) return;

        setOptionCache(previous => {
            const next = new Map(previous);

            selectedOptions.forEach(option => {
                next.set(getOptionValue(option), option);
            });

            return next;
        });
    }, [selectedOptions, getOptionValue]);

    // Options sélectionnées retrouvées par valeur (props en priorité, sinon cache)
    const selected = value
        .map(optionValue =>
            selectedOptions.find(option => getOptionValue(option) === optionValue)
            ?? optionCache.get(optionValue)
        )
        .filter((option): option is TOption => option !== undefined);

    const visible = options.slice(0, Math.max(1, Math.floor(maxOptions)));
    const limitReached = maxSelected !== undefined && maxSelected > 1 && value.length >= maxSelected;

    function toggleOption(option: TOption) {
        const optionValue = getOptionValue(option);

        if (value.includes(optionValue)) {
            onChange(value.filter(item => item !== optionValue));
            return;
        }

        if (limitReached) return;

        setOptionCache(previous => {
            const next = new Map(previous);
            next.set(optionValue, option);
            return next;
        });

        const nextValue = maxSelected === 1
            ? [optionValue]
            : [...value, optionValue];

        onChange(nextValue);

        if (maxSelected !== undefined && nextValue.length >= maxSelected) {
            setOpen(false);
            setSearch("");
        }
    }

    function toggleDropdown() {
        if (disabled) return;

        if (open) {
            setSearch("");
            setOpen(false);
        } else {
            setOpen(true);
        }
    }

    return (
        <div className="position-relative w-100" ref={root}>
            <div
                role="button"
                tabIndex={disabled ? -1 : 0}
                aria-expanded={open}
                aria-controls={menuId}
                aria-disabled={disabled}
                className={`border rounded p-2 d-flex align-items-center gap-2 ${
                    disabled ? "bg-body-tertiary" : ""
                }`}
                style={{
                    minHeight: 38,
                    cursor: disabled ? "not-allowed" : "pointer",
                }}
                onClick={toggleDropdown}
                onKeyDown={event => {
                    if (event.target !== event.currentTarget) return;

                    if (event.key === "Enter" || event.key === " ") {
                        event.preventDefault();
                        toggleDropdown();
                    }
                }}
            >
                <div className="d-flex flex-wrap align-items-center gap-1 flex-grow-1">
                    {selected.length === 0 ? (
                        <span className="text-body-secondary">
                            {placeholder}
                        </span>
                    ) : (
                        selected.map(option => {
                            const optionValue = getOptionValue(option);
                            const optionLabel = getOptionLabel(option);

                            return (
                                <span
                                    key={optionValue}
                                    className="d-inline-flex align-items-center gap-1 rounded px-2 py-1 bg-body-secondary"
                                >
                                    {optionLabel}

                                    <button
                                        type="button"
                                        className="border-0 bg-transparent p-0 text-secondary lh-1"
                                        aria-label={`Retirer ${optionLabel}`}
                                        disabled={disabled}
                                        onClick={event => {
                                            event.stopPropagation();
                                            toggleOption(option);
                                        }}
                                    >
                                        ×
                                    </button>
                                </span>
                            );
                        })
                    )}
                </div>

                <i
                    className={`bi bi-chevron-${open ? "up" : "down"} text-secondary`}
                    aria-hidden="true"
                />
            </div>

            {open && (
                <div
                    id={menuId}
                    className="mt-2 border rounded bg-body text-body p-2 shadow"
                    style={{
                        position: "relative",
                        zIndex: 1050,
                        width: "100%",
                    }}
                >
                    <input
                        ref={searchInput}
                        type="text"
                        className="form-control"
                        placeholder="Rechercher..."
                        value={search}
                        disabled={disabled}
                        onChange={event => setSearch(event.target.value)}
                    />

                    <div className="overflow-y-auto mt-1" style={{ maxHeight: 280 }}>
                        {loading ? (
                            <div className="text-body-secondary text-center py-3">
                                Chargement...
                            </div>
                        ) : error ? (
                            <div className="text-danger text-center py-3">
                                Erreur lors du chargement des options.
                            </div>
                        ) : visible.length === 0 ? (
                            <div className="text-body-secondary text-center py-3">
                                Aucun résultat
                            </div>
                        ) : (
                            visible.map(option => {
                                const optionValue = getOptionValue(option);
                                const optionLabel = getOptionLabel(option);

                                const optionDisabled =
                                    (typeof option === "object" &&
                                    option !== null &&
                                    "disabled" in option &&
                                    Boolean(option.disabled)) ||
                                    (limitReached && !value.includes(optionValue));

                                return (
                                    <label
                                        key={optionValue}
                                        className="d-flex align-items-center gap-2 rounded py-2 px-2 text-body"
                                        style={{
                                            cursor: optionDisabled || disabled
                                                ? "not-allowed"
                                                : "pointer",
                                        }}
                                    >
                                        <input
                                            type="checkbox"
                                            className="form-check-input mt-0"
                                            checked={value.includes(optionValue)}
                                            disabled={disabled || optionDisabled}
                                            onChange={() => toggleOption(option)}
                                        />

                                        {optionLabel}
                                    </label>
                                );
                            })
                        )}

                        {!loading && !error && options.length > visible.length && (
                            <div className="text-body-secondary small px-2 py-2">
                                Affinez la recherche pour voir les autres résultats.
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}
