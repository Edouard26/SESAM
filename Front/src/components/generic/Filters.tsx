type FilterOption = {
    label: string;
    value: string;
};

type SelectFilter = {
    name: string;
    label: string;
    options: FilterOption[];
};

type FiltersProps = {
    search?: string;
    searchLabel?: string;
    searchPlaceholder?: string;
    selects?: SelectFilter[];
    values: Record<string, string>;
    onSearchChange?: (value: string) => void;
    onFilterChange: (name: string, value: string) => void;
    onReset: () => void;
    embedded?: boolean;
};

export default function Filters({
    search = "",
    searchLabel = "Recherche",
    searchPlaceholder = "Rechercher...",
    selects = [],
    values,
    onSearchChange,
    onFilterChange,
    onReset,
    embedded = false,
}: FiltersProps) {
    return (
        <div className={embedded ? "p-3 border overflow-hidden" : "card border mb-4"}>
            <div className={embedded ? "" : "card-body"}>
                <div className="row g-3 align-items-end">
                    {onSearchChange && (
                        <div className="col-12 col-lg">
                            <label className="form-label">
                                {searchLabel}
                            </label>
                            <input
                                type="text"
                                className="form-control"
                                placeholder={searchPlaceholder}
                                value={search}
                                onChange={(event) =>
                                    onSearchChange(event.target.value)
                                }
                            />
                        </div>
                    )}

                    {selects.map((filter) => (
                        <div
                            key={filter.name}
                            className="col-12 col-sm-6 col-lg"
                        >
                            <label
                                htmlFor={filter.name}
                                className="form-label"
                            >
                                {filter.label}
                            </label>

                            <select
                                id={filter.name}
                                className="form-select"
                                value={values[filter.name] ?? ""}
                                onChange={(event) =>
                                    onFilterChange(
                                        filter.name,
                                        event.target.value
                                    )
                                }
                            >
                                {filter.options.map((option) => (
                                    <option
                                        key={option.value}
                                        value={option.value}
                                    >
                                        {option.label}
                                    </option>
                                ))}
                            </select>
                        </div>
                    ))}

                    <div className="col-12 col-lg-auto">
                        <button
                            type="button"
                            className="btn btn-outline-primary w-100"
                            onClick={onReset}
                        >
                            <i className="bi bi-arrow-clockwise me-2" />
                            Réinitialiser
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}