import PageHeader from "../../components/layout/PageHeader.tsx";
import Filters from "../../components/generic/Filters.tsx";
import CardTable from "../../components/generic/display/CardTable.tsx";
import { useNavigate } from "react-router-dom";
import { roomApi } from "../../api";
import usePaginatedList from "../../hooks/usePaginatedList.ts";

export default function Rooms() {
    const navigate = useNavigate();

    const selects = [
        {
            name: "type",
            label: "Type de salle",
            options: [
                { label: "Tous", value: "" },
                { label: "Cours", value: "COURS" },
                { label: "Stockage", value: "STOCKAGE" },
            ],
        },
        {
            name: "location",
            label: "Bâtiment",
            options: [
                { label: "Tous", value: "" },
                { label: "Bâtiment A", value: "A" },
            ],
        },
        {
            name: "state",
            label: "État",
            options: [
                { label: "Tous", value: "" },
                { label: "Disponible", value: "DISPONIBLE" },
                { label: "Non disponible", value: "NON_DISPONIBLE" },
                { label: "Réinstallation en cours", value: "REINSTALLATION_EN_COURS",},
            ],
        },
    ];

    const initialFilters: Record<string, string> = Object.fromEntries(
        selects.map(({ name }) => [name, ""]),
    );

    const {
        search,
        filters,
        items: rooms,
        loading,
        page,
        totalPages,
        setPage,
        setSearch,
        handleFilterChange,
        resetFilters,
    } = usePaginatedList({
        load: roomApi.list,
        initialFilters,
        pageSize: 9,
        errorMessage: "Impossible de charger les salles",
    });

    return (
        <>
            <PageHeader
                title="Salles"
                description="Retrouvez les équipements et la disponibilité des salles."
                actions={[
                    {
                        label: "Ajouter une salle",
                        icon: "bi-plus-lg",
                        variant: "primary",
                        onClick: () => console.log("Ajouter"),
                    },
                ]}
            />

            <Filters
                search={search}
                searchPlaceholder="Nom de salle, bâtiment..."
                selects={selects}
                values={filters}
                onSearchChange={setSearch}
                onFilterChange={handleFilterChange}
                onReset={resetFilters}
            />

            <CardTable
                data={rooms}
                loading={loading}
                pagination={{
                    currentPage: page,
                    totalPages,
                    onPageChange: setPage,
                }}
                card={{
                    icon: (room) => (
                        <i className={`bi ${ room.typeSalle === "COURS" ? "bi-display" : "bi-box-seam"} fs-5`} aria-hidden="true"/>
                    ),
                    title: (room) => "Salle " + room.nom,
                    subtitle: (room) =>
                        `Batiment ${room.nomBatiment ?? "-"}`,
                    status: (room) => {
                        const state = room.etat.toUpperCase();

                        switch (state) {
                            case "DISPONIBLE": {
                                return {
                                    label: "Disponible",
                                    variant: "success",
                                };
                            }
                            case "MAINTENANCE": {
                                return {
                                    label: "Réinstallation en cours",
                                    variant: "warning",
                                };
                            }
                            default: {
                                return {
                                    label: "Non disponible",
                                    variant: "danger",
                                };
                            }
                        }
                    },
                    footer: (room) => (
                        <div className="d-flex align-items-center justify-content-between gap-3 small">
                            <span className="text-secondary">
                                {room.nbMaterielDispo ?? "x"} postes ·{" "}
                                {room.nbPlaces ?? "-"} places
                            </span>

                            <button
                                type="button"
                                className="btn btn-link p-0 text-decoration-none text-nowrap"
                                onClick={() =>
                                    navigate(`/rooms/${room.id}`)
                                }
                            >
                                Consulter
                            </button>
                        </div>
                    ),
                }}
            />
        </>
    );
}