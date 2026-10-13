import PageHeader from "../../components/layout/PageHeader.tsx";
import Filters from "../../components/generic/Filters.tsx";
import Table, {type Column} from "../../components/generic/display/Table.tsx";
import { useEffect, useState } from "react";
import {ApiError, materialApi} from "../../api";
import type {Material} from "../../types/Material.ts";
import useNotification from "../../hooks/useNotification.ts";

export default function Materials() {
    const { notify } = useNotification();
    const initialFilters = {
        type: "",
        state: "",
        room: "",
        location: "",
    };

    const [search, setSearch] = useState("");
    const [filters, setFilters] = useState(initialFilters);
    const [loading, setLoading] = useState(false);

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

    function handleFilterChange(name: string, value: string) {
        setFilters((previous) => ({
            ...previous,
            [name]: value,
        }));
        setPage(0);
    }

    function resetFilters() {
        setSearch("");
        setFilters(initialFilters);
        setPage(0);
    }


    const [materials, setMaterials] = useState<Material[]>([
        {
            name: "Poste A101-01",
            type: "UC",
            state: "DISPONIBLE",
            room: "A101",
        } as Material,
        {
            name: "Écran A101-01",
            type: "ECRAN",
            state: "PANNE",
            room: "A101",
        } as Material,
    ]);

    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const columns = [
        { key: "name", label: "Nom" },
        { key: "type", label: "Type" },
        { key: "state", label: "État" },
        { key: "room", label: "Salle" },
        {
            label: "Action", render: (material: Material) => (
                <button
                    type="button"
                    className="btn btn-sm btn-outline-primary"
                    onClick={() => console.log("Modifier", material)}
                >
                    Modifier
                </button>
            ),
        },
    ] satisfies Column<Material>[];

    useEffect(() => {
        /*setLoading(true);
        materialApi.list({ search, ...filters, page, size: 10 }).then(data=> {
            setMaterials(data.content);
            setTotalPages(data.totalPages);
        }).catch(() => {
            notify("Impossible de charger les matériels", "danger", 10000);
            setMaterials([]);
            setTotalPages(0);
        }).finally(() => setLoading(false));*/
    }, [search, filters, page]);

    return (
        <>
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
                        onClick: () => console.log("Ajouter"),
                    },
                ]}
            />
            <Filters
                embedded
                search={search}
                searchPlaceholder="Nom, numéro de série, marque..."
                selects={selects}
                values={filters}
                onSearchChange={(value) => {
                    setSearch(value);
                    setPage(0);
                }}
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