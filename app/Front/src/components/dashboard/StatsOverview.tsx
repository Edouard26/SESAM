import { useEffect, useState } from "react";
import { statsApi } from "../../api";
import type { Stats } from "../../types/Stats.ts";
import Card from "../generic/display/Card.tsx";
import Loader from "../generic/Loader.tsx";
import useNotification from "../../hooks/useNotification.ts";

const ETAT_MATERIEL: Record<string, string> = {
    EN_ETAT: "En état",
    EN_MAINTENANCE: "En maintenance",
    DEPRECIER: "Déprécié",
};

const TYPE_MATERIEL: Record<string, string> = {
    ECRAN: "Écrans",
    UC: "Unités centrales",
    VIDEO_PROJECTEUR: "Vidéoprojecteurs",
};

const ETAT_SALLE: Record<string, string> = {
    DISPONIBLE: "Disponible",
    MAINTENANCE: "Maintenance",
    INDISPONIBLE: "Indisponible",
};

const STATUT_ALERTE: Record<string, string> = {
    EN_ATTENTE: "En attente",
    TRAITEE: "Traitée",
    ANNULEE: "Annulée",
};

const STATUT_STAGE: Record<string, string> = {
    A_AFFECTER: "À affecter",
    AFFECTER: "Affecté",
    EN_COURS: "En cours",
    CLOTURER: "Clôturé",
};

type KpiProps = {
    icon: string;
    label: string;
    value: number;
    variant?: "primary" | "warning" | "danger";
};

// Indicateur chiffré avec icône, valeur et libellé
function Kpi({ icon, label, value, variant = "primary" }: KpiProps) {
    return (
        <div className="card h-100">
            <div className="card-body d-flex align-items-center gap-3">
                <i className={`bi ${icon} fs-2 text-${variant}`} />
                <div>
                    <div className="fs-3 fw-bold lh-1">{value}</div>
                    <div className="text-secondary small">{label}</div>
                </div>
            </div>
        </div>
    );
}

type BreakdownProps = {
    title: string;
    data: Record<string, number>;
    labels: Record<string, string>;
};

// Liste "libellé : effectif" ; une clé sans libellé connu est affichée telle quelle
function Breakdown({ title, data, labels }: BreakdownProps) {
    return (
        <Card title={title}>
            <ul className="list-group list-group-flush">
                {Object.entries(data).map(([key, count]) => (
                    <li
                        key={key}
                        className="list-group-item d-flex justify-content-between px-0"
                    >
                        <span>{labels[key] ?? key}</span>
                        <span className="fw-semibold">{count}</span>
                    </li>
                ))}
            </ul>
        </Card>
    );
}

// Statistiques du centre : indicateurs clés et répartitions (matériels, salles, alertes, stages)
export default function StatsOverview() {
    const { notify } = useNotification();
    const [stats, setStats] = useState<Stats | null>(null);

    useEffect(() => {
        statsApi
            .get()
            .then(setStats)
            .catch(() => notify("Impossible de charger les statistiques.", "danger"));
    }, [notify]);

    if (!stats) {
        return (
            <div className="d-flex justify-content-center py-5">
                <Loader text="Chargement des statistiques..." />
            </div>
        );
    }

    return (
        <div className="d-flex flex-column gap-4">
            <div className="row g-3">
                <div className="col-6 col-lg-3">
                    <Kpi icon="bi-pc-display" label="Matériels" value={stats.materiels.total} />
                </div>
                <div className="col-6 col-lg-3">
                    <Kpi icon="bi-door-open" label="Salles" value={stats.salles.total} />
                </div>
                <div className="col-6 col-lg-3">
                    <Kpi
                        icon="bi-exclamation-triangle"
                        label="Alertes en attente"
                        value={stats.alertes.parStatut.EN_ATTENTE ?? 0}
                        variant="danger"
                    />
                </div>
                <div className="col-6 col-lg-3">
                    <Kpi
                        icon="bi-shield-exclamation"
                        label="Garanties expirant sous 30 jours"
                        value={stats.materiels.garantieExpirantSous30Jours}
                        variant="warning"
                    />
                </div>
            </div>

            <div className="row g-3">
                <div className="col-12 col-md-6 col-xl-4">
                    <Breakdown title="Matériels par type" data={stats.materiels.parType} labels={TYPE_MATERIEL} />
                </div>
                <div className="col-12 col-md-6 col-xl-4">
                    <Breakdown title="Matériels par état" data={stats.materiels.parEtat} labels={ETAT_MATERIEL} />
                </div>
                <div className="col-12 col-md-6 col-xl-4">
                    <Breakdown title="Salles par état" data={stats.salles.parEtat} labels={ETAT_SALLE} />
                </div>
                <div className="col-12 col-md-6 col-xl-4">
                    <Breakdown title="Alertes par statut" data={stats.alertes.parStatut} labels={STATUT_ALERTE} />
                </div>
                <div className="col-12 col-md-6 col-xl-4">
                    <Breakdown title="Stages par statut" data={stats.stages.parStatut} labels={STATUT_STAGE} />
                </div>
            </div>
        </div>
    );
}
