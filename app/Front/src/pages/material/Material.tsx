import { useEffect, useState, type ReactNode } from "react";
import { Link, useParams } from "react-router-dom";
import { materialApi } from "../../api";
import type { EtatType, MaterialDetail, MaterialType } from "../../types/Material.ts";
import PageHeader from "../../components/layout/PageHeader.tsx";
import Confirmation from "../../components/generic/overlay/Confirmation.tsx";
import useNotification from "../../hooks/useNotification.ts";
import { routes } from "../../router/routes.ts";

const etatBadges: Record<EtatType, { label: string; className: string; icon: string }> = {
    EN_ETAT: { label: "En état", className: "bg-success-subtle text-success-emphasis", icon: "bi-check-circle" },
    EN_MAINTENANCE: { label: "En maintenance", className: "bg-warning-subtle text-warning-emphasis", icon: "bi-tools" },
    DEPRECIER: { label: "Déprécié", className: "bg-danger-subtle text-danger-emphasis", icon: "bi-x-circle" },
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

const formatDate = (value?: Date | string) =>
    value ? new Date(value).toLocaleDateString("fr-FR") : "—";

type DetailItemProps = {
    icon: string;
    label: string;
    children: ReactNode;
    last?: boolean;
};

function DetailItem({ icon, label, children, last = false }: DetailItemProps) {
    return (
        <div
            className={`d-flex align-items-start gap-3 py-2 ${
                last ? "" : "border-bottom"
            }`}
        >
            <i
                className={`bi ${icon} fs-5 text-primary mt-1`}
                aria-hidden="true"
            />

            <div className="flex-grow-1">
                <div className="small text-secondary">{label}</div>
                <div className="fw-medium text-break">{children}</div>
            </div>
        </div>
    );
}

type DetailSectionProps = {
    icon: string;
    title: string;
    children: ReactNode;
};

function DetailSection({ icon, title, children }: DetailSectionProps) {
    return (
        <div className="col-12 col-md-6">
            <div className="card shadow-sm">
                <div className="card-header bg-body-tertiary d-flex align-items-center gap-2 py-3">
                    <i
                        className={`bi ${icon} text-primary fs-5`}
                        aria-hidden="true"
                    />
                    <h2 className="h6 fw-bold mb-0">{title}</h2>
                </div>

                <div className="card-body py-2">{children}</div>
            </div>
        </div>
    );
}

// Fiche détaillée d'un matériel (l'id vient de l'URL)
export default function Material() {
    const { id } = useParams<{ id: string }>();
    const [material, setMaterial] = useState<MaterialDetail | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [showReinstallConfirmation, setShowReinstallConfirmation] = useState(false);
    const { notify } = useNotification();

    useEffect(() => {
        if (!id) return;

        let cancelled = false;

        setLoading(true);
        setError(null);

        materialApi
            .get(Number(id))
            .then((data) => {
                if (!cancelled) setMaterial(data);
            })
            .catch(() => {
                if (!cancelled) setError("Impossible de charger ce matériel.");
            })
            .finally(() => {
                if (!cancelled) setLoading(false);
            });

        return () => {
            cancelled = true;
        };
    }, [id]);

    if (loading) {
        return (
            <div className="d-flex justify-content-center py-5">
                <div className="spinner-border text-primary" role="status" />
            </div>
        );
    }

    if (error || !material) {
        return (
            <>
                <PageHeader
                    title="Matériel introuvable"
                    description={error ?? "Ce matériel n'existe pas."}
                    back
                />

                <Link to={routes.materials}>Retour à la liste</Link>
            </>
        );
    }

    const etat = etatBadges[material.etat] ?? etatBadges.EN_ETAT;

    async function reinstallMaterial() {
        if (!material) return;
        try {
            await materialApi.reinstall(material.id);
            notify(`Réinstallation du poste ${material.nom} lancée.`, "success");
        } catch {
            notify("Impossible de lancer la réinstallation du poste.", "danger");
        }
    }

    return (
        <>
            <Confirmation
                open={showReinstallConfirmation}
                title="Réinstaller le poste ?"
                message={`Le poste ${material.nom} (${material.numeroSerie}) sera réinstallé.`}
                onCancel={() => setShowReinstallConfirmation(false)}
                onConfirm={() => void reinstallMaterial()}
            />

            <PageHeader
                title={material.nom}
                description="Fiche descriptive du matériel"
                back
                actions={
                    material.typeMateriel === "UC"
                        ? [
                            {
                                label: "Réinstaller le poste",
                                icon: "bi-arrow-repeat",
                                onClick: () => setShowReinstallConfirmation(true),
                            },
                        ]
                        : []
                }
            />

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
                            {typeLabels[material.typeMateriel]}
                        </div>

                        <div className="text-secondary small">
                            <i
                                className="bi bi-upc-scan me-2"
                                aria-hidden="true"
                            />
                            {material.numeroSerie}
                        </div>
                    </div>
                </div>

                <span className={`badge rounded-pill px-3 py-2 ${etat.className}`}>
                    <i className={`bi ${etat.icon} me-2`} aria-hidden="true" />
                    {etat.label}
                </span>
            </div>

            <div className="row g-3">
                <DetailSection icon="bi-info-circle" title="Identification">
                    <DetailItem icon="bi-tag" label="Marque">
                        {material.marque || "—"}
                    </DetailItem>

                    <DetailItem icon="bi-box" label="Modèle">
                        {material.modele || "—"}
                    </DetailItem>

                    <DetailItem icon="bi-ethernet" label="Adresse MAC" last>
                        {material.mac || "—"}
                    </DetailItem>
                </DetailSection>

                {(material.typeMateriel === "UC" ||
                    material.typeMateriel === "ECRAN") && (
                    <DetailSection
                        icon="bi-cpu"
                        title="Caractéristiques techniques"
                    >
                        {material.typeMateriel === "UC" && (
                            <>
                                <DetailItem icon="bi-cpu" label="Processeur">
                                    {material.processeur || "—"}
                                </DetailItem>

                                <DetailItem
                                    icon="bi-gpu-card"
                                    label="Carte graphique"
                                    last
                                >
                                    {material.carteGraphique || "—"}
                                </DetailItem>
                            </>
                        )}

                        {material.typeMateriel === "ECRAN" && (
                            <DetailItem
                                icon="bi-aspect-ratio"
                                label="Taille"
                                last
                            >
                                {material.taille
                                    ? `${material.taille}"`
                                    : "—"}
                            </DetailItem>
                        )}
                    </DetailSection>
                )}

                <DetailSection icon="bi-geo-alt" title="Affectation">
                    <DetailItem icon="bi-door-open" label="Salle">
                        {material.salleId ? (
                            <Link to={`${routes.rooms}/${material.salleId}`}>
                                {material.salleNom ||
                                    `Salle ${material.salleId}`}
                            </Link>
                        ) : (
                            "Non affecté"
                        )}
                    </DetailItem>

                    <DetailItem icon="bi-building" label="Bâtiment" last>
                        {material.nomBatiment || "—"}
                    </DetailItem>
                </DetailSection>

                <DetailSection
                    icon="bi-calendar-check"
                    title="Suivi et garantie"
                >
                    <DetailItem
                        icon="bi-calendar-plus"
                        label="Date d'ajout"
                    >
                        {formatDate(material.dateAjout)}
                    </DetailItem>

                    <DetailItem
                        icon="bi-shield-check"
                        label="Fin de garantie"
                        last
                    >
                        {formatDate(material.dateFinGarantie)}
                    </DetailItem>
                </DetailSection>
            </div>
        </>
    );
}