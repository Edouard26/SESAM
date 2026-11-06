import { useCallback, useEffect, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import PageHeader from "../../components/layout/PageHeader.tsx";
import { materialApi, processApi } from "../../api";
import type { Processus } from "../../types/Processus";
import type { EtatType, MaterialDetail, MaterialType } from "../../types/Material.ts";
import useNotification from "../../hooks/useNotification.ts";

const etatBadges: Record<EtatType, { label: string; className: string; icon: string }> = {
    EN_ETAT: {
        label: "En état",
        className: "bg-success-subtle text-success-emphasis",
        icon: "bi-check-circle",
    },
    EN_MAINTENANCE: {
        label: "En maintenance",
        className: "bg-warning-subtle text-warning-emphasis",
        icon: "bi-tools",
    },
    DEPRECIER: {
        label: "Déprécié",
        className: "bg-danger-subtle text-danger-emphasis",
        icon: "bi-x-circle",
    },
};

const typeLabels: Record<MaterialType, string> = {
    UC: "Unité centrale",
    ECRAN: "Écran",
    VIDEO_PROJECTEUR: "Projecteur",
};

const typeIcons: Record<MaterialType, string> = {
    UC: "bi-pc-display",
    ECRAN: "bi-display",
    VIDEO_PROJECTEUR: "bi-camera-video",
};

export default function Process() {
    const { id } = useParams<{ id: string }>();
    const { notify } = useNotification();

    const [material, setMaterial] = useState<MaterialDetail | null>(null);
    const [processes, setProcesses] = useState<Processus[]>([]);

    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [killingIds, setKillingIds] = useState<number[]>([]);

    const requestIdRef = useRef(0);

    const loadData = useCallback(async (isRefresh = false) => {
        if (!id || Number.isNaN(Number(id))) {
            notify("Identifiant du matériel invalide.", "danger");
            setLoading(false);
            return;
        }

        const requestId = ++requestIdRef.current;

        if (isRefresh) {
            setRefreshing(true);
        } else {
            setLoading(true);
        }

        const [materialResult, processesResult] = await Promise.allSettled([
            materialApi.get(Number(id)),
            processApi.list({
                uniteCentralId: Number(id),
            }),
        ]);

        if (requestId !== requestIdRef.current) return;

        let hasError = false;

        if (materialResult.status === "fulfilled") {
            setMaterial(materialResult.value);
        } else {
            hasError = true;
            notify("Impossible de récupérer les informations du matériel.", "danger");
        }

        if (processesResult.status === "fulfilled") {
            setProcesses(processesResult.value.content);
        } else {
            hasError = true;
            notify("Impossible de récupérer les processus.", "danger");
        }

        if (isRefresh && !hasError) {
            notify("Informations actualisées.", "success");
        }

        setLoading(false);
        setRefreshing(false);
    }, [id, notify]);

    useEffect(() => {
        void loadData();

        return () => {
            requestIdRef.current++;
        };
    }, [loadData]);

    const handleRefresh = () => {
        if (!loading && !refreshing) {
            void loadData(true);
        }
    };

    const handleKill = (process: Processus) => {
        if (killingIds.includes(process.id)) return;

        setKillingIds((previous) => [...previous, process.id]);

        window.setTimeout(() => {
            setProcesses((previous) =>
                previous.filter((item) => item.id !== process.id)
            );

            setKillingIds((previous) =>
                previous.filter((id) => id !== process.id)
            );

            notify(`Le processus "${process.nom}" a été arrêté.`, "success");
        }, 700);
    };

    const etat = material
        ? etatBadges[material.etat] ?? etatBadges.EN_ETAT
        : null;

    return (
        <div className="container-fluid">
            <PageHeader
                title="Processus"
                description={
                    material
                        ? `Processus associés à ${material.nom}.`
                        : "Liste des processus associés à cette unité centrale."
                }
                back
            />

            {loading ? (
                <div className="d-flex justify-content-center py-5">
                    <div className="spinner-border text-primary" role="status">
                        <span className="visually-hidden">Chargement...</span>
                    </div>
                </div>
            ) : (
                <>
                    {/* Informations du matériel */}
                    {material && etat ? (
                        <>
                            <div className="d-flex flex-wrap align-items-center justify-content-between gap-3 p-3 mb-3 border rounded-3 bg-body-tertiary">
                                <div className="d-flex align-items-center gap-3">
                                    <div className="rounded-3 bg-primary-subtle text-primary p-3">
                                        <i
                                            className={`bi ${typeIcons[material.typeMateriel]} fs-2`}
                                            aria-hidden="true"
                                        />
                                    </div>

                                    <div>
                                        <div className="fw-semibold">
                                            {material.nom}
                                        </div>

                                        <div className="text-secondary small">
                                            <i
                                                className="bi bi-pc-display me-2"
                                                aria-hidden="true"
                                            />
                                            {typeLabels[material.typeMateriel]}
                                        </div>

                                        <div className="text-secondary small">
                                            <i
                                                className="bi bi-upc-scan me-2"
                                                aria-hidden="true"
                                            />
                                            {material.numeroSerie || "Numéro de série indisponible"}
                                        </div>
                                    </div>
                                </div>

                                <span className={`badge rounded-pill px-3 py-2 ${etat.className}`}>
                                    <i className={`bi ${etat.icon} me-2`} aria-hidden="true" />
                                    {etat.label}
                                </span>
                            </div>

                            <div className="p-3 mb-4 border rounded-3 bg-body-tertiary">
                                {/* En-tête */}
                                <div className="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-3">
                                    <div className="d-flex align-items-center gap-3">
                                        <div className="rounded-3 bg-primary-subtle text-primary p-3">
                                            <i
                                                className={`bi ${typeIcons[material.typeMateriel]} fs-2`}
                                                aria-hidden="true"
                                            />
                                        </div>

                                        <div>
                                            <div className="fw-semibold">{material.nom}</div>
                                            <div className="text-secondary small">
                                                {typeLabels[material.typeMateriel]}
                                            </div>
                                        </div>
                                    </div>

                                    <span className={`badge rounded-pill px-3 py-2 ${etat.className}`}>
            <i className={`bi ${etat.icon} me-2`} aria-hidden="true" />
                                        {etat.label}
        </span>
                                </div>

                                <hr />

                                {/* Informations */}
                                <div className="row g-3">
                                    <div className="col-12 col-md-6">
                                        <div className="small text-secondary mb-2 fw-semibold">
                                            Identification
                                        </div>

                                        <div className="row g-2">
                                            <div className="col-6">
                                                <div className="small text-secondary">Marque</div>
                                                <div className="fw-medium">{material.marque || "—"}</div>
                                            </div>

                                            <div className="col-6">
                                                <div className="small text-secondary">Modèle</div>
                                                <div className="fw-medium">{material.modele || "—"}</div>
                                            </div>

                                            <div className="col-6">
                                                <div className="small text-secondary">Numéro de série</div>
                                                <div className="fw-medium text-break">
                                                    {material.numeroSerie || "—"}
                                                </div>
                                            </div>

                                            <div className="col-6">
                                                <div className="small text-secondary">Adresse MAC</div>
                                                <div className="fw-medium text-break">
                                                    {material.mac || "—"}
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    {(material.typeMateriel === "UC") && (
                                        <div className="col-12 col-md-6">
                                            <div className="small text-secondary mb-2 fw-semibold">
                                                Caractéristiques techniques
                                            </div>

                                            <div className="row g-2">
                                                {material.typeMateriel === "UC" && (
                                                    <>
                                                        <div className="col-12">
                                                            <div className="small text-secondary">Processeur</div>
                                                            <div className="fw-medium">
                                                                {material.processeur || "—"}
                                                            </div>
                                                        </div>

                                                        <div className="col-12">
                                                            <div className="small text-secondary">Carte graphique</div>
                                                            <div className="fw-medium">
                                                                {material.carteGraphique || "—"}
                                                            </div>
                                                        </div>
                                                    </>
                                                )}
                                            </div>
                                        </div>
                                    )}
                                </div>
                            </div>
                        </>
                    ) : (
                        <div className="alert alert-warning">
                            Impossible de récupérer les informations du matériel.
                        </div>
                    )}

                    {/* Liste des processus */}
                    <div className="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-3">
                        <div>
                            <h2 className="h5 fw-bold mb-1">
                                Processus associés
                            </h2>
                            <div className="text-secondary small">
                                Gestion des processus de cette unité centrale.
                            </div>
                        </div>

                        <button
                            type="button"
                            className="btn btn-outline-primary"
                            onClick={handleRefresh}
                            disabled={refreshing}
                        >
                            {refreshing ? (
                                <>
                                    <span
                                        className="spinner-border spinner-border-sm me-2"
                                        role="status"
                                        aria-hidden="true"
                                    />
                                    Actualisation...
                                </>
                            ) : (
                                <>
                                    <i className="bi bi-arrow-clockwise me-2" />
                                    Actualiser
                                </>
                            )}
                        </button>
                    </div>

                    <div className="d-flex align-items-center gap-2 mb-2">
                        <span className="badge bg-secondary-subtle text-secondary-emphasis">
                            {processes.length} processus
                        </span>
                    </div>

                    {processes.length === 0 ? (
                        <div className="alert alert-info">
                            Aucun processus associé à cette unité centrale.
                        </div>
                    ) : (
                        <div className="list-group">
                            {processes.map((process) => {
                                const isKilling = killingIds.includes(process.id);

                                return (
                                    <div
                                        key={process.id}
                                        className="list-group-item d-flex align-items-center justify-content-between gap-3"
                                    >
                                        <div className="d-flex align-items-center min-w-0">
                                            <i className="bi bi-cpu me-3 text-secondary fs-5" />
                                            <span className="fw-medium text-break">
                                                {process.nom}
                                            </span>
                                        </div>

                                        <button
                                            type="button"
                                            className="btn btn-sm btn-outline-danger flex-shrink-0"
                                            onClick={() => handleKill(process)}
                                            disabled={isKilling || refreshing}
                                        >
                                            {isKilling ? (
                                                <>
                                                    <span
                                                        className="spinner-border spinner-border-sm me-2"
                                                        role="status"
                                                        aria-hidden="true"
                                                    />
                                                    Arrêt...
                                                </>
                                            ) : (
                                                <>
                                                    <i className="bi bi-x-circle me-2" />
                                                    Arrêter
                                                </>
                                            )}
                                        </button>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </>
            )}
        </div>
    );
}