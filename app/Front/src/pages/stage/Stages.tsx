import { useNavigate } from "react-router-dom";
import PageHeader from "../../components/layout/PageHeader.tsx";
import Filters from "../../components/generic/Filters.tsx";
import Table, { type Column } from "../../components/generic/display/Table.tsx";
import usePaginatedList from "../../hooks/usePaginatedList.ts";
import { stageApi } from "../../api";
import { routes } from "../../router/routes.ts";
import type { Stage, StageStatut } from "../../types/Stage.ts";

const statutLabels: Record<StageStatut, string> = {
    A_AFFECTER: "À affecter",
    AFFECTER: "Affecté",
    EN_COURS: "En cours",
    CLOTURER: "Clôturé",
};

export default function Stages() {
    const navigate = useNavigate();

    const selects = [
        {
            name: "statut",
            label: "Statut",
            options: [
                { label: "Tous", value: "" },
                { label: "À affecter", value: "A_AFFECTER" },
                { label: "Affecté", value: "AFFECTER" },
                { label: "En cours", value: "EN_COURS" },
                { label: "Clôturé", value: "CLOTURER" },
            ],
        },
    ];

    const initialFilters: Record<string, string> = Object.fromEntries(
        selects.map(({ name }) => [name, ""])
    );

    const {
        search,
        filters,
        items: stages,
        loading,
        page,
        totalPages,
        totalElements,
        setPage,
        setSearch,
        handleFilterChange,
        resetFilters,
    } = usePaginatedList({
        load: stageApi.list,
        initialFilters,
        pageSize: 10,
        errorMessage: "Impossible de charger les stages",
    });

    const columns = [
        {
            key: "code",
            label: "Code",
        },
        {
            label: "Date de début",
            render: (stage: Stage) =>
                new Date(stage.dateDebut).toLocaleDateString("fr-FR"),
        },
        {
            label: "Date de fin",
            render: (stage: Stage) =>
                new Date(stage.dateFin).toLocaleDateString("fr-FR"),
        },
        {
            label: "Statut",
            render: (stage: Stage) => statutLabels[stage.statut],
        },
        {
            label: "Salle",
            render: (stage: Stage) => stage.salleId,
        },
        {
            label: "Stagiaires",
            render: (stage: Stage) => stage.stagiairesId.length,
        },
        {
            label: "Action",
            render: (stage: Stage) => (
                <button
                    type="button"
                    className="btn btn-sm btn-outline-secondary"
                    onClick={() =>
                        navigate(`${routes.stages}/${stage.id}`)
                    }
                >
                    Voir
                </button>
            ),
        },
    ] satisfies Column<Stage>[];

    return (
        <>
            <PageHeader
                title="Stages"
                description="Stages chargés depuis SAGAIE et formations du centre."
            />

            <Filters
                embedded
                search={search}
                searchPlaceholder="Code du stage..."
                selects={selects}
                values={filters}
                onSearchChange={setSearch}
                onFilterChange={handleFilterChange}
                onReset={resetFilters}
            />

            <Table
                columns={columns}
                data={stages}
                currentPage={page}
                totalPages={totalPages}
                totalElements={totalElements}
                onPageChange={setPage}
                loading={loading}
            />
        </>
    );
}