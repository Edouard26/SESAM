import PageHeader from "../../components/layout/PageHeader.tsx";
import Filters from "../../components/generic/Filters.tsx";
import CardTable from "../../components/generic/display/CardTable.tsx";
import {useNavigate} from "react-router-dom";
import {roomApi} from "../../api";
import usePaginatedList from "../../hooks/usePaginatedList.ts";
import RoomPopup from "../../components/room/RoomPopup.tsx";
import {useEffect, useState} from "react";
import type {Building} from "../../types/Building.ts";
import {buildingApi} from "../../api/building.api.ts";
import type {RoomForm} from "../../types/Room.ts";
import AlertPopup from "../../components/alert/AlertPopup.tsx";
import {Role} from "../../types/Role.ts";
import {useAuth} from "../../context/AuthContext.tsx";

export default function Rooms() {
    const { user } = useAuth();
    const navigate = useNavigate();

    // Récupération du rôle numérique depuis l'enum.
    const userRole = user?.role
        ? Number(Role[user.role as Role])
        : -1;

    const isTrainer = user?.role != null && user?.role === Role.GESTIONNAIRE;
    const canManageRooms = userRole >= Role.GESTIONNAIRE;
    const canSendAlert = isTrainer;

    const [showRoomPopup, setShowRoomPopup] = useState(false);
    const [showAlertPopup, setShowAlertPopup] = useState(false);
    const [buildings, setBuildings] = useState<Building[]>([]);

    // Charge les bâtiments uniquement lorsque les filtres sont nécessaires.
    useEffect(() => {
        if (isTrainer) return;

        let cancelled = false;

        const loadBuildings = async () => {
            try {
                const response = await buildingApi.list({
                    page: 0,
                    size: 100,
                    sort: "nom,asc",
                });

                if (!cancelled) {
                    setBuildings(response.content);
                }
            } catch (error) {
                console.error("Impossible de charger les bâtiments", error);
            }
        };

        void loadBuildings();

        return () => {
            cancelled = true;
        };
    }, [isTrainer]);

    const selects = [
        {
            name: "typeSalle",
            label: "Type de salle",
            options: [
                { label: "Tous", value: "" },
                { label: "Cours", value: "COURS" },
                { label: "Stockage", value: "STOCKAGE" },
            ],
        },
        {
            name: "batimentId",
            label: "Bâtiment",
            options: [
                { label: "Tous", value: "" },
                ...buildings.map((building) => ({
                    label: building.nom,
                    value: String(building.id),
                })),
            ],
        },
        {
            name: "etat",
            label: "État",
            options: [
                { label: "Tous", value: "" },
                { label: "Disponible", value: "DISPONIBLE" },
                { label: "Non disponible", value: "INDISPONIBLE" },
                { label: "Maintenance", value: "MAINTENANCE" },
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
        reload,
        totalPages,
        setPage,
        setSearch,
        handleFilterChange,
        resetFilters,
    } = usePaginatedList({
        load: (params) =>
            roomApi.list({
                ...params,
                ...(isTrainer ? { page: 0, size: 1 } : {}),
            }),
        initialFilters,
        pageSize: isTrainer ? 1 : 9,
        loadKey: isTrainer ? "trainer" : "all",
        errorMessage: "Impossible de charger les salles",
    });

    // Sécurité supplémentaire côté affichage.
    // Un formateur ne reçoit jamais plus d'une salle dans CardTable.
    const displayedRooms = isTrainer ? rooms.slice(0, 1) : rooms;

    return (
        <>
            {showRoomPopup && canManageRooms && (
                <RoomPopup
                    onClose={() => setShowRoomPopup(false)}
                    onSubmit={(room: RoomForm) => {
                        setShowRoomPopup(false);
                        reload();
                        setSearch(room.nom);
                    }}
                />
            )}

            {showAlertPopup && canSendAlert && user && (
                <AlertPopup
                    user={user}
                    onClose={() => setShowAlertPopup(false)}
                    onSubmit={() => setShowAlertPopup(false)}
                />
            )}

            <PageHeader
                title="Salles"
                description="Retrouvez les équipements et la disponibilité des salles."
                actions={[
                    ...(canManageRooms
                        ? [
                            {
                                label: "Ajouter une salle",
                                icon: "bi-plus-lg",
                                variant: "primary" as const,
                                onClick: () => setShowRoomPopup(true),
                            },
                        ]
                        : []),
                    ...(canSendAlert
                        ? [
                            {
                                label: "Envoyer une alerte",
                                icon: "bi-exclamation-triangle",
                                onClick: () => setShowAlertPopup(true),
                            },
                        ]
                        : []),
                ]}
            />

            {!isTrainer && (
                <Filters
                    search={search}
                    searchPlaceholder="Nom de salle, bâtiment..."
                    selects={selects}
                    values={filters}
                    onSearchChange={setSearch}
                    onFilterChange={handleFilterChange}
                    onReset={resetFilters}
                />
            )}

            <CardTable
                data={displayedRooms}
                loading={loading}
                pagination={{
                    currentPage: page,
                    totalPages: isTrainer ? 1 : totalPages,
                    onPageChange: setPage,
                }}
                card={{
                    icon: (room) => (
                        <i
                            className={`bi ${
                                room.typeSalle === "COURS"
                                    ? "bi-display"
                                    : "bi-box-seam"
                            } fs-5`}
                            aria-hidden="true"
                        />
                    ),
                    title: (room) => "Salle " + room.nom,
                    subtitle: (room) =>
                        `Batiment ${room.nomBatiment ?? "-"}`,
                    status: (room) => {
                        const state = room.etat.toUpperCase();

                        switch (state) {
                            case "DISPONIBLE":
                                return {
                                    label: "Disponible",
                                    variant: "success",
                                };

                            case "MAINTENANCE":
                                return {
                                    label: "Réinstallation en cours",
                                    variant: "warning",
                                };

                            default:
                                return {
                                    label: "Non disponible",
                                    variant: "danger",
                                };
                        }
                    },
                    footer: (room) => {
                        const nbPlaces = room.nbPlaces ?? 0;
                        const nbMaterielDispo = room.nbMaterielDispo ?? 0;

                        const tauxOccupation =
                            nbPlaces > 0
                                ? Math.round(
                                    (nbMaterielDispo / nbPlaces) * 100,
                                )
                                : 0;

                        return (
                            <div className="d-flex align-items-center justify-content-between gap-3 small">
                                <div>
                                    <span className="text-secondary">
                                        {room.nbMaterielDispo ?? "x"} postes ·{" "}
                                        {room.nbPlaces ?? "-"} places

                                        {(room.nbAlertes ?? 0) > 0 && (
                                            <>
                                                {" · "}
                                                <span className="badge bg-danger rounded-pill">
                                                    <i className="bi bi-exclamation-triangle-fill me-1" />
                                                    {room.nbAlertes} alertes
                                                </span>
                                            </>
                                        )}
                                    </span>

                                    <div className="text-secondary mt-1">
                                        Taux d'occupation :{" "}
                                        <span className="fw-semibold text-body">
                                            {tauxOccupation} %
                                        </span>
                                    </div>
                                </div>

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
                        );
                    },
                }}
            />
        </>
    );
}