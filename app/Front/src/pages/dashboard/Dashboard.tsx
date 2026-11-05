
import { useEffect, useState } from "react";

import PageHeader from "../../components/layout/PageHeader.tsx";
import AlertPopup from "../../components/alert/AlertPopup.tsx";
import StatsOverview from "../../components/dashboard/StatsOverview.tsx";
import CardTable from "../../components/generic/display/CardTable.tsx";

import { useAuth } from "../../context/AuthContext.tsx";
import { Role } from "../../types/Role.ts";
import type { Material, MaterialType } from "../../types/Material.ts";
import type { Alert } from "../../types/Alert.ts";

import { alertApi, materialApi } from "../../api";

const MATERIALS_PAGE_SIZE = 6;
const ALERTS_PAGE_SIZE = 6;

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

export default function Dashboard() {
    const { user } = useAuth();

    const [showAlertPopup, setShowAlertPopup] = useState(false);

    const [materiels, setMateriels] = useState<Material[]>([]);
    const [totalElements, setTotalElements] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);
    const [loadingMateriels, setLoadingMateriels] = useState(false);
    const [errorMateriels, setErrorMateriels] = useState<string | null>(null);

    const [alerts, setAlerts] = useState<Alert[]>([]);
    const [totalAlerts, setTotalAlerts] = useState(0);
    const [currentAlertsPage, setCurrentAlertsPage] = useState(0);
    const [loadingAlerts, setLoadingAlerts] = useState(false);
    const [errorAlerts, setErrorAlerts] = useState<string | null>(null);

    const isStagiaire = user?.role === Role.STAGIAIRE;

    // Les stagiaires et formateurs peuvent consulter et envoyer des alertes.
    const canSendAlert = user != null && user.role <= Role.FORMATEUR;
    const canSeeAlerts = canSendAlert;

    const canSeeStats = user != null && user.role >= Role.GESTIONNAIRE;

    // Charge les matériels du stagiaire.
    useEffect(() => {
        if (!isStagiaire) {
            setMateriels([]);
            setTotalElements(0);
            setLoadingMateriels(false);
            return;
        }

        let cancelled = false;

        async function loadMateriels() {
            setLoadingMateriels(true);
            setErrorMateriels(null);

            try {
                const result = await materialApi.list({
                    page: currentPage,
                    size: MATERIALS_PAGE_SIZE,
                    sort: "nom,asc",
                });

                if (!cancelled) {
                    setMateriels(result.content);
                    setTotalElements(result.totalElements);
                }
            } catch {
                if (!cancelled) {
                    setMateriels([]);
                    setTotalElements(0);
                    setErrorMateriels("Impossible de charger vos matériels.");
                }
            } finally {
                if (!cancelled) {
                    setLoadingMateriels(false);
                }
            }
        }

        void loadMateriels();

        return () => {
            cancelled = true;
        };
    }, [isStagiaire, currentPage]);

    // Charge les alertes de l'utilisateur connecté.
    // Le backend applique automatiquement le filtrage selon l'utilisateur.
    useEffect(() => {
        if (!canSeeAlerts) {
            setAlerts([]);
            setTotalAlerts(0);
            setLoadingAlerts(false);
            return;
        }

        let cancelled = false;

        async function loadAlerts() {
            setLoadingAlerts(true);
            setErrorAlerts(null);

            try {
                const result = await alertApi.list({
                    page: currentAlertsPage,
                    size: ALERTS_PAGE_SIZE,
                });

                if (!cancelled) {
                    setAlerts(result.content);
                    setTotalAlerts(result.totalElements);
                }
            } catch {
                if (!cancelled) {
                    setAlerts([]);
                    setTotalAlerts(0);
                    setErrorAlerts("Impossible de charger vos alertes.");
                }
            } finally {
                if (!cancelled) {
                    setLoadingAlerts(false);
                }
            }
        }

        void loadAlerts();

        return () => {
            cancelled = true;
        };
    }, [canSeeAlerts, currentAlertsPage]);

    return (
        <>
            <PageHeader
                title="Vue d’ensemble"
                description="Centre de formation."
                actions={
                    canSendAlert
                        ? [
                            {
                                label: "Envoyer une alerte",
                                icon: "bi bi-exclamation-triangle",
                                onClick: () => setShowAlertPopup(true),
                            },
                        ]
                        : []
                }
            />

            {canSeeStats && <StatsOverview />}

            {isStagiaire && (
                <section className="mt-4">
                    <h2 className="h5 mb-3">
                        Mes matériels{" "}
                        <span className="badge text-bg-light align-middle">
                            {totalElements}
                        </span>
                    </h2>

                    {errorMateriels && (
                        <div className="alert alert-danger" role="alert">
                            {errorMateriels}
                        </div>
                    )}

                    {!loadingMateriels &&
                        !errorMateriels &&
                        materiels.length === 0 && (
                            <div className="alert alert-info" role="status">
                                Aucun matériel disponible.
                            </div>
                        )}

                    {(loadingMateriels || materiels.length > 0) && (
                        <CardTable<Material>
                            data={materiels}
                            loading={loadingMateriels}
                            pagination={{
                                currentPage,
                                totalPages: Math.ceil(
                                    totalElements / MATERIALS_PAGE_SIZE
                                ),
                                onPageChange: setCurrentPage,
                            }}
                            card={{
                                icon: (materiel) => (
                                    <i
                                        className={`bi ${
                                            MATERIAL_ICONS[materiel.typeMateriel] ??
                                            "bi-box-seam"
                                        } fs-5`}
                                        aria-hidden="true"
                                    />
                                ),
                                title: (materiel) => materiel.nom,
                                subtitle: (materiel) =>
                                    `${
                                        MATERIAL_LABELS[materiel.typeMateriel] ??
                                        materiel.typeMateriel
                                    } · ${materiel.marque} ${materiel.modele}`,
                            }}
                        />
                    )}
                </section>
            )}

            {canSeeAlerts && (
                <section className="mt-4">
                    <h2 className="h5 mb-3">
                        Mes alertes{" "}
                        <span className="badge text-bg-light align-middle">
                            {totalAlerts}
                        </span>
                    </h2>

                    {errorAlerts && (
                        <div className="alert alert-danger" role="alert">
                            {errorAlerts}
                        </div>
                    )}

                    {!loadingAlerts &&
                        !errorAlerts &&
                        alerts.length === 0 && (
                            <div className="alert alert-info" role="status">
                                Aucune alerte signalée.
                            </div>
                        )}

                    {(loadingAlerts || alerts.length > 0) && (
                        <CardTable<Alert>
                            data={alerts}
                            loading={loadingAlerts}
                            pagination={{
                                currentPage: currentAlertsPage,
                                totalPages: Math.ceil(
                                    totalAlerts / ALERTS_PAGE_SIZE
                                ),
                                onPageChange: setCurrentAlertsPage,
                            }}
                            card={{
                                icon: () => (
                                    <i
                                        className="bi bi-exclamation-triangle text-warning fs-5"
                                        aria-hidden="true"
                                    />
                                ),
                                title: (alert) =>
                                    alert.typeProbleme || "Alerte",
                                subtitle: (alert) =>
                                    new Date(
                                        alert.dateSignalement
                                    ).toLocaleString("fr-FR"),
                                footer: (alert) => (
                                    <div className="small text-body-secondary">
                                        {alert.description}
                                    </div>
                                ),
                            }}
                        />
                    )}
                </section>
            )}

            {showAlertPopup && canSendAlert && user && (
                <AlertPopup
                    user={user}
                    onClose={() => setShowAlertPopup(false)}
                    onSubmit={() => {
                        setShowAlertPopup(false);
                        setCurrentAlertsPage(0);
                    }}
                />
            )}
        </>
    );
}
