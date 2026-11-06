import { useCallback, useEffect, useRef, useState, type ReactNode } from "react";
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

type InfoItemProps = {
    icon: string;
    label: string;
    children: ReactNode;
    last?: boolean;
};

function InfoItem({ icon, label, children, last = false }: InfoItemProps) {
    return (
        <div className={`d-flex align-items-start gap-3 py-2 ${last ? "" : "border-bottom"}`}>
            <i className={`bi ${icon} fs-5 text-primary mt-1`} aria-hidden="true" />

            <div className="flex-grow-1">
                <div className="small text-secondary">{label}</div>
                <div className="fw-medium text-break">{children}</div>
            </div>
        </div>
    );
}

type InfoSectionProps = {
    icon: string;
    title: string;
    children: ReactNode;
};

function InfoSection({ icon, title, children }: InfoSectionProps) {
    return (
        <div className="col-12 col-md-6">
            <div className="card shadow-sm h-100">
                <div className="card-header bg-body-tertiary d-flex align-items-center gap-2 py-3">
                    <i className={`bi ${icon} text-primary fs-5`} aria-hidden="true" />
                    <h2 className="h6 fw-bold mb-0">{title}</h2>
                </div>

                <div className="card-body py-2">
                    {children}
                </div>
            </div>
        </div>
    );
}

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

                            <div className="row g-3 mb-4">
                                <InfoSection icon="bi-info-circle" title="Identification">
                                    <InfoItem icon="bi-tag" label="Marque">
                                        {material.marque || "—"}
                                    </InfoItem>

                                    <InfoItem icon="bi-box" label="Modèle">
                                        {material.modele || "—"}
                                    </InfoItem>

                                    <InfoItem icon="bi-upc-scan" label="Numéro de série">
                                        {material.numeroSerie || "—"}
                                    </InfoItem>

                                    <InfoItem icon="bi-ethernet" label="Adresse MAC" last>
                                        {material.mac || "—"}
                                    </InfoItem>
                                </InfoSection>

                                {(material.typeMateriel === "UC") && (
                                    <InfoSection icon="bi-cpu" title="Caractéristiques techniques">
                                        <InfoItem icon="bi-cpu" label="Processeur">
                                            {material.processeur || "—"}
                                        </InfoItem>

                                        <InfoItem icon="bi-gpu-card" label="Carte graphique" last>
                                            {material.carteGraphique || "—"}
                                        </InfoItem>
                                    </InfoSection>
                                )}
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