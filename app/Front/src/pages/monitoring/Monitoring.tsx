import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { roomApi } from "../../api";
import PageHeader from "../../components/layout/PageHeader.tsx";
import Filters from "../../components/generic/Filters.tsx";
import CardTable from "../../components/generic/display/CardTable.tsx";
import usePaginatedList from "../../hooks/usePaginatedList.ts";
import type { Room } from "../../types/Room.ts";
import type { Material, MaterialType } from "../../types/Material.ts";
import { routes } from "../../router/routes.ts";

const PAGE_SIZE = 9;

const MATERIAL_ICONS: Record<MaterialType, string> = {
    UC: "bi-pc-display",
    ECRAN: "bi-display",
    VIDEO_PROJECTEUR: "bi-projector",
};

const MATERIAL_LABELS: Record<MaterialType, string> = {
    UC: "Unité centrale",
    ECRAN: "Écran",
    VIDEO_PROJECTEUR: "Vidéoprojecteur",
};

const selects = [
    {
        name: "type",
        label: "Type de matériel",
        options: [
            { label: "Tous", value: "" },
            { label: "Unité centrale", value: "UC" },
            { label: "Écran", value: "ECRAN" },
            { label: "Vidéoprojecteur", value: "VIDEO_PROJECTEUR" },
        ],
    },
];

const initialFilters: Record<string, string> = {
    type: "",
};

export default function Monitoring() {
    const navigate = useNavigate();

    const {
        items: rooms,
        loading: roomsLoading,
    } = usePaginatedList({
        load: roomApi.list,
        initialFilters: {},
        pageSize: 1000,
        errorMessage: "Impossible de charger les salles",
    });

    const [selectedRoomId, setSelectedRoomId] = useState("");
    const [selectedRoom, setSelectedRoom] = useState<Room | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const [search, setSearch] = useState("");
    const [filters, setFilters] = useState(initialFilters);
    const [page, setPage] = useState(0);

    useEffect(() => {
        if (!selectedRoomId) {
            setSelectedRoom(null);
            setLoading(false);
            setError(null);
            return;
        }

        let cancelled = false;

        setLoading(true);
        setError(null);
        setSelectedRoom(null);

        roomApi.get(Number(selectedRoomId))
            .then((room) => {
                if (!cancelled) {
                    setSelectedRoom(room);
                    setPage(0);
                }
            })
            .catch(() => {
                if (!cancelled) {
                    setError("Impossible de charger les matériels de cette salle");
                }
            })
            .finally(() => {
                if (!cancelled) {
                    setLoading(false);
                }
            });

        return () => {
            cancelled = true;
        };
    }, [selectedRoomId]);

    const allMaterials = selectedRoom?.materiels ?? [];

    const filteredMaterials = useMemo(() => {
        const normalizedSearch = search.trim().toLocaleLowerCase("fr");

        return allMaterials.filter((material) => {
            if (
                filters.type &&
                material.typeMateriel !== filters.type
            ) {
                return false;
            }

            if (!normalizedSearch) {
                return true;
            }

            return [
                material.nom,
                material.numeroSerie,
                material.marque,
                material.modele,
                MATERIAL_LABELS[material.typeMateriel],
            ].some((value) =>
                (value ?? "")
                    .toLocaleLowerCase("fr")
                    .includes(normalizedSearch),
            );
        });
    }, [allMaterials, search, filters.type]);

    const totalPages = Math.ceil(filteredMaterials.length / PAGE_SIZE);

    const materials = filteredMaterials.slice(
        page * PAGE_SIZE,
        (page + 1) * PAGE_SIZE,
    );

    function handleSearchChange(value: string) {
        setSearch(value);
        setPage(0);
    }

    function handleFilterChange(name: string, value: string) {
        setFilters((current) => ({
            ...current,
            [name]: value,
        }));
        setPage(0);
    }

    function resetFilters() {
        setSearch("");
        setFilters(initialFilters);
        setPage(0);
    }

    function handleRoomChange(roomId: string) {
        setSelectedRoomId(roomId);
        setSearch("");
        setFilters(initialFilters);
        setPage(0);
    }

    return (
        <>
            <PageHeader
                title="Supervision"
                description="Choisir une salle pour consulter et superviser ses matériels."
            />

            <div className="mb-4">
                <label
                    htmlFor="monitoring-room"
                    className="form-label"
                >
                    Salle
                </label>

                <select
                    id="monitoring-room"
                    className="form-select"
                    value={selectedRoomId}
                    disabled={roomsLoading}
                    onChange={(event) => handleRoomChange(event.target.value)}
                >
                    <option value="">
                        {roomsLoading
                            ? "Chargement des salles..."
                            : "Sélectionner une salle"}
                    </option>

                    {rooms.map((room) => (
                        <option key={room.id} value={room.id}>
                            Salle {room.nom} · Bâtiment {room.nomBatiment ?? "-"}
                        </option>
                    ))}
                </select>
            </div>

            {!selectedRoomId && (
                <div className="text-center text-secondary py-5">
                    Sélectionnez une salle pour afficher ses matériels.
                </div>
            )}

            {selectedRoomId && (
                <>
                    {selectedRoom && (
                        <h2 className="h5 mb-3">
                            Matériels de la salle {selectedRoom.nom}{" "}
                            <span className="badge text-bg-light align-middle">
                                {filteredMaterials.length}
                            </span>
                        </h2>
                    )}

                    <Filters
                        search={search}
                        searchPlaceholder="Nom, marque, modèle, numéro de série..."
                        selects={selects}
                        values={filters}
                        onSearchChange={handleSearchChange}
                        onFilterChange={handleFilterChange}
                        onReset={resetFilters}
                    />

                    {error ? (
                        <div className="alert alert-danger mt-3" role="alert">
                            {error}
                        </div>
                    ) : (
                        <CardTable
                            data={materials}
                            loading={loading}
                            pagination={{
                                currentPage: page,
                                totalPages,
                                onPageChange: setPage,
                            }}
                            card={{
                                icon: (material: Material) => (
                                    <i
                                        className={`bi ${
                                            MATERIAL_ICONS[material.typeMateriel] ??
                                            "bi-box-seam"
                                        } fs-5`}
                                        aria-hidden="true"
                                    />
                                ),
                                title: (material: Material) => material.nom,
                                subtitle: (material: Material) =>
                                    `${MATERIAL_LABELS[material.typeMateriel] ?? material.typeMateriel} · ${material.marque} ${material.modele} · N° série : ${material.numeroSerie || "-"}`,
                                footer: (material: Material) => (
                                    <div className="d-flex align-items-center justify-content-between gap-3 small">
                                        <button type="button" className="btn btn-link p-0 text-decoration-none text-nowrap"
                                            onClick={() =>
                                                navigate(`${routes.materials}/${material.id}`)
                                            }
                                        >
                                            Consulter
                                        </button>
                                        {material.typeMateriel === "UC" && (
                                            <button type="button" className="btn btn-link p-0 text-decoration-none text-nowrap"
                                                onClick={() => navigate(`/process/${material.id}`)}
                                            >
                                                Processus
                                            </button>
                                        )}
                                    </div>
                                ),
                            }}
                        />
                    )}
                </>
            )}
        </>
    );
}