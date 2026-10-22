import { useEffect, useState, type ReactNode } from "react";
import { Link, useParams } from "react-router-dom";
import { stageApi } from "../../api";
import type { Stage as StageModel, StageStatut } from "../../types/Stage.ts";
import PageHeader from "../../components/layout/PageHeader.tsx";
import { routes } from "../../router/routes.ts";

type StageDetail = StageModel & {
    salleNom?: string;
    stagiaires?: {
        id: number;
        nom: string;
        prenom: string;
    }[];
};

const statutLabels: Record<StageStatut, string> = {
    A_AFFECTER: "À affecter",
    AFFECTER: "Affecté",
    EN_COURS: "En cours",
    CLOTURER: "Clôturé",
};

const statutClasses: Record<StageStatut, string> = {
    A_AFFECTER: "bg-warning-subtle text-warning-emphasis",
    AFFECTER: "bg-info-subtle text-info-emphasis",
    EN_COURS: "bg-success-subtle text-success-emphasis",
    CLOTURER: "bg-secondary-subtle text-secondary-emphasis",
};

const statutIcons: Record<StageStatut, string> = {
    A_AFFECTER: "bi-hourglass-split",
    AFFECTER: "bi-calendar-check",
    EN_COURS: "bi-play-circle",
    CLOTURER: "bi-check-circle",
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
            <div className="card shadow-sm h-100">
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

export default function Stage() {
    const { id } = useParams<{ id: string }>();

    const [stage, setStage] = useState<StageDetail | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!id) {
            setError("Identifiant du stage invalide.");
            setLoading(false);
            return;
        }

        let cancelled = false;

        setLoading(true);
        setError(null);

        stageApi
            .get(Number(id))
            .then((data) => {
                if (!cancelled) {
                    setStage(data as StageDetail);
                }
            })
            .catch(() => {
                if (!cancelled) {
                    setError("Impossible de charger ce stage.");
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
    }, [id]);

    if (loading) {
        return (
            <div className="d-flex justify-content-center py-5">
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Chargement...</span>
                </div>
            </div>
        );
    }

    if (error || !stage) {
        return (
            <>
                <PageHeader
                    title="Stage introuvable"
                    description={error ?? "Ce stage n'existe pas."}
                    back
                />

                <Link to={routes.stages}>Retour à la liste</Link>
            </>
        );
    }

    return (
        <>
            <PageHeader
                title={`Stage ${stage.code}`}
                description="Fiche descriptive du stage"
                back
            />

            <div className="d-flex flex-wrap align-items-center justify-content-between gap-3 p-3 mb-3 border rounded-3 bg-body-tertiary">
                <div className="d-flex align-items-center gap-3">
                    <div className="rounded-3 bg-primary-subtle text-primary p-3">
                        <i
                            className="bi bi-mortarboard fs-2"
                            aria-hidden="true"
                        />
                    </div>

                    <div>
                        <div className="fw-semibold">{stage.code}</div>
                        <div className="text-secondary small">
                            <i
                                className="bi bi-hash me-2"
                                aria-hidden="true"
                            />
                            Identifiant : {stage.id}
                        </div>
                    </div>
                </div>

                <span
                    className={`badge rounded-pill px-3 py-2 ${
                        statutClasses[stage.statut]
                    }`}
                >
                    <i
                        className={`bi ${statutIcons[stage.statut]} me-2`}
                        aria-hidden="true"
                    />
                    {statutLabels[stage.statut]}
                </span>
            </div>

            <div className="row g-3">
                <DetailSection
                    icon="bi-info-circle"
                    title="Informations générales"
                >
                    <DetailItem icon="bi-upc" label="Code du stage">
                        {stage.code || "—"}
                    </DetailItem>

                    <DetailItem
                        icon="bi-flag"
                        label="Statut"
                        last
                    >
                        {statutLabels[stage.statut]}
                    </DetailItem>
                </DetailSection>

                <DetailSection
                    icon="bi-calendar-range"
                    title="Période du stage"
                >
                    <DetailItem
                        icon="bi-calendar-event"
                        label="Date de début"
                    >
                        {formatDate(stage.dateDebut)}
                    </DetailItem>

                    <DetailItem
                        icon="bi-calendar-check"
                        label="Date de fin"
                        last
                    >
                        {formatDate(stage.dateFin)}
                    </DetailItem>
                </DetailSection>

                <DetailSection
                    icon="bi-geo-alt"
                    title="Affectation"
                >
                    <DetailItem icon="bi-door-open" label="Salle" last>
                        {stage.salleId != null ? (
                            <Link to={`${routes.rooms}/${stage.salleId}`}>
                                {stage.salleNom || `Salle ${stage.salleId}`}
                            </Link>
                        ) : (
                            "Aucune salle affectée"
                        )}
                    </DetailItem>
                </DetailSection>

                <DetailSection
                    icon="bi-people"
                    title={`Stagiaires (${stage.stagiairesId?.length ?? 0})`}
                >
                    {stage.stagiaires && stage.stagiaires.length > 0 ? (
                        stage.stagiaires.map((stagiaire, index) => (
                            <DetailItem
                                key={stagiaire.id}
                                icon="bi-person"
                                label={`Stagiaire ${index + 1}`}
                                last={index === stage.stagiaires!.length - 1}
                            >
                                {stagiaire.prenom} {stagiaire.nom}
                            </DetailItem>
                        ))
                    ) : stage.stagiairesId?.length > 0 ? (
                        <DetailItem
                            icon="bi-people"
                            label="Nombre de stagiaires"
                            last
                        >
                            {stage.stagiairesId.length}
                        </DetailItem>
                    ) : (
                        <div className="text-secondary text-center py-3">
                            <i className="bi bi-person-x fs-3 d-block mb-2" />
                            Aucun stagiaire associé
                        </div>
                    )}
                </DetailSection>
            </div>
        </>
    );
}