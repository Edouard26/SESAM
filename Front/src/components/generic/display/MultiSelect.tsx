import { useEffect, useId, useRef, useState } from "react";

type MultiSelectProps<TOption, TValue extends string | number> = {
    searchOptions: (query: string) => Promise<{ content: TOption[] }>;
    getOptionValue: (option: TOption) => TValue;
    getOptionLabel: (option: TOption) => string;
    value: TValue[];
    onChange: (value: TValue[]) => void;
    maxOptions?: number;
    disabled?: boolean;
};

export default function MultiSelect<TOption, TValue extends string | number>({searchOptions, getOptionValue, getOptionLabel, value, onChange, disabled = false, maxOptions = 10 }: MultiSelectProps<TOption, TValue>) {
    const [open, setOpen] = useState(false);
    const [search, setSearch] = useState("");
    const [options, setOptions] = useState<TOption[]>([]);
    const [optionCache, setOptionCache] = useState<Map<TValue, TOption>>(new Map());
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(false);
    const root = useRef<HTMLDivElement>(null);
    const searchOptionsRef = useRef(searchOptions);
    const getOptionValueRef = useRef(getOptionValue);
    const menuId = useId();

    useEffect(() => {
        searchOptionsRef.current = searchOptions;
        getOptionValueRef.current = getOptionValue;
    }, [searchOptions, getOptionValue]);

    useEffect(() => {
        if (!open) return;

        let active = true;

        setLoading(true);
        setError(false);
        setOptions([]);

        Promise.resolve()
            .then(() => searchOptionsRef.current(search))
            .then(result => {
                if (!active) return;

                setOptions(result.content);
                setOptionCache(previous => {
                    const next = new Map(previous);

                    result.content.forEach(option => {
                        next.set(getOptionValueRef.current(option), option);
                    });

                    return next;
                });
            })
            .catch(() => {
                if (!active) return;

                setOptions([]);
                setError(true);
            })
            .finally(() => {
                if (active) setLoading(false);
            });

        return () => {
            active = false;
        };
    }, [open, search]);

    const selected = value.map(optionValue => optionCache.get(optionValue)).filter((option): option is TOption => option !== undefined);
    const visible = options.slice(0, Math.max(1, Math.floor(maxOptions)));

    function toggleOption(option: TOption) {
        const optionValue = getOptionValue(option);

        setOptionCache(previous => {
            const next = new Map(previous);
            next.set(optionValue, option);
            return next;
        });

        onChange(
            value.includes(optionValue)
                ? value.filter(item => item !== optionValue)
                : [...value, optionValue]
        );
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
            <div role="button" tabIndex={disabled ? -1 : 0} aria-expanded={open} aria-controls={menuId} aria-disabled={disabled}
                className={`border rounded p-2 d-flex align-items-center gap-2 ${ disabled ? "bg-body-tertiary" : ""}`}
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
                            Sélectionner des éléments
                        </span>
                    ) : (
                        selected.map(option => {
                            const optionValue = getOptionValue(option);
                            const optionLabel = getOptionLabel(option);

                            return (
                                <span key={optionValue} className="d-inline-flex align-items-center gap-1 rounded px-2 py-1 bg-body-secondary">
                                    {optionLabel}
                                    <button type="button" className="border-0 bg-transparent p-0 text-secondary lh-1" aria-label={`Retirer ${optionLabel}`}
                                        disabled={disabled}
                                        onClick={event => {
                                            event.stopPropagation();
                                            toggleOption(option);
                                        }}
                                    >*</button>
                                </span>
                            );
                        })
                    )}
                </div>

                <i className={`bi bi-chevron-${open ? "up" : "down"} text-secondary`} aria-hidden="true"/>
            </div>
            {open && (
                <div id={menuId} className="position-absolute top-100 start-0 w-100 mt-1 border rounded bg-white p-2" style={{ zIndex: 1050 }}>
                    <input type="text" placeholder="Rechercher..." value={search} disabled={disabled}
                        onChange={event => setSearch(event.target.value)}
                        onKeyDown={event => {
                            if (event.key === "Escape") {
                                setOpen(false);
                                setSearch("");
                            }
                        }}
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
                                    typeof option === "object" &&
                                    option !== null &&
                                    "disabled" in option &&
                                    Boolean(option.disabled);

                                return (
                                    <label key={optionValue} className="dropdown-item d-flex align-items-center gap-2 rounded py-2">
                                        <input type="checkbox" className="form-check-input mt-0"
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