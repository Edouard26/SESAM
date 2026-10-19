import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { roomApi } from "../../api";
import type { Room } from "../../types/Room.ts";
import type { Material, MaterialType } from "../../types/Material.ts";
import PageHeader from "../../components/layout/PageHeader.tsx";
import CardTable from "../../components/generic/display/CardTable.tsx";
import Loader from "../../components/generic/Loader.tsx";
import AssignmentsPopup from "../../components/material/AssignmentsPopup.tsx";
import { routes } from "../../router/routes.ts";

const MATERIELS_PAGE_SIZE = 6;

type Badge = { label: string; className: string };

const MATERIAL_ICONS: Record<MaterialType, string> = {
    UniteCentral: "bi-pc-display",
    Ecran: "bi-display",
    VideoProjecteur: "bi-projector",
};

const MATERIAL_LABELS: Record<MaterialType, string> = {
    UniteCentral: "Unité centrale",
    Ecran: "Écran",
    VideoProjecteur: "Vidéoprojecteur",
};

// Même logique que dans la liste des salles (Rooms.tsx).
// TODO: à extraire dans un utilitaire partagé (ex: utils/roomState.ts)
function getStatus(etat: string | undefined): Badge {
    switch ((etat ?? "").toUpperCase()) {
        case "DISPONIBLE":
            return { label: "Disponible", className: "text-bg-success" };
        case "MAINTENANCE":
            return { label: "Réinstallation en cours", className: "text-bg-warning" };
        default:
            return { label: "Non disponible", className: "text-bg-danger" };
    }
}

function StatCard({ label, value }: { label: string; value: string | number }) {
    return (
        <div className="col-12 col-md-6 col-xl-3">
            <div className="card h-100 shadow-sm border-0">
                <div className="card-body">
                    <div className="small text-secondary mb-2">{label}</div>
                    <div className="fs-2 fw-bold">{value}</div>
                </div>
            </div>
        </div>
    );
}

export default function Room() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const roomId = Number(id);

    const [room, setRoom] = useState<Room | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [showAffectationPopup, setShowAffectationPopup] = useState(false);
    const [reloadKey, setReloadKey] = useState(0);

    const [materielsPage, setMaterielsPage] = useState(0);

    const allMateriels = room?.materiels ?? [];
    const totalElements = allMateriels.length;
    const totalPages = Math.ceil(totalElements / MATERIELS_PAGE_SIZE);

    const materiels = allMateriels.slice(
        materielsPage * MATERIELS_PAGE_SIZE,
        (materielsPage + 1) * MATERIELS_PAGE_SIZE
    );

    useEffect(() => {
        if (!id) return;

        let cancelled = false;

        setLoading(true);
        setError(null);

        roomApi.get(roomId)
            .then((data) => {
                if (!cancelled) {
                    setRoom(data);
                    setMaterielsPage(0);
                }
            })
            .catch(() => {
                if (!cancelled) {
                    setError("Impossible de charger la salle");
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
    }, [id, roomId, reloadKey]);

    if (loading) {
        return <Loader text="Chargement..." />;
    }

    if (error || !room) {
        return (
            <>
                <Link to="/rooms" className="text-decoration-none small">
                    ‹ Retour aux salles
                </Link>
                <div className="alert alert-danger mt-3" role="alert">
                    {error ?? "Salle introuvable"}
                </div>
            </>
        );
    }

    const isCours = room.typeSalle === "COURS";
    const status = getStatus(room.etat);

    const actions: {
        label: string;
        icon?: string;
        variant?: "primary";
        onClick: () => void;
    }[] = [
        ...(isCours
            ? [
                {
                    label: "Superviser les postes",
                    icon: "bi-display",
                    onClick: () => console.log("Superviser les postes", room.id),
                },
            ]
            : []),
        {
            label: "Affecter du matériel",
            icon: "bi-plus-lg",
            variant: "primary" as const,
            onClick: () => setShowAffectationPopup(true),
        },
    ];

    return (
        <>
            {showAffectationPopup && (
                <AssignmentsPopup
                    onClose={() => setShowAffectationPopup(false)}
                    onSubmit={() => {
                        setShowAffectationPopup(false);
                        setReloadKey((key) => key + 1);
                    }}
                />
            )}

            <Link to="/rooms" className="text-decoration-none small">
                ‹ Retour aux salles
            </Link>

            <div className="mt-3">
                <PageHeader
                    title={`Salle ${room.nom}`}
                    description={`Bâtiment ${room.nomBatiment ?? "-"} · ${
                        isCours ? "Salle de cours" : "Salle de stockage"
                    }`}
                    actions={actions}
                />
            </div>

            <div className="mb-4">
                <span className={`badge rounded-pill ${status.className}`}>
                    {status.label}
                </span>
            </div>

            {/* Indicateurs */}
            <div className="row g-3 mb-4">
                <StatCard
                    label={isCours ? "Postes de la salle" : "Matériels disponibles"}
                    value={room.nbMaterielDispo ?? 0}
                />
                <StatCard label="Capacité" value={room.nbPlaces ?? "-"} />
                <StatCard
                    label="Matériels affectés"
                    value={totalElements ?? 0}
                />
                <StatCard label="Bâtiment" value={room.nomBatiment ?? "-"} />
            </div>

            {/*
              TODO (à reprendre plus tard) : section « Affectation des stages ».
              Maquette : stage en cours (nom, dates, badge « En cours ») + tableau des
              stages à affecter (nom, dates, formation, bouton « Affecter le stage à une salle »).
              Mis de côté car absent du type Room : il faudra un endpoint / un champ
              (ex: stages liés à la salle) avant de l'implémenter.
              Aussi absents du type Room pour l'instant : « Postes connectés »
              et « Dernière réinstallation » (présents sur la maquette).
            */}

            <h2 className="h5 mb-3">
                Matériels de la salle{" "}
                <span className="badge text-bg-light align-middle">
                    {totalElements ?? 0}
                </span>
            </h2>

            <CardTable
                data={materiels}
                loading={loading}
                pagination={{
                    currentPage: materielsPage,
                    totalPages,
                    onPageChange: setMaterielsPage,
                }}
                card={{
                    icon: (materiel: Material) => (
                        <i
                            className={`bi ${MATERIAL_ICONS[materiel.typeMateriel] ?? "bi-box-seam"} fs-5`}
                            aria-hidden="true"
                        />
                    ),
                    title: (materiel: Material) => materiel.nom,
                    subtitle: (materiel: Material) =>
                        `${MATERIAL_LABELS[materiel.typeMateriel] ?? materiel.typeMateriel} · ${materiel.marque} ${materiel.modele}`,
                    footer: (materiel: Material) => (
                        <div className="d-flex justify-content-end small">
                            <button
                                type="button"
                                className="btn btn-link p-0 text-decoration-none text-nowrap"
                                onClick={() =>
                                    navigate(`${routes.materials}/${materiel.id}`)
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