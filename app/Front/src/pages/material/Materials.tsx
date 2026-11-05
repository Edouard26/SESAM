import PageHeader from "../../components/layout/PageHeader.tsx";
import Filters, {type FilterField} from "../../components/generic/Filters.tsx";
import Table, {type Column} from "../../components/generic/display/Table.tsx";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {materialApi, roomApi} from "../../api";
import { routes } from "../../router/routes.ts";
import type {Material} from "../../types/Material.ts";
import MaterialPopup from "../../components/material/MaterialPopup.tsx";
import usePaginatedList from "../../hooks/usePaginatedList.ts";
import AssignmentsPopup from "../../components/material/AssignmentsPopup.tsx";
import ImportMaterialsPopup from "../../components/material/ImportMaterialsPopup.tsx";
import AssignmentsUserPopup from "../../components/material/AssignmentsUserPopup.tsx";

async function searchRooms(query: string) {
    const rooms = await roomApi.list({ search: query, page: 0, size: 20 });
    return {
        content: rooms.content.map((room) => ({ label: room.nom, value: String(room.id) })),
    };
}

function loadMaterials(params?: Parameters<typeof materialApi.list>[0]) {
    return materialApi.list(params?.salleId ? { ...params, excludeStagiaire: false } : params);
}

const materialStateLabels: Record<string, string> = {
    EN_ETAT: "En état",
    EN_MAINTENANCE: "En maintenance",
    DEPRECIER: "Déprécié",
};

const materialTypeLabels: Record<string, string> = {
    UC: "UC",
    ECRAN: "Écran",
    VIDEO_PROJECTEUR: "Vidéo Projecteur",
};

// Liste paginée des matériels (recherche, filtres, tri) avec les popups d'ajout/modification, d'affectation et d'import
export default function Materials() {
    const navigate = useNavigate();
    const [showMaterialPopup, setShowMaterialPopup] = useState(false);
    const [showAffectationPopup, setShowAffectationPopup] = useState(false);
    const [showAffectationStagiairePopup, setShowAffectationStagiairePopup] = useState(false);
    const [showImportPopup, setShowImportPopup] = useState(false);
    const [selectedMaterial, setSelectedMaterial] = useState<Material | undefined>();

    const selects: FilterField[] = [
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
            type: "multiselect",
            name: "salleId",
            label: "Salle",
            searchOptions: searchRooms,
            maxSelected: 1,
            placeholder: "Toutes",
        },
    ];

    const initialFilters: Record<string, string> = Object.fromEntries(
        selects.map(({ name }) => [name, ""])
    );

    const {search, filters, items: materials, loading, page, totalPages, totalElements, sort, setSort, setPage, setSearch, handleFilterChange, resetFilters, reload,} = usePaginatedList({
        load: loadMaterials,
        initialFilters,
        pageSize: 10,
        errorMessage: "Impossible de charger les matériels",
        initialSort: "dateFinGarantie,asc",
    });

    const columns = [
        { key: "nom", label: "Nom", sortKey: "nom" },
        { key: "marque", label: "Marque", sortKey: "marque" },
        { key: "modele", label: "Modèle", sortKey: "modele" },
        {
            key: "etat",
            label: "État",
            sortKey: "etat",
            render: (material: Material) => (
                <span>{materialStateLabels[material.etat] ?? material.etat}</span>
            ),
        },
        { key: "numeroSerie", label: "Numero de série", sortKey: "numeroSerie" },
        {
            key: "typeMateriel",
            label: "Type",
            sortKey: "typeMateriel",
            render: (material: Material) => (
                <span>{materialTypeLabels[material.typeMateriel] ?? material.typeMateriel}</span>
            ),
        },
        {
            key: "dateFinGarantie",
            label: "Fin garantie",
            sortKey: "dateFinGarantie",
            // Rouge si la garantie est expirée, orange si elle expire dans moins d'un an
            render: (material: Material) => {
                const date = new Date(material.dateFinGarantie);
                const days = (date.getTime() - Date.now()) / 86400000;
                return (<span className={days < 0 ? "fw-bold text-danger" : days <= 365 ? "fw-bold text-warning" : ""}>{date.toLocaleDateString("fr-FR")}</span>);
            },
        },
        {
            key: "dateAjout",
            label: "Ajout",
            sortKey: "dateAjout",
            // Orange si le matériel a 5 ans ou plus
            render: (material: Material) => {
                const date = new Date(material.dateAjout);
                const years = (Date.now() - date.getTime()) / (365.25 * 86400000);
                return (<span className={years >= 5 ? "fw-bold text-warning" : ""}>{date.toLocaleDateString("fr-FR")}</span>);
            },
        },
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
                    // Après création/modification, on recharge et on filtre sur le matériel concerné
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
            {showAffectationStagiairePopup && (
                <AssignmentsUserPopup
                    onClose={() => setShowAffectationStagiairePopup(false)}
                    onSubmit={() => {
                        setShowAffectationStagiairePopup(false);
                        reload();
                    }}
                />
            )}

            <PageHeader
                title="Matériels"
                description="Rechercher et gérer les matériels du centre."
                actions={[
                    {
                        label: "Affecter à une salle",
                        icon: "bi-display",
                        onClick: () => setShowAffectationPopup(true),
                    },
                    {
                        label: "Affecter à un stagiaire",
                        icon: "bi-people ",
                        onClick: () => setShowAffectationStagiairePopup(true),
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
                sort={sort}
                onSortChange={setSort}
            />
        </>
    );
}
