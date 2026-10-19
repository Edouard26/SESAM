import PageHeader from "../../components/layout/PageHeader.tsx";
import Filters from "../../components/generic/Filters.tsx";
import Table, {type Column} from "../../components/generic/display/Table.tsx";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { materialApi } from "../../api";
import { routes } from "../../router/routes.ts";
import type {Material} from "../../types/Material.ts";
import MaterialPopup from "../../components/material/MaterialPopup.tsx";
import usePaginatedList from "../../hooks/usePaginatedList.ts";
import AssignmentsPopup from "../../components/material/AssignmentsPopup.tsx";
import ImportMaterialsPopup from "../../components/material/ImportMaterialsPopup.tsx";

export default function Materials() {
    const navigate = useNavigate();
    const [showMaterialPopup, setShowMaterialPopup] = useState(false);
    const [showAffectationPopup, setShowAffectationPopup] = useState(false);
    const [showImportPopup, setShowImportPopup] = useState(false);
    const [selectedMaterial, setSelectedMaterial] = useState<Material | undefined>();

    const selects = [
        {
            name: "type",
            label: "Type",
            options: [
                { label: "Tous", value: "" },
                { label: "UC", value: "UC" },
                { label: "Écran", value: "ECRAN" },
                { label: "Vidéo Projecteur", value: "VIDEO_PROJECTEUR" },
            ],
        },
        {
            name: "etat",
            label: "État",
            options: [
                { label: "Tous", value: "" },
                { label: "En Etat", value: "EN_ETAT" },
                { label: "En Maintenance", value: "EN_MAINTENANCE" },
                { label: "Deprécier", value: "DEPRECIER" },
            ],
        },
        {
            name: "salleId",
            label: "Salle",
            options: [
                { label: "Toutes", value: "" },
                { label: "Salle A101", value: "1" },
            ],
        },
    ];

    const initialFilters: Record<string, string> = Object.fromEntries(
        selects.map(({ name }) => [name, ""])
    );

    const {search, filters, items: materials, loading, page, totalPages, totalElements, setPage, setSearch, handleFilterChange, resetFilters, reload,} = usePaginatedList({
        load: materialApi.list,
        initialFilters,
        pageSize: 10,
        errorMessage: "Impossible de charger les matériels",
    });

    const columns = [
        { key: "nom", label: "Nom" },
        { key: "marque", label: "Marque" },
        { key: "modele", label: "Modèle" },
        { key: "numeroSerie", label: "Numero de série" },
        { key: "typeMateriel", label: "Type" },
        {
            label: "Action", render: (material: Material) => (
                <div className="d-flex gap-2">
                    <button
                        type="button"
                        className="btn btn-sm btn-outline-secondary"
                        onClick={() => navigate(`${routes.materials}/${material.id}`)}
                    >
                        Voir
                    </button>
                    <button
                        type="button"
                        className="btn btn-sm btn-outline-primary"
                        onClick={() => {
                            setSelectedMaterial(material);
                            setShowMaterialPopup(true);
                        }}
                    >
                        Modifier
                    </button>
                </div>
            ),
        },
    ] satisfies Column<Material>[];

    return (
        <>
            {showMaterialPopup && (
                <MaterialPopup
                    material={selectedMaterial}
                    onClose={() => setShowMaterialPopup(false)}
                    onSubmit={(material) => {
                        setShowMaterialPopup(false);
                        reload();
                        setSearch(material.nom);
                    }}
                />
            )}
            {showAffectationPopup && (
                <AssignmentsPopup
                    onClose={() => setShowAffectationPopup(false)}
                    onSubmit={() => {
                        setShowAffectationPopup(false);
                        reload();
                    }}
                />
            )}
            {showImportPopup && (
                <ImportMaterialsPopup
                    onClose={() => setShowImportPopup(false)}
                    onSubmit={() => {
                        setShowImportPopup(false);
                        reload();
                    }}
                />
            )}

            <PageHeader
                title="Matériels"
                description="Rechercher et gérer les matériels du centre."
                actions={[
                    {
                        label: "Affecter",
                        icon: "bi-display",
                        onClick: () => setShowAffectationPopup(true),
                    },
                    {
                        label: "Importer",
                        icon: "bi-file-earmark-arrow-down",
                        onClick: () => setShowImportPopup(true),
                    },
                    {
                        label: "Ajouter",
                        icon: "bi-plus-lg",
                        variant: "primary",
                        onClick: () => {
                            setSelectedMaterial(undefined);
                            setShowMaterialPopup(true);
                        }
                    },
                ]}
            />
            <Filters
                embedded
                search={search}
                searchPlaceholder="Nom, numéro de série, marque..."
                selects={selects}
                values={filters}
                onSearchChange={setSearch}
                onFilterChange={handleFilterChange}
                onReset={resetFilters}
            />

            <Table
                columns={columns}
                data={materials}
                currentPage={page}
                totalPages={totalPages}
                totalElements={totalElements}
                onPageChange={setPage}
                loading={loading}
            />
        </>
    );
}