import PageHeader from "../../components/layout/PageHeader.tsx";
import Filters from "../../components/generic/Filters.tsx";
import Table, {type Column} from "../../components/generic/display/Table.tsx";
import { useState } from "react";
import { materialApi } from "../../api";
import type {Material} from "../../types/Material.ts";
import MaterialPopup from "../../components/material/MaterialPopup.tsx";
import usePaginatedList from "../../hooks/usePaginatedList.ts";

export default function Materials() {
    const [showPopup, setShowPopup] = useState(false);
    const [selectedMaterial, setSelectedMaterial] = useState<Material | undefined>();

    const selects = [
        {
            name: "type",
            label: "Type",
            options: [
                { label: "Tous", value: "" },
                { label: "UC", value: "UC" },
                { label: "Écran", value: "ECRAN" },
                { label: "Vidéoprojecteur", value: "PROJECTEUR" },
            ],
        },
        {
            name: "state",
            label: "État",
            options: [
                { label: "Tous", value: "" },
                { label: "Disponible", value: "DISPONIBLE" },
                { label: "En panne", value: "PANNE" },
            ],
        },
        {
            name: "room",
            label: "Salle",
            options: [
                { label: "Toutes", value: "" },
                { label: "Salle A101", value: "A101" },
            ],
        },
        {
            name: "location",
            label: "Localisation",
            options: [
                { label: "Toutes", value: "" },
                { label: "Bâtiment A", value: "A" },
            ],
        },
    ];

    const initialFilters: Record<string, string> = Object.fromEntries(
        selects.map(({ name }) => [name, ""])
    );

    const {search, filters, items: materials, loading, page, totalPages, setPage, setSearch, handleFilterChange, resetFilters, reload,} = usePaginatedList({
        load: materialApi.list,
        initialFilters,
        pageSize: 10,
        errorMessage: "Impossible de charger les matériels",
    });

    const columns = [
        { key: "nom", label: "Nom" },
        { key: "marque", label: "Marque" },
        { key: "modele", label: "Modèle" },
        { key: "typeMateriel", label: "Type" },
        {
            label: "Action", render: (material: Material) => (
                <button
                    type="button"
                    className="btn btn-sm btn-outline-primary"
                    onClick={() => {
                        setSelectedMaterial(material);
                        setShowPopup(true);
                    }}
                >
                    Modifier
                </button>
            ),
        },
    ] satisfies Column<Material>[];

    return (
        <>
            <MaterialPopup
                open={showPopup}
                material={selectedMaterial}
                onClose={() => setShowPopup(false)}
                onSubmit={(material) => {
                    setShowPopup(false);
                    reload();
                    setSearch(material.nom);
                }}
            />
            <PageHeader
                title="Matériels"
                description="Rechercher et gérer les matériels du centre."
                actions={[
                    {
                        label: "Affecter du matériel",
                        icon: "bi-display",
                        onClick: () => console.log("Affecter"),
                    },
                    {
                        label: "Ajouter un matériel",
                        icon: "bi-plus-lg",
                        variant: "primary",
                        onClick: () => {
                            setSelectedMaterial(undefined);
                            setShowPopup(true);
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
                onPageChange={setPage}
                loading={loading}
            />
        </>
    );
}